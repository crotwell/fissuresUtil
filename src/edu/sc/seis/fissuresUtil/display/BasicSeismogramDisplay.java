package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import edu.sc.seis.TauP.Arrival;
import java.util.*;
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
	timeRegistrar = new TimeConfigRegistrar();
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(ampRegistrar));
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeRangeConfig tr, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(tr);
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(ampRegistrar));
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, AmpConfigRegistrar ar, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar();
	ampRegistrar = new AmpConfigRegistrar(ar);
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeRangeConfig tr, AmpConfigRegistrar ar, String name, 
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
	ampRegistrar.visibleAmpCalc(timeRegistrar);
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
	imageSize = new Dimension(w, h);
	imagePainter = new ImagePainter();
	add(imagePainter);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void addSeismogram(DataSetSeismogram newSeismogram){
	seismos.add(newSeismogram);
	SeismogramPlotter newPlotter = new SeismogramPlotter(newSeismogram, ampRegistrar);
	if(parent != null)
	    newPlotter.setVisibility(parent.getOriginalVisibility());
	if(autoColor)
	    seisPlotters.put(newPlotter, seisColors[seisPlotters.size()%seisColors.length]);
	else
	    seisPlotters.put(newPlotter, Color.blue);
	Iterator e = filters.keySet().iterator();
	while(e.hasNext()){
	    ColoredFilter current =((ColoredFilter)e.next());
	    FilteredSeismogramPlotter currentPlotter = new FilteredSeismogramPlotter(current, newSeismogram, ampRegistrar);
	    currentPlotter.setVisibility(((Boolean)filters.get(current)).booleanValue());
	    filterPlotters.put(currentPlotter, current.getColor());
	}
	timeRegistrar.addSeismogram(newSeismogram);
	ampRegistrar.addSeismogram(newSeismogram);
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
					     arrivals[i].getPhase().getName()), Color.red);
	}
	}catch(Exception e){}
	redo = true;
	repaint();
    }

    public void removeAllFlags(){
	flagPlotters = new HashMap();
	redo = true;
	repaint();
    }
    public LinkedList getSeismograms(){ return seismos; }
  
    public void removeSeismogram(DataSetSeismogram oldSeis){}

    public String getName(){ return name; }
    
    public void setName(String name){ this.name = name; } 

    public AmpRangeConfig getAmpConfig(){ return ampRegistrar.getAmpConfig(); }
    
    public AmpConfigRegistrar getAmpRegistrar(){ return ampRegistrar; } 

    public void updateAmpRange(){
	ampScaleMap.setUnitRange(ampRegistrar.getAmpRange());
	redo = true;
	repaint();
    }

    public TimeConfigRegistrar getTimeConfig(){ return timeRegistrar; }

    public void setTimeConfig(TimeRangeConfig tc){ timeRegistrar.setTimeConfig(tc); }
    
    public void updateTimeRange(){
	this.timeScaleMap.setTimes(timeRegistrar.getTimeRange().getBeginTime(), 
				   timeRegistrar.getTimeRange().getEndTime());
	repaint();
    }

    public VerticalSeismogramDisplay getVerticalParent(){ return parent; } 
    
    public LinkedList getSelections(){ return selections; }

    public void addSelection(Selection newSelection){ 
	if(!selections.contains(newSelection))
	    selections.add(newSelection);
    }
    
    public void removeSelection(Selection oldSelection){ 
	selections.remove(oldSelection); 
	repaint();
    }

    public Dimension getDisplaySize(){ return displaySize; }

    public TimeInterval getDisplayInterval(){ return imagePainter.displayInterval; }

    public static HashMap getFilters(){ return filters; }

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
	    imageSize = new Dimension(w, h);
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

    public void clearSelections(){
	Iterator e = selections.iterator();
	while(e.hasNext()){
	    ((Selection)e.next()).remove();
	}
	selections.clear();
	repaint();
    }

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
	   DataSetSeismogram current = ((DataSetSeismogram)e.next());
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

    public void applyFilter(ColoredFilter filter){
	synchronized(imageMaker){
	    if(filters.containsKey(filter)){
		Iterator e = filterPlotters.keySet().iterator();
		while(e.hasNext()){
		FilteredSeismogramPlotter current = ((FilteredSeismogramPlotter)e.next());
		if(current.getFilter() == filter)
		    current.toggleVisibility();
		}
	    }else{
		Iterator e = seismos.iterator();
		while(e.hasNext()){
		    DataSetSeismogram current = (DataSetSeismogram)e.next();
		    logger.debug("creating a new filter for " + name);
		    FilteredSeismogramPlotter filteredPlotter = new FilteredSeismogramPlotter(filter, current,
											      ampRegistrar);
		    filteredPlotter.setVisibility(true);
		    filterPlotters.put(filteredPlotter, filter.getColor());
		}
	    }
	}
	redo = true;
	repaint();
    }

    public void setFilter(ColoredFilter filter, boolean visible){
	synchronized(imageMaker){
	    if(filters.containsKey(filter)){
		Iterator e = filterPlotters.keySet().iterator();
		while(e.hasNext()){
		FilteredSeismogramPlotter current = ((FilteredSeismogramPlotter)e.next());
		if(current.getFilter() == filter)
		    current.setVisibility(visible);
		}
	    }else{
		filters.put(filter, new Boolean(true));
		Iterator e = seismos.iterator();
		while(e.hasNext()){
		    DataSetSeismogram current = (DataSetSeismogram)e.next();
		    logger.debug("creating a new filter for " + name);
		    FilteredSeismogramPlotter filteredPlotter = new FilteredSeismogramPlotter(filter, current,
											      ampRegistrar);
		    filteredPlotter.setVisibility(visible);
		    filterPlotters.put(filteredPlotter, filter.getColor());
		}
	    }
	}
	redo = true;
	repaint();
    }

    public void ampFillWindow(){
	ampRegistrar.individualizeAmpConfig(timeRegistrar);
    }

    public void createParticleDisplay(MouseEvent me, boolean advancedOption){
	parent.createParticleDisplay(this, advancedOption);
    }

    protected class ImagePainter extends JComponent{
	public void paint(Graphics g){
	    Date begin = new Date();
	    if(image == null){
		//logger.debug("the image is null and is being created");
		synchronized(this){ displayInterval = timeRegistrar.getTimeRange().getInterval(); }
		this.createImage();
		return;
		}
	    if(image.get() == null){
		//logger.debug("image was garbage collected, and is being recreated");
		synchronized(this){ displayInterval = timeRegistrar.getTimeRange().getInterval(); }
		this.createImage();
		return;
	    }
	    long endTime = timeRegistrar.getTimeRange().getEndTime().getMicroSecondTime();
	    long beginTime = timeRegistrar.getTimeRange().getBeginTime().getMicroSecondTime();
	    Graphics2D g2 = (Graphics2D)g;
	    if(displayTime == timeRegistrar.getTimeRange().getInterval().getValue()){
		double offset = (beginTime - imageBeginTime)/ (double)(imageTimeInterval) * imageSize.getWidth();
		if(imageCache.contains(image.get())){
		    imageCache.remove(image.get());
		    imageCache.addFirst(image.get());
		}
		g2.drawImage(((Image)image.get()), AffineTransform.getTranslateInstance(-offset, 0.0), null);
		if(redo || endTime >= imageTimeRange.getEndTime().getMicroSecondTime() || 
		   beginTime <= imageTimeRange.getBeginTime().getMicroSecondTime()){
		    //logger.debug("the image is being redone");
		    this.createImage();
		}
		redo = false;
	    } else{
		double scale = displayTime/timeRegistrar.getTimeRange().getInterval().getValue(); 
		double offset = (beginTime - imageBeginTime)/ (double)(imageTimeInterval) * (imageSize.getWidth() * scale);
		AffineTransform tx = AffineTransform.getTranslateInstance(-offset, 0.0);
		tx.scale(scale, 1);
		if(imageCache.contains(image.get())){
		    imageCache.remove(image.get());
		    imageCache.addFirst(image.get());
		}
		g2.drawImage(((Image)image.get()), tx, null);
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
	    imageMaker.createImage(this, new PlotInfo(imageSize, seisPlotters, filterPlotters, flagPlotters, 
						      timeRegistrar.takeSnapshot()));
	}

	public synchronized void setImage(Image newImage, TimeSnapshot imageState){
	    imageTimeRange = imageState.getTimeRange().getOversizedTimeRange(OVERSIZED_SCALE);
	    displayTime = imageState.getTimeRange().getInterval().getValue();
	    imageBeginTime = imageTimeRange.getBeginTime().getMicroSecondTime();
	    imageTimeInterval = imageTimeRange.getEndTime().getMicroSecondTime() - imageBeginTime;
	    if(image != null && imageCache.contains(image.get())){
		imageCache.remove(image.get());
	    }
	    imageCache.addFirst(newImage);
	    image = new SoftReference(newImage);
	    if(imageCache.size() > 5)
		imageCache.removeLast();
	    repaint();	
 	}
	
	public TimeRangeConfig getTimeConfig(){ return timeRegistrar; }
	
	protected long imageEndTime, imageBeginTime;

	protected long imageTimeInterval;

	protected double displayTime;
	
	protected MicroSecondTimeRange imageTimeRange;
	
	protected TimeInterval displayInterval;
    
	protected SoftReference image;
    }
    protected static LinkedList imageCache = new LinkedList();
           
    protected Dimension displaySize;

    protected VerticalSeismogramDisplay parent; 
    
    protected Selection currentSelection; 
    
    protected LinkedList seismos = new LinkedList();
    
    protected LinkedList selections = new LinkedList();
    
    protected static HashMap filters = new HashMap();

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

    protected Dimension imageSize;

    public static final int OVERSIZED_SCALE = 3;

    protected static ImageMaker imageMaker = new ImageMaker();

    private Color[] seisColors = { Color.blue, Color.red,  Color.gray, Color.magenta, Color.cyan };

    private Color[] selectionColors = { new NamedColor(255, 0, 0, 64, "red"),  
					new NamedColor(255, 255, 0, 64, "yellow"), 
					new NamedColor(0, 255, 0, 64, "green"),  
					new NamedColor(0, 0, 255, 64, "blue")};

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
