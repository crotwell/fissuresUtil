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
	this(seis, tr, ar, timeBorder, "");
    }

    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder, String name){
	this(seis, tr, ar, timeBorder, name, null);
    }

    public BasicSeismogramDisplay(LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar, boolean timeBorder, String name, 
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
	this.addSeismogram(seis);
	this.parentFilters = parent.getCurrentFilters();
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
     */
    public void addSeismogram(LocalSeismogram newSeismogram){
	SeismogramPlotter newPlotter = new SeismogramPlotter(newSeismogram, timeConfig, ampConfig);
	Iterator e = parentFilters.iterator();
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
     */
    public void removeSeismogram(LocalSeismogram oldSeis){}

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
	this.revalidate();
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
	this.revalidate();
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

    public void stopImageCreation(){
	synchronized(imageMaker){ imageMaker.remove(imagePainter); }
    }

    public void turnOnToolTip(){
	this.setToolTipText(name);
    }

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
		g2.setPaint(new Color(0, 0, 0, 64));
		g2.drawString(name, 5, 10);
	    }
	}
	
	public synchronized void createImage(){
	    imageMaker.createImage(this, new PlotInfo(overSize, plotters, displayInterval));
	}

	public synchronized void setImage(Image newImage){
	    overTimeRange = timeConfig.getTimeRange().getOversizedTimeRange(OVERSIZED_SCALE);
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

    protected VerticalSeismogramDisplay parent; 
    
    protected Selection currentSelection, previousSelection; 
    
    protected LinkedList selections = new LinkedList();
    
    protected LinkedList filters = new LinkedList();

    protected LinkedList parentFilters = new LinkedList();

    protected static ImageMaker imageMaker = new ImageMaker();

    protected Dimension overSize;

    protected HashMap plotters = new HashMap();
    
    protected AmpRangeConfig ampConfig;
    
    protected TimeRangeConfig timeConfig;

    protected ScaleBorder scaleBorder;

    protected TimeScaleCalc timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    private Color[] colors = { Color.blue, Color.red, Color.yellow, Color.green, Color.black };

    private Color[] transparentColors = { new Color(255, 0, 0, 64), new Color(255, 255, 0, 64), new Color(0, 255, 0, 64), 
					  new Color(0, 0, 255, 64)};
    
    static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());

    protected ImagePainter imagePainter;

    protected boolean redo;

    protected String name;

    public static final int OVERSIZED_SCALE = 3;
}// BasicSeismogramDisplay
