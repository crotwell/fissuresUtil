package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.drawable.*;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.util.*;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;
import org.apache.log4j.Category;

/**
 * BasicSeismogramDisplay.java
 *
 *
 * Created: Thu Jun  6 09:52:51 2002
 *
 * @author Charlie Groves
 *
 */

public class BasicSeismogramDisplay extends SeismogramDisplay implements ConfigListener{

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
        timeScaleMap = new TimeScaleCalc(PREFERRED_WIDTH, registrar);
        ampScaleMap = new AmpScaleMapper(PREFERRED_HEIGHT, 4, registrar);
        scaleBorder = new ScaleBorder();
        scaleBorder.setLeftScaleMapper(ampScaleMap);
        setBorder(createDefaultBorder());
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        addMouseMotionListener(SeismogramDisplay.getMouseMotionForwarder());
        addMouseListener(SeismogramDisplay.getMouseForwarder());
        plotters.add(new TimeAmpPlotter(this));
        add(new PlotPainter());
    }

    public void add(DataSetSeismogram[] seismos){
        registrar.add(seismos);
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] != null){
                seismograms.add(seismos[i]);
                DrawableSeismogram newPlotter = new DrawableSeismogram(this, seismos[i],
                                                                       seisColors[(seismograms.size() -1) % seisColors.length]);
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
        try{
            MicroSecondDate originTime = new MicroSecondDate(((DataSetSeismogram)seismograms.getFirst()).getDataSet().
                                                                 getEvent().get_preferred_origin().origin_time);

            for(int i = 0; i < arrivals.length; i++){
                FlagPlotter current = new FlagPlotter(new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) +
                                                                              originTime.getMicroSecondTime()),
                                                      arrivals[i].getPhase().getName());
                plotters.addLast(current);
            }
        } catch ( NoPreferredOrigin e) {
            logger.warn("Caught NoPreferredOrigin on addFlags", e);
        } // end of catch

        repaint();
    }

    public void reset(){
        registrar.reset();
    }

    public void reset(DataSetSeismogram[] seismograms){
        registrar.reset(seismograms);
    }

    public void setCurrentTimeFlag(boolean visible){
        if(visible){
            plotters.addLast(new CurrentTimeFlagPlotter());
        }else{
            PlotterIterator it = new PlotterIterator(CurrentTimeFlagPlotter.class);
            it.clear();
        }
    }

    public void removeAllFlags(){
        PlotterIterator it = new PlotterIterator(FlagPlotter.class);
        it.clear();
        repaint();
    }

    public VerticalSeismogramDisplay getParentDisplay(){ return parent; }

    public void setRegistrar(Registrar registrar){
        registrar.add(getSeismograms());
        this.registrar.removeListener(this);
        this.registrar.remove(getSeismograms());
        this.registrar = registrar;
        registrar.addListener(this);
    }

    public Registrar getRegistrar(){ return registrar; }

    public void updateAmp(AmpEvent event){
        currentAmpEvent = event;
        repaint();
    }

    public void setAmpConfig(AmpConfig ac){ registrar.setAmpConfig(ac); }

    public void setGlobalizedAmpConfig(AmpConfig ac){ setAmpConfig(ac); }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig(){ return registrar.getAmpConfig(); }

    public void updateTime(TimeEvent event){
        currentTimeEvent = event;
        repaint();
    }

    public void setTimeConfig(TimeConfig tc){ registrar.setTimeConfig(tc); }

    public TimeConfig getTimeConfig(){ return registrar.getTimeConfig(); }

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

    public TimeAmpPlotter getTimeAmpPlotter(){
        PlotterIterator pi = new PlotterIterator(TimeAmpPlotter.class);
        return (TimeAmpPlotter)pi.next();
    }

    public java.util.List getSelections(){
        return getPlotters(Selection.class);
    }

    public void clearSelections(){
        new PlotterIterator(Selection.class).clear();
    }

    public void addSelection(Selection newSelection){
        if(!plotters.contains(newSelection)){
            plotters.add(newSelection);
            repaint();
        }
    }

    public void remove(Selection old){
        if(plotters.remove(old)){
            repaint();
        }
    }

    public void print(){
        parent.print();
    }

    public static Set getGlobalFilters(){ return globalFilters; }

    public boolean hasBottomTimeBorder(){
        if(scaleBorder.getBottomScaleMapper() != null){
            return true;
        }
        return false;
    }

    public void addBottomTimeBorder(){
        scaleBorder.setBottomScaleMapper(timeScaleMap);
        Insets current = this.getInsets();
        setPreferredSize(new Dimension(PREFERRED_WIDTH + current.left + current.right,
                                       PREFERRED_HEIGHT + current.top + current.bottom));
        this.revalidate();
    }

    public void removeBottomTimeBorder(){
        scaleBorder.clearBottomScaleMapper();
        Insets current = this.getInsets();
        setPreferredSize(new Dimension(PREFERRED_WIDTH + current.left + current.right,
                                       PREFERRED_HEIGHT + current.top + current.bottom));
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
        setPreferredSize(new Dimension(PREFERRED_WIDTH + current.left + current.right,
                                       PREFERRED_HEIGHT + current.top + current.bottom));
        this.revalidate();
    }

    public void removeTopTimeBorder(){
        scaleBorder.clearTopScaleMapper();
        Insets current = this.getInsets();
        setPreferredSize(new Dimension(PREFERRED_WIDTH + current.left + current.right,
                                       PREFERRED_HEIGHT + current.top + current.bottom));
    }

    private Border createDefaultBorder(){
        return BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                                                                                     new SeismogramDisplayRemovalBorder(this)),
                                                  BorderFactory.createCompoundBorder(scaleBorder,
                                                                                     BorderFactory.createLoweredBevelBorder()));
    }

    public void addLeftTitleBorder(LeftTitleBorder ltb){
        Border bevelTitle = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                                                               ltb);
        Border bevelTitleRemoval = BorderFactory.createCompoundBorder(bevelTitle,
                                                                      new SeismogramDisplayRemovalBorder(this));
        Border scaleBevel = BorderFactory.createCompoundBorder(scaleBorder,
                                                               BorderFactory.createLoweredBevelBorder());
        setBorder(BorderFactory.createCompoundBorder(bevelTitleRemoval, scaleBevel));
        resize();
    }

    protected void resize() {
        Insets insets = getInsets();
        Dimension d = getSize();
        Dimension size = new Dimension(d.width - insets.left - insets.right,
                                       d.height - insets.top - insets.bottom);
        if(size.height < 0 || size.width < 0){
            return;
        }
        timeScaleMap.setTotalPixels(size.width);
        ampScaleMap.setTotalPixels(size.height);
    }

    public boolean contains(DataSetSeismogram seismo){
        if(seismograms.contains(seismo)){
            return true;
        }
        return false;
    }

    public void clear(){
        remove();
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
                        repaint();
                    }
                }
            }
        }
        if(seismograms.size() == 0){
            clear();
        }
        registrar.remove(seismos);
    }

    /** removes this Basic SeismogramDisplay from the parent. */
    public void remove(){
        parent.removeDisplay(this);
        destroy();
    }

    void destroy(){
        clearSelections();
        registrar.removeListener(this);
        registrar.remove(getSeismograms());
        registrar.removeListener(ampScaleMap);
        registrar.removeListener(timeScaleMap);
    }

    public void setOriginalVisibility(boolean visible){
        Iterator e = new PlotterIterator(DrawableSeismogram.class);
        LinkedList seismoList = new java.util.LinkedList();
        List drawableList = new ArrayList();
        while(e.hasNext()){
            DrawableSeismogram current = (DrawableSeismogram)e.next();
            if ( current.getClass().equals(DrawableSeismogram.class)) {
                drawableList.add(current);
                seismoList.addLast(current.getSeismogram());
            } // end of if ()
        }
        DataSetSeismogram[] seismos = new DataSetSeismogram[seismoList.size()];
        seismos = (DataSetSeismogram[])seismoList.toArray(seismos);
        if (visible) {
            registrar.add(seismos);
        }else {
            registrar.remove(seismos);
        } // end of else
        e = drawableList.iterator();
        while(e.hasNext()){
            DrawableSeismogram current = (DrawableSeismogram)e.next();
            current.setVisibility(visible);
        }
        repaint();
    }

    public void applyFilter(ColoredFilter filter){
        DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
        int i = 0;
        LinkedList filterShapes = new LinkedList();
        if(filters.contains(filter)){
            Iterator e = new PlotterIterator(DrawableFilteredSeismogram.class);
            while(e.hasNext()){
                DrawableFilteredSeismogram current = ((DrawableFilteredSeismogram)e.next());
                if(current.getFilter() == filter){
                    current.setVisibility(filter.getVisibility());
                    e.remove();
                    filterShapes.addLast(current);
                    seismos[i] = current.getFilteredSeismogram();
                    i++;
                }
            }
        }else{
            filters.add(filter);
            Iterator e = seismograms.iterator();
            while(e.hasNext()){
                DataSetSeismogram current = (DataSetSeismogram)e.next();
                DrawableFilteredSeismogram filteredShape = new DrawableFilteredSeismogram(this, current, filter);
                seismos[i] = filteredShape.getFilteredSeismogram();
                filterShapes.add(filteredShape);
                i++;
            }
        }
        if (filter.getVisibility()) {
            registrar.add(seismos);
        }else {
            registrar.remove(seismos);
        }
        plotters.addAll(filterShapes);
        repaint();
    }

    public void removeFilter(ColoredFilter filter){
        if(filters.contains(filter)){
            DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
            int i = 0;
            Iterator e = new PlotterIterator(DrawableFilteredSeismogram.class);
            while(e.hasNext()){
                DrawableFilteredSeismogram current = ((DrawableFilteredSeismogram)e.next());
                if(current.getFilter() == filter){
                    e.remove();
                    seismos[i] = current.getFilteredSeismogram();
                    i++;
                }
            }
            filters.remove(filter);
            registrar.remove(seismos);
        }
    }

    private class PlotPainter extends JComponent{
        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.WHITE);
            Dimension size = getSize();
            g2.fill(new Rectangle2D.Float(0,0, size.width, size.height));
            int namesDrawn = 1;
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(g2.getFontMetrics().getStringBounds("test", g2));
            for (int i = 0; i < plotters.size(); i++){
                Plotter current = (Plotter)plotters.get(i);
                current.draw(g2, size, currentTimeEvent, currentAmpEvent);
                if(current instanceof TimeAmpPlotter){
                    TimeAmpPlotter taPlotter = (TimeAmpPlotter)current;
                    g2.setFont(DisplayUtils.MONOSPACED_FONT);
                    stringBounds.setRect(g2.getFontMetrics().getStringBounds(taPlotter.getText(), g2));
                    taPlotter.drawName(g2,(int)(size.width - stringBounds.width), size.height - 3);
                    g2.setFont(DisplayUtils.DEFAULT_FONT);
                }else if(current instanceof NamedPlotter){
                    if(((NamedPlotter)current).drawName(g2, 5, (int)(namesDrawn * stringBounds.height)))
                        namesDrawn++;
                }
            }
        }
    }

    /**
     * An iterator that only returns instances of the given class. All other
     * elements are silently skipped.
     */
    private class PlotterIterator implements Iterator {

        private PlotterIterator(Class iteratorClass) {
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

        public void clear(){
            while(hasNext()){
                next();
                remove();
            }
        }
        private Iterator it;

        private Class iteratorClass;

        private Object nextObj;

        private boolean finished = false;

    }

    public void addSoundPlay(){
        try{
            plotters.add(new SoundPlay(this, new SeismogramContainer(getSeismograms()[0])));
        }
        catch(NullPointerException e){
            System.out.println("Sample Rate cannot be calculated, so sound is not permitted.");
            e.printStackTrace();
        }
    }

    public void removeSoundPlay(){
        new PlotterIterator(SoundPlay.class).clear();
    }

    private static Set globalFilters = new HashSet();

    public final static int PREFERRED_HEIGHT = 150;

    public final static int PREFERRED_WIDTH = 250;

    private ArrayList filters = new ArrayList();

    private VerticalSeismogramDisplay parent;

    private LinkedList seismograms = new LinkedList();

    private LinkedList plotters =new LinkedList();

    private Registrar registrar;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private ScaleBorder scaleBorder;

    private TimeScaleCalc timeScaleMap;

    private AmpScaleMapper ampScaleMap;

    private DataSetSeismogram[] seismogramArray;

    private static Color[] seisColors = { Color.BLUE, Color.RED,  Color.DARK_GRAY, Color.GREEN, Color.BLACK, Color.GRAY };

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
