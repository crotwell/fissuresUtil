package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.Logger;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * BasicSeismogramDisplay.java
 *
 *
 * Created: Thu Jun  6 09:52:51 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class BasicSeismogramDisplay extends JComponent implements SeismogramDisplay{
    
    public BasicSeismogramDisplay(LocalSeismogram seis, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), new MinMaxAmpConfig(), timeBorder);
    }
   
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, boolean timeBorder){
	this(seis, tr, new MinMaxAmpConfig(), timeBorder);
    }
    
    public BasicSeismogramDisplay(LocalSeismogram seis, AmpRangeConfig ar, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), ar, timeBorder);
    }

    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder){
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
	tr.addTimeSyncListener(this);
	ar.addAmpSyncListener(this);
	this.timeConfig = tr;
	this.ampConfig = ar;
	this.addSeismogram(seis);
	scaleBorder = new ScaleBorder();
	if(timeBorder)
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
											BorderFactory.createLoweredBevelBorder())));
	plot = new ImageMaker();
	this.add(plot);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    class ImageMaker extends JComponent{
	public void paint(Graphics g){
	    if(redo)
		this.createOversizedImage();
	    long endTime = timeConfig.getTimeRange().getEndTime().getMicroSecondTime();
	    long beginTime = timeConfig.getTimeRange().getBeginTime().getMicroSecondTime();
	    long overEndTime = overTimeRange.getEndTime().getMicroSecondTime();
	    long overBeginTime = overTimeRange.getBeginTime().getMicroSecondTime();
	    if(endTime >= overEndTime || beginTime <= overBeginTime) 
		this.createOversizedImage();
	    //displayInterval.getValue() != timeConfig.getTimeRange().getInterval().getValue()
	    Graphics2D g2 = (Graphics2D)g;
	    double offset = (beginTime - overBeginTime)/ (double)(overEndTime - overBeginTime) * overSize.getWidth();
	    AffineTransform tx;
	    if(displayInterval.getValue() == timeConfig.getTimeRange().getInterval().getValue()){
		tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		g2.drawImage(overSizedImage, tx, null);
	    } else{
		double scale = displayInterval.getValue()/timeConfig.getTimeRange().getInterval().getValue();
		tx = AffineTransform.getTranslateInstance(-offset * scale, 0.0);
		tx.scale(scale, 1);
		g2.drawImage(overSizedImage, tx, null);
		redo = true;
		repaint();
	    }
	   
	} 

	public void createOversizedImage(){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(2);
	    redo = false;
	    Dimension d = getSize();
	    int sizeScale = 5;
	    int w = d.width * sizeScale, h = d.height;
	    overSize = new Dimension(w, h);
	    overSizedImage = createImage(w, h);
	    displayInterval = timeConfig.getTimeRange().getInterval();
	    Graphics2D overSizedGraphic = (Graphics2D)overSizedImage.getGraphics();
	    Iterator e = plotters.keySet().iterator();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		overSizedGraphic.setColor((Color)plotters.get(current));
		overSizedGraphic.draw(current.draw(overSize));
	    }
	}
    }
    
    /**
     * Adds a seismogram to the display
     *
     */
    public void addSeismogram(LocalSeismogram newSeismogram){
	SeismogramPlotter newPlotter = new SeismogramPlotter(newSeismogram, timeConfig, ampConfig);
	plotters.put(newPlotter, colors[plotters.size()%colors.length]);
	timeConfig.addSeismogram(newSeismogram); 
	ampConfig.addSeismogram(newSeismogram);
	redo = true;
    }
    
    /**
     * Removes a seismogram from the display
     *
     * 
     */
    public void removeSeismogram(LocalSeismogram oldSeis){}

   public void removeAllSeismograms(){
       Iterator e = plotters.keySet().iterator();
       while(e.hasNext()){
	   LocalSeismogram current = ((SeismogramPlotter)e.next()).getSeismogram();
	   timeConfig.removeSeismogram(current);
	   ampConfig.removeSeismogram(current);
       }
       timeConfig.removeTimeSyncListener(this);
       ampConfig.removeAmpSyncListener(this);
   }

       /**
     * Returns the amp range configurator the display is using
     *
     * 
     */
    public AmpRangeConfig getAmpConfig(){ return ampConfig; }
    
    public void updateAmpRange(){
	redo = true;
	this.ampScaleMap.setUnitRange(ampConfig.getAmpRange());
	repaint();
    }

    /**
     * Returns the time range configurator the display is using
     *
     * 
     */
    public TimeRangeConfig getTimeConfig(){ return timeConfig; }
    
    public void updateTimeRange(){
	this.timeScaleMap.setTimes(timeConfig.getTimeRange().getBeginTime(), 
				   timeConfig.getTimeRange().getEndTime());
	repaint();
	
    }

    public void addBottomTimeBorder(){	
	scaleBorder.setBottomScaleMapper(timeScaleMap); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void removeBottomTimeBorder(){ 
	scaleBorder.clearBottomScaleMapper(); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }


    public void addTopTimeBorder(){ 
	scaleBorder.setTopScaleMapper(timeScaleMap);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void removeTopTimeBorder(){ 
	scaleBorder.clearTopScaleMapper();
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void redraw(){
	redo = true;
	repaint();
    }

    protected void resize() {
	Dimension dim = getSize();
        Insets insets = getInsets();
	timeScaleMap.setTotalPixels(dim.width-insets.left-insets.right);
        ampScaleMap.setTotalPixels(dim.height-insets.top-insets.bottom);
	redo = true;
	repaint();
    }

    protected ImageMaker plot;

    protected HashMap plotters = new HashMap();
    
    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig;

    protected ScaleBorder scaleBorder;

    protected TimeScaleMapper timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    protected LeftTitleBorder titleBorder;

    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };
    
    protected boolean redo = true;

    protected MicroSecondTimeRange overTimeRange;

    protected TimeInterval displayInterval;
    
    protected Image overSizedImage;

    protected Dimension overSize;
    }// BasicSeismogramDisplay
