package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.sc.seis.fissuresUtil.freq.ButterworthFilter;
import java.util.LinkedList;
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

public class BasicSeismogramDisplay extends JComponent implements SeismogramDisplay, GlobalToolbarActions{
    
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param timeBorder a <code>boolean</code> value
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), new RMeanAmpConfig(), timeBorder);
    }
   
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param tr a <code>TimeRangeConfig</code> value
     * @param timeBorder a <code>boolean</code> value
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, boolean timeBorder){
	this(seis, tr, new RMeanAmpConfig(), timeBorder);
    }
    
    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param ar an <code>AmpRangeConfig</code> value
     * @param timeBorder a <code>boolean</code> value
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, AmpRangeConfig ar, boolean timeBorder){
	this(seis, new BoundedTimeConfig(), ar, timeBorder);
    }

    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param tr a <code>TimeRangeConfig</code> value
     * @param ar an <code>AmpRangeConfig</code> value
     * @param timeBorder a <code>boolean</code> value
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder){
	this(seis, tr, ar, timeBorder, "");
    }

    /**
     * Creates a new <code>BasicSeismogramDisplay</code> instance.
     *
     * @param seis a <code>LocalSeismogram</code> value
     * @param tr a <code>TimeRangeConfig</code> value
     * @param ar an <code>AmpRangeConfig</code> value
     * @param timeBorder a <code>boolean</code> value
     * @param name a <code>String</code> value
     */
    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder, String name){
	this(seis, tr, ar, timeBorder, name, null);
    }

   BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder, String name, 
				  VerticalSeismogramDisplay parent){
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
	this.parent = parent;
	this.name = name;
	this.timeConfig = tr;
	this.ampConfig = ar;
	this.filters = parent.getCurrentFilters();
	this.addSeismogram(seis);
	scaleBorder = new ScaleBorder();
	if(timeBorder)
	    scaleBorder.setBottomScaleMapper(timeScaleMap);
	scaleBorder.setLeftScaleMapper(ampScaleMap);        
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
											new LeftTitleBorder("")),
						     BorderFactory.createCompoundBorder(scaleBorder,
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
     * @param newSeismogram a <code>LocalSeismogram</code> value
     */
    public void addSeismogram(LocalSeismogram newSeismogram){
	SeismogramPlotter newPlotter = new SeismogramPlotter(newSeismogram, timeConfig, ampConfig);
	Iterator e = filters.iterator();
	plotters.put(newPlotter, colors[plotters.size()%colors.length]);
	while(e.hasNext()){
	    plotters.put(new FilteredSeismogramPlotter(((ButterworthFilter)e.next()), newSeismogram, timeConfig, ampConfig), 
			 colors[plotters.size()%colors.length]);
	}
	timeConfig.addSeismogram(newSeismogram);
	ampConfig.addSeismogram(newSeismogram);
	redo = true;
    }
    
    /**
     * Removes a seismogram from the display
     *
     *
     * @param oldSeis a <code>LocalSeismogram</code> value
     */
    public void removeSeismogram(LocalSeismogram oldSeis){}

    /**
     * Describe <code>remove</code> method here.
     *
     * @param me a <code>MouseEvent</code> value
     */
    public void remove(MouseEvent me){
       logger.debug(name + " being removed");
       Iterator e = plotters.keySet().iterator();
       while(e.hasNext()){
	   LocalSeismogram current = ((SeismogramPlotter)e.next()).getSeismogram();
	   timeConfig.removeSeismogram(current);
	   ampConfig.removeSeismogram(current);
       }
       this.stopImageCreation();
       timeConfig.removeTimeSyncListener(this);
       ampConfig.removeAmpSyncListener(this);
       parent.removeDisplay(this);
   }

    /**
     * Returns the amp range configurator the display is using
     *
     *
     * @return an <code>AmpRangeConfig</code> value
     */
    public AmpRangeConfig getAmpConfig(){ return ampConfig; }
    
    /**
     * Describe <code>updateAmpRange</code> method here.
     *
     */
    public void updateAmpRange(){
	redo = true;
	this.ampScaleMap.setUnitRange(ampConfig.getAmpRange());
	repaint();
    }

    /**
     * Returns the time range configurator the display is using
     *
     *
     * @return a <code>TimeRangeConfig</code> value
     */
    public TimeRangeConfig getTimeConfig(){ return timeConfig; }
    
    /**
     * Describe <code>updateTimeRange</code> method here.
     *
     */
    public void updateTimeRange(){
	this.timeScaleMap.setTimes(timeConfig.getTimeRange().getBeginTime(), 
				   timeConfig.getTimeRange().getEndTime());
	repaint();
	
    }

    /**
     * Describe <code>addBottomTimeBorder</code> method here.
     *
     */
    public void addBottomTimeBorder(){	
	scaleBorder.setBottomScaleMapper(timeScaleMap); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
	this.revalidate();
    }

    /**
     * Describe <code>removeBottomTimeBorder</code> method here.
     *
     */
    public void removeBottomTimeBorder(){ 
	scaleBorder.clearBottomScaleMapper(); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }


    /**
     * Describe <code>addTopTimeBorder</code> method here.
     *
     */
    public void addTopTimeBorder(){ 
	scaleBorder.setTopScaleMapper(timeScaleMap);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
	this.revalidate();
    }

    /**
     * Describe <code>removeTopTimeBorder</code> method here.
     *
     */
    public void removeTopTimeBorder(){ 
	scaleBorder.clearTopScaleMapper();
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    /**
     * Describe <code>redraw</code> method here.
     *
     */
    public void redraw(){
	redo = true;
	repaint();
    }

    /**
     * Describe <code>resize</code> method here.
     *
     */
    protected void resize() {
	Insets insets = getInsets();
	synchronized(imagePainter){
	    Dimension d = getSize();
	    int w = (d.width - insets.left - insets.right) * 5, h = d.height - insets.top - insets.bottom;
	    overSize = new Dimension(w, h);
	    timeScaleMap.setTotalPixels(d.width-insets.left-insets.right);
	    ampScaleMap.setTotalPixels(d.height-insets.top-insets.bottom);
	}
	redo = true;
	repaint();
    }

    /**
     * Describe <code>stopImageCreation</code> method here.
     *
     */
    public void stopImageCreation(){
	synchronized(imageMaker){ imageMaker.remove(imagePainter); }
    }

    /**
     * Describe <code>turnOnToolTip</code> method here.
     *
     */
    public void turnOnToolTip(){
	this.setToolTipText(name);
    }

    /**
     * Describe <code>turnOffToolTip</code> method here.
     *
     */
    public void turnOffToolTip(){
	this.setToolTipText("");
    }

    public String getName(){ return name; }
    
    public void setName(String name){ this.name = name; } 

    public void selectRegion(MouseEvent one, MouseEvent two){
	Insets insets = this.getInsets();
	Dimension dim = getSize();
	double x1percent, x2percent;
	if(one.getX() < two.getX()){
	    x1percent = (one.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	    x2percent = (two.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	}else{
	    x2percent = (one.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	    x1percent = (two.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	}
	MicroSecondDate current = timeConfig.getTimeRange().getBeginTime();
	MicroSecondDate selectionBegin = new MicroSecondDate((long)(imagePainter.displayInterval.getValue() * x1percent + 
								    current.getMicroSecondTime()));
	MicroSecondDate selectionEnd = new MicroSecondDate((long)(imagePainter.displayInterval.getValue() * x2percent + 
								  current.getMicroSecondTime()));
	if(currentSelection == null){
	    Iterator e = selections.iterator();
	    while(e.hasNext()){
		Selection curr = ((Selection)e.next());
		if(curr.borders(selectionBegin, selectionEnd)){
		    currentSelection = curr;
		    currentSelection.adjustRange(selectionBegin, selectionEnd);		
		    return;
		}
	    }
	    currentSelection = new Selection(selectionBegin, selectionEnd, timeConfig, plotters, this, 
					     transparentColors[selections.size()%transparentColors.length]);
	    selections.add(currentSelection);
	    return;
	}
	currentSelection.adjustRange(selectionBegin, selectionEnd);		
    }

    public Selection releaseCurrentSelection(){
	if(currentSelection != null && currentSelection.remove()){
	    selections.remove(currentSelection);
	    repaint();
	    previousSelection = currentSelection;
	    currentSelection = null;
	    return previousSelection;
	}
	currentSelection = null;
	return null;
    }

    public void zoomIn(MouseEvent me) {
	Insets insets = this.getInsets();
	Dimension dim = this.getSize();
	if (me.getX() < insets.left ||
	    me.getX() > dim.width-insets.right ||
	    me.getY() < insets.top ||
	    me.getY() > dim.height-insets.bottom) {
	    return;
	}
	int x = me.getX()-insets.left;
	int center = (dim.width-insets.left-insets.right)/2;
	float centerPercent = (x - center)/2/(float)center;
	timeConfig.fireTimeRangeEvent(new TimeSyncEvent(.25 + centerPercent, -.25 + centerPercent, false));
    }

    public void zoomIn(MouseEvent begin, MouseEvent end){}

    public void zoomOut(MouseEvent me){
	Insets insets = this.getInsets();
	Dimension dim = this.getSize();
	if (me.getX() < insets.left ||
	    me.getX() > dim.width-insets.right ||
	    me.getY() < insets.top ||
	    me.getY() > dim.height-insets.bottom) {
	    return;
	} 
	int x = me.getX()-insets.left;
	int center = (dim.width-insets.left-insets.right)/2;
	float centerPercent = (x - center)/2/(float)center;
	timeConfig.fireTimeRangeEvent(new TimeSyncEvent(-.25 + centerPercent, .25 + centerPercent, false));
    }

    public void drag(MouseEvent meone, MouseEvent metwo) {
	parent.stopImageCreation();
	if(meone == null) return;
	Dimension dim = this.getSize();
	double xDiff = -(metwo.getX() - meone.getX())/(double)(dim.width);// - (insets.right+insets.left));
	timeConfig.fireTimeRangeEvent(new TimeSyncEvent(xDiff, xDiff, false));
    }

    public void mouseReleased(MouseEvent me){
	parent.redraw();
    }

    public void mouseMoved(MouseEvent me){
	Insets insets = this.getInsets();
	Dimension dim = this.getSize();
	/*BasicSeismogramDisplay selected = ((BasicSeismogramDisplay)me.getComponent());
	if(last == null)
	    last = selected;
	    if (me.getX() < insets.left){	    
	    last.turnOffToolTip();
	    selected.turnOnToolTip();
	    last = selected;
	    return;
	} 
	if(me.getX() > dim.width-insets.right ||
	   me.getY() < insets.top ||
	   me.getY() > dim.height-insets.bottom) {
	    return;
	}
	last.turnOffToolTip();*/
	    double xPercent = (me.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	MicroSecondTimeRange currRange = timeConfig.getTimeRange();
	MicroSecondDate time = new MicroSecondDate((long)(currRange.getBeginTime().getMicroSecondTime() + 
							  currRange.getInterval().getValue() * xPercent));	
	double amp;
	UnitRangeImpl current = ampConfig.getAmpRange();
	if(current == null)
	    amp = 0;
	else{
	    double yPercent = (dim.getHeight() - (me.getY() + insets.bottom))/(double)(dim.getHeight() - insets.top - insets.bottom);
	    amp = (current.getMaxValue() - current.getMinValue()) * yPercent + current.getMinValue();
	}
	parent.setLabels(time, amp);
    }	

    public Selection getCurrentSelection(){ return currentSelection; }
    
    public void toggleUnfilteredDisplay(){
	synchronized(imageMaker){
	    Iterator e = plotters.keySet().iterator();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		if(current instanceof SeismogramPlotter)
		    current.toggleVisibility();
	    }
	}
	redo = true;
	repaint();
    }

    public void setFilter(ButterworthFilter filter, boolean visible){
	LinkedList seismos = new LinkedList();
	LinkedList filteredSeismos = new LinkedList();
	LinkedList filteredPlotters = new LinkedList();
	boolean changingExisting = false;
	synchronized(imageMaker){
	    if(filters.contains(filter)){
		changingExisting = true;
	    }else{
		filters.add(filter);
	    }
	    Iterator e = plotters.keySet().iterator();
	    while(e.hasNext()){
		Plotter current = ((Plotter)e.next());
		if(current instanceof SeismogramPlotter){
		    seismos.add(((SeismogramPlotter)current).getSeismogram());
		}else if(changingExisting && current instanceof FilteredSeismogramPlotter  && 
			 ((FilteredSeismogramPlotter)current).getFilter() == filter){
		    filteredSeismos.add(((FilteredSeismogramPlotter)current).getUnfilteredSeismogram());
		    filteredPlotters.add(current);
		}
	    }
	    e = seismos.iterator();
	    while(e.hasNext()){
		LocalSeismogramImpl current = ((LocalSeismogramImpl)e.next());
		if(changingExisting && filteredSeismos.contains(current)){
		    ((FilteredSeismogramPlotter)filteredPlotters.get(filteredSeismos.indexOf(current))).setVisibility(visible);
		}else{
		    logger.debug("creating a new filter for " + name);
		    FilteredSeismogramPlotter filteredPlotter = new FilteredSeismogramPlotter(filter, (LocalSeismogram)current,
											      timeConfig, ampConfig);
		    filteredPlotter.setVisibility(visible);
		    plotters.put(filteredPlotter, colors[plotters.size()%colors.length]);
		}
	    }
	}
	redo = true;
	repaint();
    }
    
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
		double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * overSize.getWidth();
		if(imageCache.contains(overSizedImage.get())){
		    imageCache.addFirst(overSizedImage.get());
		    imageCache.remove(overSizedImage.get());
		}
		if(ImageMaker.bufferedImage)
		    g2.drawImage(((BufferedImage)overSizedImage.get()), AffineTransform.getTranslateInstance(-offset, 0.0), null);
		else
		    g2.drawImage(((Image)overSizedImage.get()), AffineTransform.getTranslateInstance(-offset, 0.0), null);
		if(redo){
		    logger.debug("the image is being redone");
		    this.createImage();
		}
		redo = false;
	    } else{
		double scale = displayTime/timeConfig.getTimeRange().getInterval().getValue(); 
		double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * (overSize.getWidth() * scale);
		AffineTransform tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		tx.scale(scale, 1);
		if(imageCache.contains(overSizedImage.get())){
		    imageCache.addFirst(overSizedImage.get());
		    imageCache.remove(overSizedImage.get());
		}
		if(ImageMaker.bufferedImage)
		    g2.drawImage(((BufferedImage)overSizedImage.get()), tx, null);
		else
		    g2.drawImage(((Image)overSizedImage.get()), tx, null);
		synchronized(this){ displayInterval = timeConfig.getTimeRange().getInterval();	}
		this.createImage();
	    }
	    if(selections.size() > 0){
		Iterator e = selections.iterator();
		int i = 0;
		while(e.hasNext()){
		    Selection currentSelection = (Selection)(e.next());
		    
		    if(currentSelection.isVisible()){
			Rectangle2D current = new Rectangle2D.Float(currentSelection.getX(getSize().width), 0, 
								    (float)(currentSelection.getWidth() * getSize().width), 
								    getSize().height);
			g2.setPaint(currentSelection.getColor());
			g2.fill(current);
			g2.draw(current);
			
		    } 
		    i++;
		}
	    }
	    if(name != null){
		g2.setPaint(new Color(0, 0, 0, 128));
		g2.drawString(name, 5, 10);
	    }
	    logger.debug(imageCache.size() + " images in the cache");
	    //System.out.println(ImageMaker.getImageSize(((BufferedImage)overSizedImage.get())));
	}
	
	public synchronized void createImage(){
	    imageMaker.createImage(this, new PlotInfo(overSize, plotters, displayInterval));
	}

	public synchronized void setImage(BufferedImage newImage){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(OVERSIZED_SCALE);
	    displayTime = displayInterval.getValue();
	    overBeginTime = overTimeRange.getBeginTime().getMicroSecondTime();
	    overTimeInterval = overTimeRange.getEndTime().getMicroSecondTime() - overBeginTime;
	    if(overSizedImage != null && imageCache.contains(overSizedImage.get())){
		imageCache.remove(overSizedImage.get());
	    }
	    imageCache.addFirst(newImage);
	    overSizedImage = new SoftReference(newImage);
	    if(imageCache.size() > 5)
		imageCache.removeLast();
	    repaint();	
 	}

	public synchronized void setImage(Image newImage){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(OVERSIZED_SCALE);
	    displayTime = displayInterval.getValue();
	    overBeginTime = overTimeRange.getBeginTime().getMicroSecondTime();
	    overTimeInterval = overTimeRange.getEndTime().getMicroSecondTime() - overBeginTime;
	    if(overSizedImage != null && imageCache.contains(overSizedImage.get())){
		imageCache.remove(overSizedImage.get());
	    }
	    imageCache.addFirst(newImage);
	    overSizedImage = new SoftReference(newImage);
	    if(imageCache.size() > 5)
		imageCache.removeLast();
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
    protected static LinkedList imageCache = new LinkedList();
    
    protected VerticalSeismogramDisplay parent; 
    
    protected Selection currentSelection, previousSelection; 
    
    protected LinkedList selections = new LinkedList();
    
    protected LinkedList filters = new LinkedList();

    protected HashMap plotters = new HashMap();
    
    protected String name;

    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig;

    protected ScaleBorder scaleBorder;

    protected TimeScaleCalc timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    protected ImagePainter imagePainter;

    protected boolean redo;

    protected Dimension overSize;

    public static final int OVERSIZED_SCALE = 3;

    protected static ImageMaker imageMaker = new ImageMaker();

    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };

    private Color[] transparentColors = { new Color(255, 0, 0, 64), new Color(255, 255, 0, 64), new Color(0, 255, 0, 64), 
					  new Color(0, 0, 255, 64)};

    static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
