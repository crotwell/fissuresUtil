package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.Logger;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Every BasicSeismogramDisplay has a TimeRangeConfig, an AmpRangeConfig, and one to several SeismogramPlotters. It 
 * combines these objects to display a the seismograms it has been given.
 *
 *
 * Created: Thu May 23 10:25:31 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class BasicSeismogramDisplay extends JLayeredPane implements SeismogramDisplay, TimeSyncListener, AmpSyncListener, ControlChangeListener{
    
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance with the seismogram passed and a BoundedTimeConfig and MinMaxAmp config
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param ct the ControlToolbar for this display
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, ControlToolbar ct){
	this(seis, new BoundedTimeConfig(), new OffsetMeanAmpConfig(), ct);
    }
   
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance with the seismogram and TimeRangeConfig passed, and a MinMaxAmp config
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param tr a <code>TimeRangeConfig</code> value
     * @param ct the ControlToolbar for this display
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, ControlToolbar ct){
	this(seis, tr, new MinMaxAmpConfig(), ct);
    }
    
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance with the seismogram and AmpRangeConfig passed and a BoundedTimeConfig
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param ar an <code>AmpRangeConfig</code> value
     * @param ct the ControlToolbar for this display
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, AmpRangeConfig ar, ControlToolbar ct){
	this(seis, new RelativeTimeConfig(), ar, ct);
    }
    
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance. Adds borders based on the time and amplitude specified by the two 
     * configs.  Also adds a mouse listener on the componenet that fires a MicroSecondTimeRangeEvent indicating zoom.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param tr a <code>TimeRangeConfig</code> value
     * @param ar an <code>AmpRangeConfig</code> value
     * @param ct the ControlToolbar for this display
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, ControlToolbar ct){
	super();
	this.setLayout(new OverlayLayout(this));
	this.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
        setMinimumSize(new Dimension(100, 50));
        setPreferredSize(new Dimension(400, 500));
	tr.addTimeSyncListener(this);
	ar.addAmpSyncListener(this);
	this.timeConfig = tr;
	this.ampConfig = ar;
	this.controls = ct;
	controls.addControlBehaviorListener(this);
	this.addSeismogram(seis);
	scaleBorder = new ScaleBorder();
	scaleBorder.setBottomScaleMapper(timeScaleMap);
	scaleBorder.setLeftScaleMapper(ampScaleMap);        
        titleBorder = 
            new LeftTitleBorder("");
	setBorder(BorderFactory.createCompoundBorder(
						     BorderFactory.createCompoundBorder(
											BorderFactory.createRaisedBevelBorder(),
											titleBorder),
						     BorderFactory.createCompoundBorder(
											scaleBorder,
											BorderFactory.createLoweredBevelBorder()))
		  );
	current = new MouseInputAdapter() {
		public void mouseClicked(MouseEvent e){
		    timeConfig.fireTimeRangeEvent(new TimeSyncEvent(.125, -.125, false));
		    ampConfig.visibleAmpCalc(timeConfig);
		}};
	addMouseListener(current);
	
    }

    

    /**
     * <code>addSeismogram</code> adds a new seismogram to the current display
     *
     * @param seismogram a <code>LocalSeismogram</code> value
     */
    public void addSeismogram(LocalSeismogram seismogram){
	timeConfig.addSeismogram(seismogram); 
	ampConfig.addSeismogram(seismogram);
	BasicSeismogramPlotter newSeismo = new BasicSeismogramPlotter(seismogram, 
								      timeConfig.getTimeRange(seismogram),
								      ampConfig.getAmpRange(seismogram),
								      colors[seismoPlotters.size()%colors.length]);
	this.seismoPlotters.add(newSeismo);
	this.add(newSeismo);
    }
    
    /**
     * <code>getAmpConfig</code> returns the ampConfig for the current display
     *
     * @return an <code>AmpRangeConfig</code> value
     */
    public AmpRangeConfig getAmpConfig(){ return ampConfig; }

    /**
     * <code>updateAmpRange</code> updates the amp range for all of the seismograms held in the object and its amp border
     *
     */
    public void updateAmpRange(){
	Iterator e = seismoPlotters.iterator();
	while(e.hasNext()){
	    BasicSeismogramPlotter current = ((BasicSeismogramPlotter)e.next());
	    try{current.setData(ampConfig.getAmpRange(current.getSeismogram()));
	    }
	    catch(Exception f){}
	}
	this.ampScaleMap.setUnitRange(ampConfig.getAmpRange());
	repaint();
    }
    
    /**
     * <code>getTimeConfig</code> returns the time config for this display
     *
     * @return a <code>TimeRangeConfig</code> value
     */
    public TimeRangeConfig getTimeConfig(){ return timeConfig; }

    /**
     * <code>updateTimeRange</code> updates the time range for all of the seismograms held in this object and its time border
     *
     */
    public void updateTimeRange(){
	Iterator e = seismoPlotters.iterator();
	while(e.hasNext()){
	    BasicSeismogramPlotter current = ((BasicSeismogramPlotter)e.next());
	    current.setData(timeConfig.getTimeRange(current.getSeismogram()));
	}
	this.timeScaleMap.setTimes(timeConfig.getTimeRange().getBeginTime(), 
				   timeConfig.getTimeRange().getEndTime());
	repaint();
    }
    
    /**
     * <code>removeSeismogram</code> removes a seismogram from the current display
     *
     * @param oldSeis a <code>LocalSeismogram</code> value
     */
    public void removeSeismogram(LocalSeismogram oldSeis){
	Iterator e = seismoPlotters.iterator();
	while(e.hasNext()){
	    BasicSeismogramPlotter current = ((BasicSeismogramPlotter)e.next());
	    if(current.getSeismogram() == oldSeis){
		this.remove(current);
		e.remove();
		timeConfig.removeSeismogram(oldSeis);
		ampConfig.removeSeismogram(oldSeis);
		repaint();
	    }
	}
    }
    
    protected void resize() {
	Dimension dim = getSize();
        Insets insets = getInsets();
	timeScaleMap.setTotalPixels(dim.width-insets.left-insets.right);
        ampScaleMap.setTotalPixels(dim.height-insets.top-insets.bottom);
	Iterator e = seismoPlotters.iterator();
	while(e.hasNext())
	    ((BasicSeismogramPlotter)e.next()).setRedo(true);
	repaint();
    }

    public ControlToolbar getControls(){ return controls; }
    
    public void setControlBehavior(String type){
	if(type.equals("Zoom In")){
	    removeMouseListener(current);
	    removeMouseMotionListener(motion);
	    current = new MouseInputAdapter() {
		    public void mouseClicked(MouseEvent e){
			double centerOffset = (e.getX() - getSize().getWidth()/2)/getSize().getWidth();
			timeConfig.fireTimeRangeEvent(new TimeSyncEvent(.125 + centerOffset, -.125 + centerOffset, false));
			ampConfig.visibleAmpCalc(timeConfig);
		    }};
	    addMouseListener(current);
	}
	
	else if(type.equals("Zoom Out")){
	    removeMouseListener(current);
	    removeMouseMotionListener(motion);
	    current = new MouseInputAdapter() {
		    public void mouseClicked(MouseEvent e){
			timeConfig.fireTimeRangeEvent(new TimeSyncEvent(-.125, .125, false));
			ampConfig.visibleAmpCalc(timeConfig);
		    }
		};
	    addMouseListener(current);
	}
	
	else if(type.equals("Pan")){
	    removeMouseListener(current);
	    motion = new MouseMotionAdapter() {
		    int x;
		    double width = getSize().getWidth();	    
		    
		    public void mouseMoved(MouseEvent e){
			x = e.getX();
			}
		    public void mouseDragged(MouseEvent e){
			int xDiff = x - e.getX();
			x = e.getX();
			timeConfig.fireTimeRangeEvent(new TimeSyncEvent(xDiff/width, xDiff/width, false));
			ampConfig.visibleAmpCalc(timeConfig);
		    }
		};
	    addMouseMotionListener(motion);	     
	    	}
	
	else if(type.equals("Select")){
	    removeMouseListener(current);
	    removeMouseMotionListener(motion);
	    current = new MouseInputAdapter() {
		    public void mousePressed(MouseEvent e){
			Rectangle checkArea = new Rectangle(e.getX()-4, e.getY()-4, 4, 4);
			Iterator f = seismoPlotters.iterator();
			while(f.hasNext())
			    ((BasicSeismogramPlotter)f.next()).check(checkArea);
			repaint();
		    }
		};
	    addMouseListener(current);}
    }
    
    /**
     * <code>seismoPlotters</code> a linked list holding all of the seismograms currently being displayed
     *
     */
    protected LinkedList seismoPlotters = new LinkedList();

    protected TimeScaleMapper timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder

    protected ScaleBorder scaleBorder;
    
    protected LeftTitleBorder titleBorder;
    
    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig; 

    protected MouseInputAdapter current;

    protected MouseMotionAdapter motion;

    protected ControlToolbar controls;
    
    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };

    public static void main(String[] args){
	try{
	    JFrame jf = new JFrame("Test Seismogram View");
	    LocalSeismogram test1 = SeisPlotUtil.createSineWave();
	    LocalSeismogram test2 = SeisPlotUtil.createTestData();
	    BasicSeismogramDisplay sv = new BasicSeismogramDisplay(test1, new ControlToolbar());
	    //sv.addSeismogram(test2);
	    Dimension size = new Dimension(400, 200);
	    sv.setPreferredSize(size);
	    jf.getContentPane().add(sv.getControls().getToolBar(), java.awt.BorderLayout.NORTH);
	    jf.getContentPane().add(sv, java.awt.BorderLayout.CENTER);
	    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    jf.setSize(size);
	    jf.setVisible(true);
	    jf.addWindowListener(new WindowAdapter() {
		    public void windowClosed(WindowEvent e) {
			System.exit(0);
		    }
		});
	    JFrame jf2 = new JFrame("Test Seismogram View 2");
	    LocalSeismogram test3 = SeisPlotUtil.createLowSineWave(0,1);
	    LocalSeismogram test4 = SeisPlotUtil.createTestData();
	    BasicSeismogramDisplay sv2 = new BasicSeismogramDisplay(test3, sv.getTimeConfig(), sv.getAmpConfig(), sv.getControls());
	    sv2.addSeismogram(test4);
	    sv2.setPreferredSize(size);
	    jf2.getContentPane().add(sv2, java.awt.BorderLayout.CENTER);
	    jf2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    jf2.setSize(size);
	    jf2.setVisible(true);
	    jf2.addWindowListener(new WindowAdapter() {
		    public void windowClosed(WindowEvent e) {
			System.exit(0);
		    }
		});
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}// BasicSeismogramDisplay
