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

public class BasicSeismogramDisplay extends JComponent implements ConfigListener{
 
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, String[] names, 
				  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
	this(seismos, new BasicTimeConfig(seismos), new RMeanAmpConfig(seismos), names, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, TimeConfig tc, String[] names, 
				  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
	this(seismos, tc, new RMeanAmpConfig(seismos), names, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, AmpConfig ac, String[] names, 
				  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
	this(seismos, new BasicTimeConfig(seismos), ac, names, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, TimeConfig tc, AmpConfig ac, String[] names, 
				  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
	if(seismos.length == 0){
	    throw new IllegalArgumentException("The array of seismograms given to a basic seismogram display must not be of length 0.");
	}
	boolean allNull = true;
	for(int i = 0; i < seismos.length; i++){
	    if(seismos[i] != null){
		allNull = false;
	    }
	}
	if(allNull){
	    throw new IllegalArgumentException("A BasicSeismogramDisplay requires at least one non-null seismogram to initialize");
	}
	registrar = new Registrar(seismos, tc, ac);
	this.parent = parent;
	registrar.addListener(this);
	add(seismos, names);
	setLayout(new OverlayLayout(this));
	addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    resize();
		    repaint();
		}
		public void componentShown(ComponentEvent e) {
		    resize();
		    repaint();
		}
		});
	timeScaleMap = new TimeScaleCalc(preferredBSDWidth, registrar);
	ampScaleMap = new AmpScaleMapper(preferredBSDHeight, 4, registrar);
	scaleBorder = new ScaleBorder();
	scaleBorder.setLeftScaleMapper(ampScaleMap);        
	setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
											new LeftTitleBorder("")),
						     BorderFactory.createCompoundBorder(scaleBorder,
											BorderFactory.createLoweredBevelBorder())));
	Dimension d = getSize();
	Insets insets = getInsets();
	setPreferredSize(new Dimension(preferredBSDWidth + insets.left + insets.right, preferredBSDHeight + insets.top + insets.bottom));
	resize();
	repaint();
	plotPainter = new PlotPainter();
	add(plotPainter);
    }

    public void add(DataSetSeismogram[] seismos, String[] names){
	registrar.add(seismos);
	for(int i = 0; i < seismos.length; i++){
	    if(seismos[i] != null){
		seismograms.add(seismos[i]);	
		SeismogramShape newPlotter;
		if (autoColor) {
		    newPlotter = new SeismogramShape(seismos[i], seisColors[seisCount%seisColors.length], names[i]);
		}else {
		    newPlotter = new SeismogramShape(seismos[i], Color.blue, names[i]);
		} // end of else
		if(parent != null){
		    newPlotter.setVisibility(parent.getOriginalVisibility());
		}
		plotters.add(seisCount, newPlotter);
		seisCount++;
	    }
	}
	Iterator e = globalFilters.iterator();
	while(e.hasNext()){
	    applyFilter((ColoredFilter)e.next());
	}
	seismogramArray = null;
    }

    public DataSetSeismogram[] getSeismograms(){ 
	if(seismogramArray == null){
	    seismogramArray = (DataSetSeismogram[])seismograms.toArray(new DataSetSeismogram[seismograms.size()]); 
	}
	return seismogramArray;
    }

    public java.util.List getSeismogramList(){ return seismograms; }
  
    public void addFlags(Arrival[] arrivals) {
	try{
	    MicroSecondDate originTime = new MicroSecondDate(((XMLDataSet)((DataSetSeismogram)seismograms.getFirst()).getDataSet()).
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

    public void addCurrentTimeFlag(){
	plotters.addLast(new CurrentTimeFlagPlotter());
	flagCount++;
    }

    public void removeAllFlags(){
	plotters.subList(filterCount + seisCount, plotters.size()).clear();
	flagCount = 0;
	repaint();
    }

    public VerticalSeismogramDisplay getParentDisplay(){ return parent; }
    
    /**
     * Returns an array of names of seismograms in corresponding order with the array 
     * returned by getSeismograms
     */
    public String[] getNames(){ 
	if(seismogramNames == null || seismogramArray == null){
	    getSeismograms();
	    seismogramNames = new String[seismogramArray.length];
	    java.util.List seismogramPlotters = plotters.subList(0, seismogramArray.length);
	    for(int i = 0; i < seismogramArray.length; i++){
		Iterator e = seismogramPlotters.iterator();
		while(e.hasNext()){
		    SeismogramShape current = (SeismogramShape)e.next();
		    if(current.getSeismogram() == seismogramArray[i]){
			seismogramNames[i] = current.getName();
		    }
		}
	    }
	}
	return seismogramNames; 
    }
    
    public Registrar getRegistrar(){ return registrar; }

    public void updateAmp(AmpEvent event){
	update++;
	currentAmpEvent = event;
	repaint();
    }

    public void updateTime(TimeEvent event){
	update++;
	currentTimeEvent = event;
	repaint();
    }

    public MicroSecondTimeRange getTime(){
	return currentTimeEvent.getTime();
    }

    public void update(ConfigEvent event){
	update++;
	currentTimeEvent = event.getTimeEvent();
	currentAmpEvent = event.getAmpEvent();
	repaint();
    }

    private int update;

    public VerticalSeismogramDisplay getVerticalParent(){ return parent; } 
    
    public java.util.List getAllSelections(){ 
	return plotters.subList(plotters.size() - selectionCount - selection3CCount, plotters.size()); 
    }
    
    public java.util.List getSelections(){ 
	return plotters.subList(plotters.size() - selectionCount - selection3CCount, plotters.size() - selection3CCount); 
    }

    public void addSelection(Selection newSelection){ 
	if(!getSelections().contains(newSelection)){
	    getSelections().add(newSelection);
	    selectionCount++;
	    repaint();
	}
	
    }
    
    public void removeSelection(Selection oldSelection){ 
	if(getSelections().remove(oldSelection)){
	    selectionCount--; 
	    repaint();
	}
    }
    
    public void removeAllSelections(){
	getSelections().clear();
	selectionCount = 0;
    }

    public java.util.List get3CSelections(){ 
	return plotters.subList(plotters.size() - selection3CCount, plotters.size());
    }

    public void add3CSelection(Selection newSelection){ 
	if(!get3CSelections().contains(newSelection)){
	    get3CSelections().add(newSelection);
	    selection3CCount++;
	    repaint();
	}
    }
    
    public void remove3CSelection(Selection oldSelection){ 
	if(get3CSelections().remove(oldSelection));{ 
	    selection3CCount--;
	    repaint();
	}
    }

    public void clearSelections(){
	parent.removeSelectionDisplay();
	parent.remove3CSelectionDisplay();
	repaint();
    }
    
    public void clearRegSelections(){
	getSelections().clear();
	selectionCount = 0;
	repaint();
    }

    public void clear3CSelections(){
	get3CSelections().clear();
	selection3CCount = 0;
	repaint();
    }

    public void print(){
	parent.print();
    }

    public Dimension getDisplaySize(){ return displaySize; }

    public static Set getGlobalFilters(){ return globalFilters; }

    public void setAutoColor(boolean b){ autoColor = b; }

    public boolean getAutoColor(){ return autoColor; }

    public boolean hasBottomTimeBorder(){
	if(scaleBorder.getBottomScaleMapper() != null){
	    return true;
	}
	return false;
    }

    public void addBottomTimeBorder(){	
	scaleBorder.setBottomScaleMapper(timeScaleMap); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(preferredBSDWidth + current.left + current.right, 
				       preferredBSDHeight + current.top + current.bottom));
	this.revalidate();
    }

    public void removeBottomTimeBorder(){ 
	scaleBorder.clearBottomScaleMapper(); 
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(preferredBSDWidth + current.left + current.right, 
				       preferredBSDHeight + current.top + current.bottom));
    }

    public boolean hasTopTimeBorder(){
	if(scaleBorder.getTopScaleMapper() != null){
	    return true;
	}
	return false;
    }

    public void addTopTimeBorder(){ 
	scaleBorder.setTopScaleMapper(timeScaleMap);
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(preferredBSDWidth + current.left + current.right, 
				       preferredBSDHeight + current.top + current.bottom));
	this.revalidate();
    }

    public void removeTopTimeBorder(){ 
	scaleBorder.clearTopScaleMapper();
	Insets current = this.getInsets();
	setPreferredSize(new Dimension(preferredBSDWidth + current.left + current.right, 
				       preferredBSDHeight + current.top + current.bottom));
    }
    
    protected void resize() {
	Insets insets = getInsets();
	Dimension d = getSize();
	displaySize = new Dimension(d.width - insets.left - insets.right, d.height - insets.top - insets.bottom);
	if(displaySize.height < 0 || displaySize.width < 0){
	    return; 
	}
	Rectangle plotPainterBounds = plotPainter.getBounds();
	Rectangle newPlotPainterBounds = new Rectangle(plotPainterBounds);
	newPlotPainterBounds.setSize(displaySize);
	plotPainter.setBounds(newPlotPainterBounds);
	timeScaleMap.setTotalPixels(displaySize.width);
	ampScaleMap.setTotalPixels(displaySize.height);
    }

    public void remove(DataSetSeismogram[] seismos){}

    public void remove(){
       logger.debug("bsd being removed");
       parent.removeDisplay(this);
       destroy();
    }

    public void destroy(){
	clearSelections();
	registrar.removeListener(this);
	registrar.remove(getSeismograms());
    }

    public void setUnfilteredDisplay(boolean visible){
	Iterator e = plotters.subList(0, seisCount).iterator();
	DataSetSeismogram[] seismos = new DataSetSeismogram[seisCount];
	int i = 0;
	while(e.hasNext()){
	    SeismogramShape current = (SeismogramShape)e.next();
	    current.setVisibility(visible);
	    seismos[i] = current.getSeismogram();
	    i++;
	} 
 	if (visible) {
	    registrar.add(seismos);
	}else {
	    registrar.remove(seismos);
	} // end of else
	repaint();
    }

    public void applyFilter(ColoredFilter filter){
	DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
	Plotter[] filteredShapes = new Plotter[seismograms.size()];
	int i = 0;
	if(filters.contains(filter)){
	    Iterator e = plotters.subList(seisCount, seisCount + filterCount).iterator();
	    while(e.hasNext()){
		FilteredSeismogramShape current = ((FilteredSeismogramShape)e.next());
		if(current.getFilter() == filter){
		    seismos[i] = current.getFilteredSeismogram();
		    filteredShapes[i] = current;
		    i++;
		}
	    }
	}else{
	    filters.add(filter);
	    Iterator e = seismograms.iterator();
	    while(e.hasNext()){
		DataSetSeismogram current = (DataSetSeismogram)e.next();
		FilteredSeismogramShape filteredShape = new FilteredSeismogramShape(filter, current);
		seismos[i] = filteredShape.getFilteredSeismogram();
		filteredShapes[i] = filteredShape;
		i++;		
		filterCount++;
	    }
	    addToPlotters(seisCount, filteredShapes);
	}
	if (filter.getVisibility()) {
	    registrar.add(seismos);
	    java.util.List filterList = plotters.subList(seisCount, seisCount + filterCount);
	    if(filterList.size() > 1){
		for(int j = 0; j < filteredShapes.length; j++){
		    filterList.remove(filteredShapes[j]);
		    filterList.add(filterList.size(),filteredShapes[j]);
		}
	    }
	}else {
	    registrar.remove(seismos);
	}
	for(i = 0; i < seismos.length; i++){
	    filteredShapes[i].setVisibility(filter.getVisibility());
	}
	repaint();
    }
   
    private void addToPlotters(int startingPosition, Plotter[] newPlotters){
	for(int i = 0; i < newPlotters.length; i++){
	    plotters.add(startingPosition + i, newPlotters[i]);
	}
    }
    
    private class PlotPainter extends JComponent{
	public void paint(Graphics g){
	    Graphics2D g2 = (Graphics2D)g;
	    Iterator e = plotters.iterator();
	    Rectangle2D.Float stringBounds = new Rectangle2D.Float();
	    stringBounds.setRect(g2.getFontMetrics().getStringBounds("test", g2));
	    int i = 0;
	    while(e.hasNext()){
		Plotter current = (Plotter)e.next();
		current.draw(g2, displaySize, currentTimeEvent, currentAmpEvent);
		if(current instanceof NamedPlotter){
		    if(((NamedPlotter)current).drawName(g2, 5, (int)(displaySize.height - 3 - i * stringBounds.height)))
			i++;
		}
	    }
	}
    }
   
    
    private static Set globalFilters = new HashSet();

    public final static int preferredBSDHeight = 100;

    public final static int preferredBSDWidth = 200;

    public ArrayList filters = new ArrayList();

    private Dimension displaySize;

    private VerticalSeismogramDisplay parent; 
    
    private LinkedList seismograms = new LinkedList();
    
    private LinkedList plotters = new LinkedList();
    
    private int seisCount = 0, filterCount = 0, flagCount = 0, selectionCount = 0, selection3CCount = 0;

    private Registrar registrar;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;
    
    private ScaleBorder scaleBorder;

    private TimeScaleCalc timeScaleMap;
    
    private AmpScaleMapper ampScaleMap;
   
    private PlotPainter plotPainter;

    private boolean autoColor = true;

    private DataSetSeismogram[] seismogramArray;

    private String[] seismogramNames;
    
    private static Color[] seisColors = { Color.blue, Color.red,  Color.gray, Color.magenta, Color.cyan };

    private static Color[] selectionColors = { new NamedColor(255, 0, 0, 64, "red"),  
					       new NamedColor(255, 255, 0, 64, "yellow"), 
					       new NamedColor(0, 255, 0, 64, "green"),  
					       new NamedColor(0, 0, 255, 64, "blue")};

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
