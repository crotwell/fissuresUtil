package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

import java.util.HashMap;
import java.util.Iterator;

import java.lang.ref.SoftReference;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import org.apache.log4j.*;

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
	    if(overSizedImage == null){
		logger.debug("the image is null and is being recreated");
		this.createOversizedImage();
	    }
	    long endTime = timeConfig.getTimeRange().getEndTime().getMicroSecondTime();
	    long beginTime = timeConfig.getTimeRange().getBeginTime().getMicroSecondTime();
	    long overEndTime = overTimeRange.getEndTime().getMicroSecondTime();
	    long overBeginTime = overTimeRange.getBeginTime().getMicroSecondTime();
	    if(endTime >= overEndTime || beginTime <= overBeginTime) 
		this.createOversizedImage();
	    Graphics2D g2 = (Graphics2D)g;
	    double offset = (beginTime - overBeginTime)/ (double)(overEndTime - overBeginTime) * overSize.getWidth();
	    AffineTransform tx;
	    if(displayInterval.getValue() == timeConfig.getTimeRange().getInterval().getValue()){
		tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		if(overSizedImage.get() == null) 
		    this.createOversizedImage();
		g2.drawImage(((Image)overSizedImage.get()), tx, null);
	    } else{
		double scale = displayInterval.getValue()/timeConfig.getTimeRange().getInterval().getValue();
		tx = AffineTransform.getTranslateInstance(-offset * scale, 0.0);
		tx.scale(scale, 1);
		this.createOversizedImage(); 
		g2.drawImage(((Image)overSizedImage.get()), tx, null);
		overSizedImage = null;
		repaint();
	    }
	   
	} 

	public void createOversizedImage(){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(2);
	    Dimension d = getSize();
	    int sizeScale = 5;
	    int w = d.width * sizeScale, h = d.height;
	    overSize = new Dimension(w, h);
	    overSizedImage = new SoftReference(createImage(w, h));
	    displayInterval = timeConfig.getTimeRange().getInterval();
	    Graphics2D overSizedGraphic = (Graphics2D)((Image)overSizedImage.get()).getGraphics();
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
	overSizedImage = null;
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
	overSizedImage = null;
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
	overSizedImage = null;
	repaint();
    }

    protected void resize() {
	Dimension dim = getSize();
        Insets insets = getInsets();
	timeScaleMap.setTotalPixels(dim.width-insets.left-insets.right);
        ampScaleMap.setTotalPixels(dim.height-insets.top-insets.bottom);
	overSizedImage = null;
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
    
    protected MicroSecondTimeRange overTimeRange;

    protected TimeInterval displayInterval;
    
    protected SoftReference overSizedImage;

    protected Dimension overSize;

    static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());

}// BasicSeismogramDisplay
