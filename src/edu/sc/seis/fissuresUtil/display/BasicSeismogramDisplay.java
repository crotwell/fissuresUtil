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
 *
 */

public class BasicSeismogramDisplay extends JComponent implements ConfigListener{
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, VerticalSeismogramDisplay parent)throws IllegalArgumentException{
        this(seismos, new BasicTimeConfig(seismos), new RMeanAmpConfig(seismos), parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, TimeConfig tc,
                                  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
        this(seismos, tc, new RMeanAmpConfig(seismos), parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, AmpConfig ac,
                                  VerticalSeismogramDisplay parent)throws IllegalArgumentException{
        this(seismos, new BasicTimeConfig(seismos), ac, parent);
    }
    
    public BasicSeismogramDisplay(DataSetSeismogram[] seismos, TimeConfig tc, AmpConfig ac,
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
        add(seismos);
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
    
    public void add(DataSetSeismogram[] seismos){
        registrar.add(seismos);
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] != null){
                seismograms.add(seismos[i]);
                DrawableSeismogram newPlotter;
                if (autoColor) {
                    newPlotter = new DrawableSeismogram(seismos[i],
                                                     seisColors[(seismograms.size() -1) % seisColors.length]);
                }else {
                    newPlotter = new DrawableSeismogram(seismos[i]);
                } // end of else
                if(parent != null){
                    newPlotter.setVisibility(parent.getOriginalVisibility());
                }
                plotters.add(newPlotter);
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
        this.arrivals = arrivals;
        try{
            MicroSecondDate originTime = new MicroSecondDate(((XMLDataSet)((DataSetSeismogram)seismograms.getFirst()).getDataSet()).
                                                                 getEvent().get_preferred_origin().origin_time);
            
            for(int i = 0; i < arrivals.length; i++){
                FlagPlotter current = new FlagPlotter(new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) +
                                                                              originTime.getMicroSecondTime()),
                                                      arrivals[i].getPhase().getName());
                plotters.addLast(current);
            }
        } catch ( edu.iris.Fissures.IfEvent.NoPreferredOrigin e) {
            logger.warn("Caught NoPreferredOrigin on addFlags", e);
        } // end of catch
        
        repaint();
    }
    
    public Arrival[] getArrivals(){
        return arrivals;
    }
    
    public void addCurrentTimeFlag(){
        plotters.addLast(new CurrentTimeFlagPlotter());
    }
    
    public void removeAllFlags(){
        arrivals = null;
        Iterator it = new PlotterIterator(FlagPlotter.class);
        while ( it.hasNext()) {
            Object o = it.next();
            logger.debug("removeAllFlags "+o.getClass().getName());
            it.remove();
        } // end of while ()
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
            for(int i = 0; i < seismogramArray.length; i++){
                Iterator e = new PlotterIterator(DrawableSeismogram.class);
                while(e.hasNext()){
                    DrawableSeismogram current = (DrawableSeismogram)e.next();
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
        currentAmpEvent = event;
        repaint();
    }
    
    public void updateTime(TimeEvent event){
        currentTimeEvent = event;
        repaint();
    }
    
    public MicroSecondTimeRange getTime(){
        return currentTimeEvent.getTime();
    }
    
    /**
     * @returns the time for the given pixel value.
     */
    public MicroSecondDate getTime(int pixel) {
        return SimplePlotUtil.getValue(getWidth()-getInsets().left-getInsets().right,
                                       getTime().getBeginTime(),
                                       getTime().getEndTime(),
                                       pixel-getInsets().left);
    }
    
    /**
     * @returns the pixel for the given time.
     */
    public int getPixel(MicroSecondDate date) {
        return SimplePlotUtil.getPixel(getWidth()-getInsets().left-getInsets().right,
                                       getTime().getBeginTime(),
                                       getTime().getEndTime(),
                                       date);
    }
    
    public void update(ConfigEvent event){
        currentTimeEvent = event.getTimeEvent();
        currentAmpEvent = event.getAmpEvent();
        repaint();
    }
    
    public VerticalSeismogramDisplay getVerticalParent(){ return parent; }
    
    public java.util.List getPlotters(Class plotterClass) {
        java.util.LinkedList out = new java.util.LinkedList();
        Iterator it = new PlotterIterator(plotterClass);
        while ( it.hasNext()) {
            out.addLast(it.next());
        } // end of while ()
        return out;
    }
    
    public java.util.List getSelections(){
        return getPlotters(Selection.class);
    }
    
    public void clearSelections(){
        clearSingleSelections();
        clearThreeCSelections();
    }
    
    public java.util.List getRegSelections(){
        return getPlotters(SingleSelection.class);
    }
    
    public void addSelection(Selection newSelection){
        if(! plotters.contains(newSelection)){
            plotters.add(newSelection);
            repaint();
        }
    }
    
    public void removeSelection(Selection old){
        if( plotters.remove(old)){
            old.removeParent(this);
            repaint();
        }
    }
    
    public void clearSingleSelections(){
        Iterator e = new PlotterIterator(SingleSelection.class);
        while(e.hasNext()){
            Selection old = (Selection)e.next();
            e.remove();
            old.removeParent(this);
        }
        repaint();
    }
    
    public java.util.List getThreeCSelections(){
        return getPlotters(ThreeCSelection.class);
    }
    
    public void addThreeCSelection(ThreeCSelection newSelection){
        if( ! plotters.contains(newSelection)){
            plotters.add(newSelection);
            repaint();
        }
    }
    
    public void removeThreeCSelection(ThreeCSelection old) {
        if(plotters.remove(old));{
            old.removeParent(this);
            repaint();
        }
    }
    
    public void clearThreeCSelections(){
        Iterator e = new PlotterIterator(ThreeCSelection.class);
        while(e.hasNext()){
            Selection old = (Selection)e.next();
            e.remove();
            old.removeParent(this);
        }
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
    
    public boolean contains(DataSetSeismogram seismo){
        if(seismograms.contains(seismo)){
            return true;
        }
        return false;
    }
    
    public void remove(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            if(seismograms.contains(seismos[i])){
                seismograms.remove(seismos[i]);
                PlotterIterator it = new PlotterIterator(DrawableSeismogram.class);
                while(it.hasNext()){
                    DrawableSeismogram current = (DrawableSeismogram)it.next();
                    if(current.getSeismogram() == seismos[i]){
                        it.remove();
                    }
                }
            }
        }
        registrar.remove(seismos);
    }
    
    /** removes this Basic SeismogramDisplay from the parent. */
    public void remove(){
        logger.debug("bsd being removed");
        parent.removeDisplay(this);
        destroy();
    }
    
    void destroy(){
        clearSelections();
        registrar.removeListener(this);
        registrar.remove(getSeismograms());
    }
    
    public void setUnfilteredDisplay(boolean visible){
        Iterator e = new PlotterIterator(DrawableSeismogram.class);
        java.util.LinkedList seismoList = new java.util.LinkedList();
        while(e.hasNext()){
            DrawableSeismogram current = (DrawableSeismogram)e.next();
            if ( current.getClass().equals(DrawableSeismogram.class)) {
                current.setVisibility(visible);
                seismoList.addLast(current.getSeismogram());
            } // end of if ()
        }
        DataSetSeismogram[] seismos =
            new DataSetSeismogram[seismoList.size()];
        seismos = (DataSetSeismogram[])seismoList.toArray(seismos);
        if (visible) {
            registrar.add(seismos);
        }else {
            registrar.remove(seismos);
        } // end of else
        repaint();
    }
    
    public void applyFilter(ColoredFilter filter){
        DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
        int i = 0;
        if(filters.contains(filter)){
            LinkedList filterShapes = new LinkedList();
            Iterator e = new PlotterIterator(DrawableFilteredSeismogram.class);
            while(e.hasNext()){
                logger.debug("contains filter");
                DrawableFilteredSeismogram current = ((DrawableFilteredSeismogram)e.next());
                if(current.getFilter() == filter){
                    current.setVisibility(filter.getVisibility());
                    e.remove();
                    filterShapes.addLast(current);
                    seismos[i] = current.getFilteredSeismogram();
                    i++;
                }
            }
            plotters.addAll(filterShapes);
        }else{
            filters.add(filter);
            Iterator e = seismograms.iterator();
            while(e.hasNext()){
                DataSetSeismogram current = (DataSetSeismogram)e.next();
                DrawableFilteredSeismogram filteredShape = new DrawableFilteredSeismogram(current, filter);
                seismos[i] = filteredShape.getFilteredSeismogram();
                plotters.add(filteredShape);
                i++;
            }
        }
        if (filter.getVisibility()) {
            registrar.add(seismos);
        }else {
            registrar.remove(seismos);
        }
        repaint();
    }
    
    private class PlotPainter extends JComponent{
        public void paintComponent(Graphics g){
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
    
    /**
     * An iterator that only returns instances of the given class. All other
     * elements are silently skipped.
     */
    class PlotterIterator implements Iterator {
        
        PlotterIterator(Class iteratorClass) {
            this.iteratorClass = iteratorClass;
            it = plotters.iterator();
        }
        
        public boolean hasNext() {
            if ( nextObj != null) {
                return true;
            } // end of if ()
            if ( finished ) {
                return false;
            } // end of if ()
            //find next
            while ( it.hasNext()) {
                Object n = it.next();
                if ( iteratorClass.isInstance(n) ) {
                    nextObj = n;
                    return true;
                } // end of if ()
            } // end of while ()
            finished = true;
            return false;
        }
        
        public Object next() {
            if ( hasNext() == false) {
                return null;
            } // end of if ()
            // hasNext will populate nextObj if it returned true
            Object out = nextObj;
            nextObj=null;
            return out;
        }
        
        public void remove() {
            it.remove();
        }
        
        private Iterator it;
        
        private Class iteratorClass;
        
        private Object nextObj;
        
        private boolean finished = false;
        
    }
    
    
    private static Set globalFilters = new HashSet();
    
    public final static int preferredBSDHeight = 100;
    
    public final static int preferredBSDWidth = 200;
    
    public ArrayList filters = new ArrayList();
    
    private Dimension displaySize;
    
    private VerticalSeismogramDisplay parent;
    
    private LinkedList seismograms = new LinkedList();
    
    private LinkedList plotters = new LinkedList();
    
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
    
    private Arrival[] arrivals;
    
    private static Color[] seisColors = { Color.blue, Color.red,  Color.gray, Color.magenta, Color.cyan };
    
    private static Color[] selectionColors = { new NamedColor(255, 0, 0, 64, "red"),
            new NamedColor(255, 255, 0, 64, "yellow"),
            new NamedColor(0, 255, 0, 64, "green"),
            new NamedColor(0, 0, 255, 64, "blue")};
    
    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
