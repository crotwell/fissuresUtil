package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.sc.seis.fissuresUtil.freq.ButterworthFilter;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import edu.sc.seis.TauP.Arrival;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.lang.ref.SoftReference;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import org.apache.log4j.*;
import java.awt.print.*;

/**
 * BasicSeismogramDisplay.java
 *
 *
 * Created: Thu Jun  6 09:52:51 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class BasicSeismogramDisplay extends JComponent implements GlobalToolbarActions, SeismogramDisplay{
 
    public BasicSeismogramDisplay(DataSetSeismogram seis, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(new BoundedTimeConfig(timeRegistrar));
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(ampRegistrar));
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeConfigRegistrar tr, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(tr);
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(ampRegistrar));
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, AmpConfigRegistrar ar, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(new BoundedTimeConfig(timeRegistrar));
	ampRegistrar = new AmpConfigRegistrar(ar);
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeConfigRegistrar tr, AmpConfigRegistrar ar, String name, 
				  VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(tr);
	ampRegistrar = new AmpConfigRegistrar(ar);
	initializeDisplay(seis, name, parent);
    }
    
    public void initializeDisplay(DataSetSeismogram seis, String name, VerticalSeismogramDisplay parent){
	this.name = name;
	this.parent = parent;
	timeRegistrar.addTimeSyncListener(this);
	ampRegistrar.addAmpSyncListener(this);
	addSeismogram(seis);
	setLayout(new OverlayLayout(this));
	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resize();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		}
	    });
	setMinimumSize(new Dimension(100, 50));
	scaleBorder = new ScaleBorder();
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
	add(imagePainter);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void addSeismogram(DataSetSeismogram newSeismogram){
	seismos.add(newSeismogram);
	SeismogramPlotter newPlotter = new SeismogramPlotter(newSeismogram.getSeismogram(), timeRegistrar, ampRegistrar);
	Iterator e = filters.iterator();
	if(autoColor)
	    seisPlotters.put(newPlotter, seisColors[seisPlotters.size()%seisColors.length]);
	else
	    seisPlotters.put(newPlotter, Color.blue);
	while(e.hasNext()){
	    filterPlotters.put(new FilteredSeismogramPlotter(((ButterworthFilter)e.next()), newSeismogram.getSeismogram(), 
							     timeRegistrar, ampRegistrar), 
			       filterColors[filterPlotters.size()%filterColors.length]);
	}
	timeRegistrar.addSeismogram(newSeismogram.getSeismogram());
	ampRegistrar.addSeismogram(newSeismogram.getSeismogram());
	redo = true;
    }

    public void addFlags(Arrival[] arrivals) {
	try{
	    MicroSecondDate originTime = new MicroSecondDate(((XMLDataSet)((DataSetSeismogram)seismos.getFirst()).getDataSet()).
							     getEvent().get_preferred_origin().origin_time);
	
	System.out.println((long)arrivals[0].getTime() + ", "
			   + arrivals[0].getPhase().getName() + " " + arrivals.length + " " + originTime);
	for(int i = 0; i < arrivals.length; i++){
	    flagPlotters.put(new FlagPlotter(new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) + 
								 originTime.getMicroSecondTime()), 
					     this.timeRegistrar, 
					     arrivals[i].getPhase().getName()), Color.red);
	}
	}catch(Exception e){}
	redo = true;
	repaint();
    }

    public void removeAllFlags(){
	flagPlotters = new HashMap();
    }
    public LinkedList getSeismograms(){ return seismos; }
  
    public void removeSeismogram(LocalSeismogram oldSeis){}

    public String getName(){ return name; }
    
    public void setName(String name){ this.name = name; } 

    public AmpRangeConfig getAmpConfig(){ return ampRegistrar.getAmpConfig(); }
    
    public AmpConfigRegistrar getAmpRegistrar(){ return ampRegistrar; } 

    public void updateAmpRange(){
	ampScaleMap.setUnitRange(ampRegistrar.getAmpRange());
	redo = true;
	repaint();
    }

    public TimeRangeConfig getTimeConfig(){ return timeRegistrar.getTimeConfig(); }
    
    public TimeConfigRegistrar getTimeRegistrar(){ return timeRegistrar; }

    public void updateTimeRange(){
	this.timeScaleMap.setTimes(timeRegistrar.getTimeRange().getBeginTime(), 
				   timeRegistrar.getTimeRange().getEndTime());
	repaint();
    }

    public VerticalSeismogramDisplay getVerticalParent(){ return parent; } 
    
    public LinkedList getSelections(){ return selections; }

    public void addSelection(Selection newSelection){ selections.add(newSelection); }
    
    public void removeSelection(Selection oldSelection){ selections.remove(oldSelection); }

    public Dimension getDisplaySize(){ return displaySize; }

    public TimeInterval getDisplayInterval(){ return imagePainter.displayInterval; }

    public LinkedList getFilters(){ return filters; }

    public void setAutoColor(boolean b){ autoColor = b; }

    public boolean getAutoColor(){ return autoColor; }

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
	    displaySize = new Dimension(d.width - insets.left - insets.right, d.height - insets.top - insets.bottom);
	    timeScaleMap.setTotalPixels(d.width-insets.left-insets.right);
	    ampScaleMap.setTotalPixels(d.height-insets.top-insets.bottom);
	}
	redo = true;
	repaint();
    }

    public void stopImageCreation(){
	synchronized(imageMaker){ imageMaker.remove(imagePainter); }
    }
    
    /*public void selectRegion(MouseEvent one, MouseEvent two){
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
	MicroSecondDate current = timeRegistrar.getTimeRange().getBeginTime();
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
	    currentSelection = new Selection(selectionBegin, selectionEnd, timeRegistrar, seismos, this, 
					     selectionColors[selections.size()%selectionColors.length]);
	    selections.add(currentSelection);
	    newSelection = true;
	    return;
	}
	currentSelection.adjustRange(selectionBegin, selectionEnd);		
    }

    public Selection getCurrentSelection(){ return currentSelection; }
    
    public void selectionReleased(MouseEvent me){
	if(currentSelection.isRemoveable()){
	    currentSelection.remove();
	    selections.remove(currentSelection);
	    repaint();
	}else if(newSelection == true){
	    parent.createSelectionDisplay(this);
	    newSelection = false;
	}
	currentSelection.release();
	currentSelection = null;
    }*/


    public void clearSelections(){
	Iterator e = selections.iterator();
	while(e.hasNext()){
	    ((Selection)e.next()).remove();
	}
	selections.clear();
	repaint();
    }
    	

    /**
     * Describe <code>remove</code> method here.
     *
     * @param me a <code>MouseEvent</code> value
     */
    public void remove(){
       logger.debug(name + " being removed");
       this.stopImageCreation();
       parent.removeDisplay(this);
       Iterator e = selections.iterator();
       while(e.hasNext()){
	   ((Selection)e.next()).remove();
       }
       timeRegistrar.removeTimeSyncListener(this);
       ampRegistrar.removeAmpSyncListener(this); 
       e = seismos.iterator();
       while(e.hasNext()){
	   LocalSeismogram current = ((DataSetSeismogram)e.next()).getSeismogram();
	   timeRegistrar.removeSeismogram(current);
	   ampRegistrar.removeSeismogram(current);
       }
    }

    public void removeAll(MouseEvent me){
	parent.removeAll();
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
	timeRegistrar.fireTimeRangeEvent(new TimeSyncEvent(.25 + centerPercent, -.25 + centerPercent, false));
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
	timeRegistrar.fireTimeRangeEvent(new TimeSyncEvent(-.25 + centerPercent, .25 + centerPercent, false));
    }

    public void drag(MouseEvent meone, MouseEvent metwo) {
	parent.stopImageCreation();
	if(meone == null) return;
	Dimension dim = this.getSize();
	double xDiff = -(metwo.getX() - meone.getX())/(double)(dim.width);// - (insets.right+insets.left));
	timeRegistrar.fireTimeRangeEvent(new TimeSyncEvent(xDiff, xDiff, false));
    }

    public void print(){
	PrinterJob pj = PrinterJob.getPrinterJob();
	pj.setPrintable(new ComponentPrintable(this));
	if(pj.printDialog()){
	    try { pj.print(); } 
	    catch(Exception e){ e.printStackTrace(); }
	}
    }

    public void mouseReleased(MouseEvent me){
	parent.redraw();
    }

    public void mouseMoved(MouseEvent me){
	Insets insets = this.getInsets();
	Dimension dim = this.getSize();
	double xPercent = (me.getX() - insets.left)/(double)(dim.getWidth() - insets.left - insets.right);
	MicroSecondTimeRange currRange = timeRegistrar.getTimeRange();
	MicroSecondDate time = new MicroSecondDate((long)(currRange.getBeginTime().getMicroSecondTime() + 
							  currRange.getInterval().getValue() * xPercent));	
	double amp;
	UnitRangeImpl current = ampRegistrar.getAmpRange();
	if(current == null)
	    amp = 0;
	else{
	    double yPercent = (dim.getHeight() - (me.getY() + insets.bottom))/(double)(dim.getHeight() - insets.top - insets.bottom);
	    amp = (current.getMaxValue() - current.getMinValue()) * yPercent + current.getMinValue();
	}
	parent.setLabels(time, amp);
    }

    public void setUnfilteredDisplay(boolean visible){
	synchronized(imageMaker){
	    Iterator e = seisPlotters.keySet().iterator();
	    while(e.hasNext()){
		((SeismogramPlotter)e.next()).setVisibility(visible);
	    }
	}
	redo = true;
	repaint();
    }

    public void applyFilter(ButterworthFilter filter){
	synchronized(imageMaker){
	    if(filters.contains(filter)){
		Iterator e = filterPlotters.keySet().iterator();
		while(e.hasNext()){
		FilteredSeismogramPlotter current = ((FilteredSeismogramPlotter)e.next());
		if(current.getFilter() == filter)
		    current.toggleVisibility();
		}
	    }else{
		Iterator e = seismos.iterator();
		while(e.hasNext()){
		    LocalSeismogram current = ((DataSetSeismogram)e.next()).getSeismogram();
		    logger.debug("creating a new filter for " + name);
		    FilteredSeismogramPlotter filteredPlotter = new FilteredSeismogramPlotter(filter, current,
											      timeRegistrar, ampRegistrar);
		    filteredPlotter.setVisibility(true);
		    filterPlotters.put(filteredPlotter, filterColors[filterPlotters.size()%filterColors.length]);
		}
	    }
	}
	redo = true;
	repaint();
    }

    public void setFilter(ButterworthFilter filter, boolean visible){
	synchronized(imageMaker){
	    if(filters.contains(filter)){
		Iterator e = filterPlotters.keySet().iterator();
		while(e.hasNext()){
		FilteredSeismogramPlotter current = ((FilteredSeismogramPlotter)e.next());
		if(current.getFilter() == filter)
		    current.setVisibility(visible);
		}
	    }else{
		filters.add(filter);
		Iterator e = seismos.iterator();
		while(e.hasNext()){
		    LocalSeismogram current = ((DataSetSeismogram)e.next()).getSeismogram();
		    logger.debug("creating a new filter for " + name);
		    FilteredSeismogramPlotter filteredPlotter = new FilteredSeismogramPlotter(filter, current,
											      timeRegistrar, ampRegistrar);
		    filteredPlotter.setVisibility(visible);
		    filterPlotters.put(filteredPlotter, filterColors[filterPlotters.size()%filterColors.length]);
		}
	    }
	}
	redo = true;
	repaint();
    }

    public void ampFillWindow(){
	ampRegistrar.individualizeAmpConfig(timeRegistrar);
    }

    public void createParticleDisplay(MouseEvent me){
	parent.createParticleDisplay(this);
    }

    protected class ImagePainter extends JComponent{
	public void paint(Graphics g){
	    Date begin = new Date();
	    if(overSizedImage == null){
		logger.debug("the image is null and is being created");
		synchronized(this){ displayInterval = timeRegistrar.getTimeRange().getInterval(); }
		this.createImage();
		return;
		}
	    if(overSizedImage.get() == null){
		logger.debug("image was garbage collected, and is being recreated");
		synchronized(this){ displayInterval = timeRegistrar.getTimeRange().getInterval(); }
		this.createImage();
		return;
	    }
	    long endTime = timeRegistrar.getTimeRange().getEndTime().getMicroSecondTime();
	    long beginTime = timeRegistrar.getTimeRange().getBeginTime().getMicroSecondTime();
	    Graphics2D g2 = (Graphics2D)g;
	    if(displayTime == timeRegistrar.getTimeRange().getInterval().getValue()){
		double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * overSize.getWidth();
		if(imageCache.contains(overSizedImage.get())){
		    imageCache.remove(overSizedImage.get());
		    imageCache.addFirst(overSizedImage.get());
		}
		g2.drawImage(((Image)overSizedImage.get()), AffineTransform.getTranslateInstance(-offset, 0.0), null);
		if(redo || endTime >= overTimeRange.getEndTime().getMicroSecondTime() || 
		   beginTime <= overTimeRange.getBeginTime().getMicroSecondTime()){
		    logger.debug("the image is being redone");
		    this.createImage();
		}
		redo = false;
	    } else{
		double scale = displayTime/timeRegistrar.getTimeRange().getInterval().getValue(); 
		double offset = (beginTime - overBeginTime)/ (double)(overTimeInterval) * (overSize.getWidth() * scale);
		AffineTransform tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		tx.scale(scale, 1);
		if(imageCache.contains(overSizedImage.get())){
		    imageCache.remove(overSizedImage.get());
		    imageCache.addFirst(overSizedImage.get());
		}
		g2.drawImage(((Image)overSizedImage.get()), tx, null);
		synchronized(this){ displayInterval = timeRegistrar.getTimeRange().getInterval();	}
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
		g2.drawString(name, 5, getSize().height - 3);
	    }
	    Date end = new Date();
	    //logger.debug("painting: " + (end.getTime() - begin.getTime()) + "ms");
	}
	
	public synchronized void createImage(){
	    imageMaker.createImage(this, new PlotInfo(overSize, seisPlotters, filterPlotters, flagPlotters, displayInterval));
	}

	public synchronized void setImage(Image newImage){
	    overTimeRange = timeRegistrar.getTimeRange().getOversizedTimeRange(OVERSIZED_SCALE);
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
	
	public TimeRangeConfig getTimeConfig(){ return timeRegistrar.getTimeConfig(); }
	
	protected long overEndTime, overBeginTime;

	protected long overTimeInterval;

	protected double displayTime;
	
	protected MicroSecondTimeRange overTimeRange;
	
	protected TimeInterval displayInterval;
    
	protected SoftReference overSizedImage;
    }
    protected static LinkedList imageCache = new LinkedList();
           
    protected Dimension displaySize;

    protected VerticalSeismogramDisplay parent; 
    
    protected Selection currentSelection; 
    
    protected LinkedList seismos = new LinkedList();
    
    protected LinkedList selections = new LinkedList();
    
    protected static LinkedList filters = new LinkedList();

    protected HashMap seisPlotters = new HashMap();
    
    protected HashMap filterPlotters = new HashMap();

    protected HashMap flagPlotters = new HashMap();

    protected String name;

    protected AmpConfigRegistrar ampRegistrar;
    
    protected TimeConfigRegistrar timeRegistrar;

    protected ScaleBorder scaleBorder;

    protected TimeScaleCalc timeScaleMap = new TimeScaleCalc(200, new MicroSecondDate(0), new MicroSecondDate(50000000));//placeholder
    
    protected AmpScaleMapper ampScaleMap = new AmpScaleMapper(50, 4, new UnitRangeImpl(0, 500, UnitImpl.COUNT));//placeholder
   
    protected ImagePainter imagePainter;

    protected boolean redo, newSelection;

    protected boolean autoColor = true;

    protected Dimension overSize;

    public static final int OVERSIZED_SCALE = 3;

    protected static ImageMaker imageMaker = new ImageMaker();

    private Color[] seisColors = { Color.blue, Color.red,  Color.gray, Color.magenta, Color.cyan };

    private Color[] filterColors = { Color.yellow, Color.green, Color.black, Color.darkGray, Color.orange };

    private Color[] selectionColors = { new NamedColor(255, 0, 0, 64, "red"),  
					new NamedColor(255, 255, 0, 64, "yellow"), 
					new NamedColor(0, 255, 0, 64, "green"),  
					new NamedColor(0, 0, 255, 64, "blue")};

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
