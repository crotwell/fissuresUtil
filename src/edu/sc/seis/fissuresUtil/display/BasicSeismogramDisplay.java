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
	this(seis, new BoundedTimeConfig(), new RMeanAmpConfig(), timeBorder);
    }
   
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, boolean timeBorder){
	this(seis, tr, new RMeanAmpConfig(), timeBorder);
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
	setBorder(BorderFactory.createCompoundBorder(
		  BorderFactory.createCompoundBorder(
		  BorderFactory.createRaisedBevelBorder(),
		  new LeftTitleBorder("")),
						     BorderFactory.createCompoundBorder(
											scaleBorder,
											BorderFactory.createLoweredBevelBorder())));
	Dimension d = getSize();
	Insets insets = this.getInsets();
	int w = (d.width - insets.left - insets.right) * 5, h = d.height - insets.top - insets.bottom;
	overSize = new Dimension(w, h);
	imagePainter = new ImagePainter();
	this.add(imagePainter);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
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
       this.stopImageCreation();
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
        Insets insets = getInsets();
	Dimension d = getSize();
	int w = (d.width - insets.left - insets.right) * 5, h = d.height - insets.top - insets.bottom;
	overSize = new Dimension(w, h);
	timeScaleMap.setTotalPixels(d.width-insets.left-insets.right);
        ampScaleMap.setTotalPixels(d.height-insets.top-insets.bottom);
	redo = true;
	repaint();
    }

    public void stopImageCreation(){
	synchronized(imageMaker){ imageMaker.remove(imagePainter); }
    }

    protected static ImageMaker imageMaker = new ImageMaker();

    protected Dimension overSize;

    protected HashMap plotters = new HashMap();
    
    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig;

    protected ScaleBorder scaleBorder;

    protected TimeScaleMapper timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };
    
    static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());

    protected ImagePainter imagePainter;

    protected boolean redo;

    protected class ImagePainter extends JComponent{
	public void paint(Graphics g){
	    if(overSizedImage == null){
		logger.debug("the image is null and is being created");
		synchronized(this){ displayInterval = timeConfig.getTimeRange().getInterval(); }
		this.createImage();
		return;
		}
	    if(overSizedImage.get() == null){
		logger.debug("image was garbage collected, and is being recreated");
		synchronized(this){ displayInterval = timeConfig.getTimeRange().getInterval(); }
		this.createImage();
		return;
	    }
	    long endTime = timeConfig.getTimeRange().getEndTime().getMicroSecondTime();
	    long beginTime = timeConfig.getTimeRange().getBeginTime().getMicroSecondTime();
	    Graphics2D g2 = (Graphics2D)g;
	    if(displayTime == timeConfig.getTimeRange().getInterval().getValue()){
		synchronized(this){
		    double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * overSize.getWidth();
		    g2.drawImage(((Image)overSizedImage.get()), AffineTransform.getTranslateInstance(-offset, 0.0), null);
		}
		if(redo){
		    logger.debug("the image is being redone");
		    this.createImage();
		}
		redo = false;
	    } else{
		synchronized(this){
		    double scale = displayTime/timeConfig.getTimeRange().getInterval().getValue(); 
		    double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * (overSize.getWidth() * scale);
		    AffineTransform tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		    tx.scale(scale, 1);
		    g2.drawImage(((Image)overSizedImage.get()), tx, null);
		    displayInterval = timeConfig.getTimeRange().getInterval();
		}
		this.createImage();
	    }
	} 
	
	public synchronized void createImage(){
	    imageMaker.createImage(this, new PlotInfo(overSize, plotters, displayInterval));
	}

	public synchronized void setImage(Image newImage){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(3);
	    displayTime = displayInterval.getValue();
	    overBeginTime = overTimeRange.getBeginTime().getMicroSecondTime();
	    overTimeInterval = overTimeRange.getEndTime().getMicroSecondTime() - overBeginTime;
	    overSizedImage = new SoftReference(newImage); 
	    repaint();	
 	}

	public TimeRangeConfig getTimeConfig(){ return timeConfig; }
	
	protected long overEndTime, overBeginTime;

	protected long overTimeInterval;

	protected double displayTime;
	
	protected MicroSecondTimeRange overTimeRange;
	
	protected TimeInterval displayInterval;
    
	protected SoftReference overSizedImage;

    }
}// BasicSeismogramDisplay
