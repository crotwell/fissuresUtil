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

public class BasicSeismogramDisplay extends JComponent implements GlobalToolbarActions, TimeSyncListener, AmpSyncListener{
 
    public BasicSeismogramDisplay(DataSetSeismogram seis, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(this);
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(), this);
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeRangeConfig tr, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(tr, this);
	ampRegistrar = new AmpConfigRegistrar(new RMeanAmpConfig(), this);
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, AmpConfigRegistrar ar, String name, VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(this);
	ampRegistrar = new AmpConfigRegistrar(ar, this);
	initializeDisplay(seis, name, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram seis, TimeRangeConfig tr, AmpConfigRegistrar ar, String name, 
				  VerticalSeismogramDisplay parent){
	timeRegistrar = new TimeConfigRegistrar(tr, this);
	ampRegistrar = new AmpConfigRegistrar(ar, this);
	initializeDisplay(seis, name, parent);
    }
    
    public void initializeDisplay(DataSetSeismogram seis, String name, VerticalSeismogramDisplay parent){
	this.name = name;
	this.parent = parent;
	plotPainter = new PlotPainter();
	add(plotPainter);
	ampRegistrar.visibleAmpCalc(timeRegistrar);
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
	
	timeScaleMap = new TimeScaleCalc(200, timeRegistrar);
	ampScaleMap = new AmpScaleMapper(50, 4, (AmpRangeConfig)ampRegistrar);
	setMinimumSize(new Dimension(100, 50));
	scaleBorder = new ScaleBorder();
	scaleBorder.setLeftScaleMapper(ampScaleMap);        
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
											new LeftTitleBorder("")),
						     BorderFactory.createCompoundBorder(scaleBorder,
											BorderFactory.createLoweredBevelBorder())));
	Dimension d = getSize();
	Insets insets = this.getInsets();
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(200 + current.left, 100 + current.top + current.bottom));
    }

    public void addSeismogram(DataSetSeismogram newSeismogram){
	seismos.add(newSeismogram);	
	SeismogramShape newPlotter;
	if (autoColor) {
	     newPlotter = new SeismogramShape(newSeismogram, seisColors[seisCount%seisColors.length]);
	}else {
	      newPlotter = new SeismogramShape(newSeismogram, Color.blue);
	} // end of else
	if(parent != null)
	    newPlotter.setVisibility(parent.getOriginalVisibility());
	plotters.add(seisCount, newPlotter);
	seisCount++;
	timeRegistrar.addSeismogram(newSeismogram);
	ampRegistrar.addSeismogram(newSeismogram);
	Iterator e = globalFilters.iterator();
	while(e.hasNext()){
	    applyFilter((ColoredFilter)e.next());
	}
	timeRegistrar.addSeismogram(newSeismogram);
	ampRegistrar.addSeismogram(newSeismogram);
    }
    
    public void addFlags(Arrival[] arrivals) {
	try{
	    MicroSecondDate originTime = new MicroSecondDate(((XMLDataSet)((DataSetSeismogram)seismos.getFirst()).getDataSet()).
							     getEvent().get_preferred_origin().origin_time);
	
	    for(int i = 0; i < arrivals.length; i++){
		FlagPlotter current = new FlagPlotter(new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) + 
									  originTime.getMicroSecondTime()), 
						      arrivals[i].getPhase().getName());
		plotters.addLast(current);
		flagCount++;
	    }
	}catch(Exception e){}
	repaint();
    }

    public void removeAllFlags(){
	plotters.subList(filterCount + seisCount, plotters.size()).clear();
	flagCount = 0;
	repaint();
    }
    public LinkedList getSeismograms(){ return seismos; }
  
    public void removeSeismogram(DataSetSeismogram oldSeis){}

    public String getName(){ return name; }
    
    public void setName(String name){ this.name = name; } 

    public AmpConfigRegistrar getAmpRegistrar(){ return ampRegistrar; } 

    public void updateAmpRange(){
	repaint();
    }

    public TimeConfigRegistrar getTimeConfig(){ return timeRegistrar; }

    public void setTimeConfig(TimeRangeConfig tc){ timeRegistrar.setTimeConfig(tc); }
    
    public void updateTimeRange(){
	repaint();
    }

    public VerticalSeismogramDisplay getVerticalParent(){ return parent; } 
    
    public LinkedList getAllSelections(){ 
	return (LinkedList)plotters.subList(plotters.size() - selectionCount - selection3CCount, plotters.size()); 
    }
    
    public LinkedList getSelections(){ 
	return (LinkedList)plotters.subList(plotters.size() - selectionCount - selection3CCount, plotters.size()); 
    }

    public void addSelection(Selection newSelection){ 
	if(!getSelections().contains(newSelection))
	    getSelections().add(newSelection);
	repaint();
    }
    
    public void removeSelection(Selection oldSelection){ 
	getSelections().remove(oldSelection); 
	repaint();
    }

    public LinkedList get3CSelections(){ 
	return (LinkedList)plotters.subList(plotters.size() - selection3CCount, plotters.size());
    }

    public void add3CSelection(Selection newSelection){ 
	if(!get3CSelections().contains(newSelection))
	    get3CSelections().add(newSelection);
	repaint();
    }
    
    public void remove3CSelection(Selection oldSelection){ 
	get3CSelections().remove(oldSelection); 
	repaint();
    }

    public Dimension getDisplaySize(){ return displaySize; }

    public TimeInterval getDisplayInterval(){ 
	return new TimeInterval(timeRegistrar.getTimeRange().getBeginTime(), 
				timeRegistrar.getTimeRange().getEndTime());
    }

    public static Set getGlobalFilters(){ return globalFilters; }

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
	repaint();
    }

    protected void resize() {
	Insets insets = getInsets();
	Dimension d = getSize();
	displaySize = new Dimension(d.width - insets.left - insets.right, d.height - insets.top - insets.bottom);
	timeScaleMap.setTotalPixels(d.width-insets.left-insets.right);
	ampScaleMap.setTotalPixels(d.height-insets.top-insets.bottom);
    	repaint();
    }

    public void stopImageCreation(){
	synchronized(plotMaker){ plotMaker.remove(plotPainter); }
    }

    public void clearSelections(){
	parent.removeSelectionDisplay();
	parent.remove3CSelectionDisplay();
	repaint();
    }
    
    public void clearRegSelections(){
	getSelections().clear();
	repaint();
    }

    public void clear3CSelections(){
	get3CSelections().clear();
	repaint();
    }

    public void remove(){
       logger.debug(name + " being removed");
       this.stopImageCreation();
       parent.removeDisplay(this);
       clearSelections();
       timeRegistrar.removeTimeSyncListener(this);
       ampRegistrar.removeAmpSyncListener(this); 
       Iterator e = seismos.iterator();
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
	Iterator e = plotters.subList(0, seisCount).iterator();
	while(e.hasNext()){
	    SeismogramShape current = (SeismogramShape)e.next();
	    current.setVisibility(visible);
	    if (visible) {
		ampRegistrar.addSeismogram(current.getSeismogram());
	    }else {
		ampRegistrar.removeSeismogram(current.getSeismogram());
	    } // end of else
	} // end of if (visible)
 	repaint();
    }

    public void applyFilter(ColoredFilter filter){
	System.out.println("applying filter");
	if(filters.contains(filter)){
	    Iterator e = plotters.subList(seisCount, seisCount + filterCount).iterator();
	    while(e.hasNext()){
		FilteredSeismogramShape current = ((FilteredSeismogramShape)e.next());
		if(current.getFilter() == filter){
		    current.setVisibility(current.getFilter().getVisibility());
		    if (current.getVisibility()) {
			ampRegistrar.addSeismogram(current.getFilteredSeismogram());
		    } // end of if (current.getVisibility())
		    else {
			ampRegistrar.removeSeismogram(current.getFilteredSeismogram());
		    } // end of else
		    
		}
	    }
	}else{
	    filters.add(filter);
	    Iterator e = seismos.iterator();
	    while(e.hasNext()){
		DataSetSeismogram current = (DataSetSeismogram)e.next();
		//logger.debug
		System.out.println("creating a new filter for " + name);
		FilteredSeismogramShape filteredShape = new FilteredSeismogramShape(filter, current);
		filteredShape.setVisibility(true);
		plotters.add(filterCount + seisCount, filteredShape);
		filterCount++;
		ampRegistrar.addSeismogram(filteredShape.getFilteredSeismogram());
	    }
	}
	repaint();
    }
    
    public void ampFillWindow(){
	ampRegistrar.individualizeAmpConfig(timeRegistrar);
    }

    public void createParticleDisplay(MouseEvent me, boolean advancedOption){
	parent.createParticleDisplay(this, advancedOption);
    }

    protected class PlotPainter extends JComponent{
	public void paint(Graphics g){
	    Date begin = new Date();
	    Graphics2D g2 = (Graphics2D)g;
	    Dimension size = getSize();
	    TimeSnapshot timeState = timeRegistrar.takeSnapshot();
	    AmpSnapshot	ampState = ampRegistrar.takeSnapshot();
	    Iterator e = plotters.iterator();
	    Date plotBegin = new Date();
	    while(e.hasNext()){
		((Plotter)e.next()).draw(g2, size, timeState, ampState);
	    }
	    Date plotEnd = new Date();
	    if(name != null){
		g2.setPaint(new Color(0, 0, 0, 128));
		g2.drawString(name, 5, getSize().height - 3);
	    }
	    Date end = new Date();
	    count++;
	    nameTime += end.getTime() - plotEnd.getTime();
	    paintTime += end.getTime() - begin.getTime();
	    plotTime += plotEnd.getTime() - plotBegin.getTime();
	    //System.out.println("total paint time: " + (paintTime/count) + 
	    //	       " plot time: " + (plotTime/count) + 
	    //	       " name time: " + (nameTime/count));	    
	}
    }
    protected long nameTime, plotTime, paintTime, count;
    
    protected static Set globalFilters = new HashSet();

    public ArrayList filters = new ArrayList();

    protected Dimension displaySize;

    protected VerticalSeismogramDisplay parent; 
    
    protected LinkedList seismos = new LinkedList();
    
    protected LinkedList plotters = new LinkedList();
    
    protected int seisCount = 0, filterCount = 0, flagCount = 0, selectionCount = 0, selection3CCount = 0;

    protected String name;

    protected AmpConfigRegistrar ampRegistrar;
    
    protected TimeConfigRegistrar timeRegistrar;

    protected ScaleBorder scaleBorder;

    protected TimeScaleCalc timeScaleMap;
    
    protected AmpScaleMapper ampScaleMap;
   
    protected PlotPainter plotPainter;

    protected boolean autoColor = true;

    public static final int OVERSIZED_SCALE = 3;

    protected static PlotMaker plotMaker = PlotMaker.getPlotMaker();

    private static Color[] seisColors = { Color.blue, Color.red,  Color.gray, Color.magenta, Color.cyan };

    private static Color[] selectionColors = { new NamedColor(255, 0, 0, 64, "red"),  
					       new NamedColor(255, 255, 0, 64, "yellow"), 
					       new NamedColor(0, 255, 0, 64, "green"),  
					       new NamedColor(0, 0, 255, 64, "blue")};

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
