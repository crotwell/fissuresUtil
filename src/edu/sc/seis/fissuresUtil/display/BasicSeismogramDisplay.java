package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.drawable.*;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.util.*;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
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

public class BasicSeismogramDisplay extends SeismogramDisplay implements TimeListener,
    AmpListener{

    public BasicSeismogramDisplay(SeismogramDisplay parent)throws IllegalArgumentException{
        this(new BasicTimeConfig(), new RMeanAmpConfig(), parent);
    }

    public BasicSeismogramDisplay(TimeConfig tc,
                                  SeismogramDisplay parent)
        throws IllegalArgumentException{
        this(tc, new RMeanAmpConfig(), parent);
    }

    public BasicSeismogramDisplay(AmpConfig ac, SeismogramDisplay parent)
        throws IllegalArgumentException{
        this(new BasicTimeConfig(), ac, parent);
    }

    public BasicSeismogramDisplay(TimeConfig tc, AmpConfig ac,
                                  SeismogramDisplay parent)throws IllegalArgumentException{
        this.parent = parent;
        setTimeConfig(tc);
        setAmpConfig(ac);
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
        timeScaleMap = new TimeScaleCalc(PREFERRED_WIDTH, tc);
        ampScaleMap = new AmpScaleMapper(PREFERRED_HEIGHT, 4, ac);
        scale = new ScaleBorder();
        scale.setLeftScaleMapper(ampScaleMap);
        Border etch = BorderFactory.createEtchedBorder();
        Border removal = new SeismogramDisplayRemovalBorder(this);
        Border etchRemoval = BorderFactory.createCompoundBorder(etch, removal);
        Border bevel = BorderFactory.createLoweredBevelBorder();
        Border scaleBevel = BorderFactory.createCompoundBorder(scale, bevel);
        setBorder(BorderFactory.createCompoundBorder(etchRemoval, scaleBevel));
        setSize();
        addMouseMotionListener(SeismogramDisplay.getMouseMotionForwarder());
        addMouseListener(SeismogramDisplay.getMouseForwarder());
        drawable.add(new TimeAmpLabel(this));
        add(new PlotPainter());
    }

    public void add(DataSetSeismogram[] seismos){
        tc.add(seismos);
        ac.add(seismos);
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] != null){
                seismograms.add(seismos[i]);
                DrawableSeismogram newPlotter = new DrawableSeismogram(this, seismos[i],
                                                                       seisColors[(seismograms.size() -1) % seisColors.length]);
                if(parent != null){
                    newPlotter.setVisibility(parent.getOriginalVisibility());
                }
                drawable.add(newPlotter);
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

    public List getSeismogramList(){ return seismograms; }

    public void addFlags(Arrival[] arrivals) {
        try{
            MicroSecondDate originTime = new MicroSecondDate(((DataSetSeismogram)seismograms.getFirst()).getDataSet().
                                                                 getEvent().get_preferred_origin().origin_time);

            for(int i = 0; i < arrivals.length; i++){
                Flag current = new Flag(new MicroSecondDate((long)(arrivals[i].getTime() * 1000000) +
                                                                              originTime.getMicroSecondTime()),
                                                      arrivals[i].getPhase().getName());
                drawable.addLast(current);
            }
        } catch ( NoPreferredOrigin e) {
            logger.warn("Caught NoPreferredOrigin on addFlags", e);
        } // end of catch

        repaint();
    }

    public void reset(){
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismograms){
        tc.reset(seismograms);
        ac.reset(seismograms);
    }

    public void setCurrentTimeFlag(boolean visible){
        if(visible){
            drawable.addLast(new CurrentTimeFlag());
        }else{
            DrawableIterator it = new DrawableIterator(CurrentTimeFlag.class);
            it.clear();
        }
    }

    public void removeAllFlags(){
        DrawableIterator it = new DrawableIterator(Flag.class);
        it.clear();
        repaint();
    }

    public SeismogramDisplay getParentDisplay(){ return parent; }

    public void updateAmp(AmpEvent event){
        currentAmpEvent = event;
        repaint();
    }

    public void setAmpConfig(AmpConfig ac){
        if(this.ac != null){
            this.ac.removeListener(this);
            tc.removeListener(this.ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ac;
        ac.addListener(this);
        tc.addListener(ac);
        ac.add(getSeismograms());
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){ setAmpConfig(ac); }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig(){ return ac; }

    public void updateTime(TimeEvent event){
        currentTimeEvent = event;
        repaint();
    }

    public void setTimeConfig(TimeConfig tc){
        if(this.tc != null){
            this.tc.removeListener(this);
            this.tc.removeListener(ac);
            this.tc.add(getSeismograms());
        }
        this.tc = tc;
        tc.addListener(this);
        tc.addListener(ac);
        tc.add(getSeismograms());
    }
    public TimeConfig getTimeConfig(){ return tc; }

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

    public SeismogramDisplay getDisplayParent(){ return parent; }

    public java.util.List getPlotters(Class plotterClass) {
        java.util.LinkedList out = new java.util.LinkedList();
        Iterator it = new DrawableIterator(plotterClass);
        while ( it.hasNext()) {
            out.addLast(it.next());
        } // end of while ()
        return out;
    }

    public TimeAmpLabel getTimeAmpLabel(){
        DrawableIterator pi = new DrawableIterator(TimeAmpLabel.class);
        return (TimeAmpLabel)pi.next();
    }

    public java.util.List getSelections(){
        return getPlotters(Selection.class);
    }

    public void clearSelections(){
        new DrawableIterator(Selection.class).clear();
    }

    public void addSelection(Selection newSelection){
        if(!drawable.contains(newSelection)){
            drawable.add(newSelection);
            repaint();
        }
    }

    public void remove(Selection old){
        if(drawable.remove(old)){
            repaint();
        }
    }

    public void print(){
        parent.print();
    }

    public static Set getGlobalFilters(){ return globalFilters; }

    public boolean hasBottomTimeBorder(){
        if(scale.getBottomScaleMapper() != null){
            return true;
        }
        return false;
    }

    public void addBottomTimeBorder(){
        scale.setBottomScaleMapper(timeScaleMap);
        setSize();
    }

    public void removeBottomTimeBorder(){
        scale.clearBottomScaleMapper();
        setSize();
    }

    public boolean hasTopTimeBorder(){
        if(scale.getTopScaleMapper() != null){
            return true;
        }
        return false;
    }

    public void addTopTimeBorder(){
        scale.setTopScaleMapper(timeScaleMap);
        setSize();
    }

    public void removeTopTimeBorder(){
        scale.clearTopScaleMapper();
        setSize();
    }

    private void setSize(){
        Insets in = getInsets();
        setPreferredSize(new Dimension(PREFERRED_WIDTH,
                                       PREFERRED_HEIGHT + in.top + in.bottom));
        revalidate();
    }

    public void addLeftTitleBorder(LeftTitleBorder ltb){
        Border etch = BorderFactory.createEtchedBorder();
        Border removal = new SeismogramDisplayRemovalBorder(this);
        Border removalTitle = BorderFactory.createCompoundBorder(removal,
                                                                 ltb);
        Border etchRemovalTitle = BorderFactory.createCompoundBorder(etch,
                                                                     removalTitle);
        Border scaleBevel = BorderFactory.createCompoundBorder(scale,
                                                               BorderFactory.createLoweredBevelBorder());
        setBorder(BorderFactory.createCompoundBorder(etchRemovalTitle, scaleBevel));
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
                DrawableIterator it = new DrawableIterator(DrawableSeismogram.class);
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
        tc.remove(seismos);
        ac.remove(seismos);
    }

    /** removes this Basic SeismogramDisplay from the parent. */
    public void remove(){
        //TODO make remove display a seismogram display method
        ((VerticalSeismogramDisplay)parent).removeDisplay(this);
        destroy();
    }

    void destroy(){
        clearSelections();
        tc.removeListener(this);
        ac.removeListener(this);
        tc.remove(getSeismograms());
        ac.remove(getSeismograms());
        ac.removeListener(ampScaleMap);
        tc.removeListener(timeScaleMap);
    }

    public void setOriginalVisibility(boolean visible){
        Iterator e = new DrawableIterator(DrawableSeismogram.class);
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
            tc.add(seismos);
            ac.add(seismos);
        }else {
            tc.remove(seismos);
            ac.remove(seismos);
        } // end of else
        e = drawableList.iterator();
        while(e.hasNext()){
            DrawableSeismogram current = (DrawableSeismogram)e.next();
            current.setVisibility(visible);
        }
        repaint();
    }

    public boolean getOriginalVisibility(){
        if(getDisplayParent() != null){
            return getDisplayParent().getOriginalVisibility();
        }
        else{
            return true;
        }
    }

    public void applyFilter(ColoredFilter filter){
        DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
        int i = 0;
        LinkedList filterShapes = new LinkedList();
        if(filters.contains(filter)){
            Iterator e = new DrawableIterator(DrawableFilteredSeismogram.class);
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
            tc.add(seismos);
            ac.add(seismos);
        }else {
            tc.remove(seismos);
            ac.remove(seismos);
        }
        drawable.addAll(filterShapes);
        repaint();
    }

    public void removeFilter(ColoredFilter filter){
        if(filters.contains(filter)){
            DataSetSeismogram[] seismos = new DataSetSeismogram[seismograms.size()];
            int i = 0;
            Iterator e = new DrawableIterator(DrawableFilteredSeismogram.class);
            while(e.hasNext()){
                DrawableFilteredSeismogram current = ((DrawableFilteredSeismogram)e.next());
                if(current.getFilter() == filter){
                    e.remove();
                    seismos[i] = current.getFilteredSeismogram();
                    i++;
                }
            }
            filters.remove(filter);
            tc.remove(seismos);
            ac.remove(seismos);
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
            for (int i = 0; i < drawable.size(); i++){
                Drawable current = (Drawable)drawable.get(i);
                current.draw(g2, size, currentTimeEvent, currentAmpEvent);
                if(current instanceof TimeAmpLabel){
                    TimeAmpLabel taPlotter = (TimeAmpLabel)current;
                    g2.setFont(DisplayUtils.MONOSPACED_FONT);
                    stringBounds.setRect(g2.getFontMetrics().getStringBounds(taPlotter.getText(), g2));
                    taPlotter.drawName(g2,(int)(size.width - stringBounds.width), size.height - 3);
                    g2.setFont(DisplayUtils.DEFAULT_FONT);
                }else if(current instanceof NamedDrawable){
                    if(((NamedDrawable)current).drawName(g2, 5, (int)(namesDrawn * stringBounds.height)))
                        namesDrawn++;
                }
            }
        }
    }

    /**
     * An iterator that only returns instances of the given class. All other
     * elements are silently skipped.
     */
    private class DrawableIterator implements Iterator {

        private DrawableIterator(Class iteratorClass) {
            this.iteratorClass = iteratorClass;
            it = drawable.iterator();
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
            drawable.add(new SoundPlay(this, new SeismogramContainer(getSeismograms()[0])));
        }
        catch(NullPointerException e){
            System.out.println("Sample Rate cannot be calculated, so sound is not permitted.");
            e.printStackTrace();
        }
    }

    public void removeSoundPlay(){
        new DrawableIterator(SoundPlay.class).clear();
    }

    private static Set globalFilters = new HashSet();

    public final static int PREFERRED_HEIGHT = 150;

    public final static int PREFERRED_WIDTH = 250;

    private ArrayList filters = new ArrayList();

    private SeismogramDisplay parent;

    private LinkedList seismograms = new LinkedList();

    private LinkedList drawable =new LinkedList();

    private TimeConfig tc;

    private AmpConfig ac;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private ScaleBorder scale;

    private TimeScaleCalc timeScaleMap;

    private AmpScaleMapper ampScaleMap;

    private DataSetSeismogram[] seismogramArray;

    private static Color[] seisColors = { Color.BLUE, Color.RED,  Color.DARK_GRAY, Color.GREEN, Color.BLACK, Color.GRAY };

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
