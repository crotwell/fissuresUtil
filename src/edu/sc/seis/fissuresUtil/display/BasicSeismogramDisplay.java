package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.drawable.*;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
        setTimeConfig(tc);
        setAmpConfig(ac);
        drawables.add(new TimeAmpLabel(this));
        add(plotPainter);
    }

    public void add(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] != null){
                seismograms.add(seismos[i]);
                drawables.add(new DrawableSeismogram(this, seismos[i]));
            }
        }
        Iterator e = activeFilters.iterator();
        while(e.hasNext()){
            DisplayUtils.applyFilter((NamedFilter)e.next(), new DrawableIterator(DrawableSeismogram.class,
                                                                                   drawables));
        }
        seismogramArray = null;
    }

    public void remove(Drawable drawable) {
        drawables.remove(drawable);
    }

    public void add(Drawable drawable) {
        if(!drawables.contains(drawable)){
            drawables.add(drawable);
            repaint();
        }
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        // TODO
        return new DrawableIterator(Drawable.class, EMPTY_LIST);
    }
    private static List EMPTY_LIST = new ArrayList();

    public DataSetSeismogram[] getSeismograms(){
        if(seismogramArray == null){
            seismogramArray = (DataSetSeismogram[])seismograms.toArray(new DataSetSeismogram[seismograms.size()]);
        }
        return seismogramArray;
    }

    public List getSeismogramList(){ return seismograms; }

    public void reset(){
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismograms){
        tc.reset(seismograms);
        ac.reset(seismograms);
    }

    public SeismogramDisplay getParentDisplay(){ return parent; }

    public void updateAmp(AmpEvent event){
        currentAmpEvent = event;
        repaint();
    }

    public void setAmpConfig(AmpConfig ac){
        if(this.ac != null){
            this.ac.removeListener(this);
            this.ac.removeListener(ampScaleMap);
            tc.removeListener(this.ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ac;
        ac.addListener(this);
        ac.addListener(ampScaleMap);
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
            this.tc.removeListener(timeScaleMap);
            this.tc.add(getSeismograms());
        }
        this.tc = tc;
        tc.addListener(this);
        tc.addListener(ac);
        tc.addListener(timeScaleMap);
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

    public DrawableIterator iterator(Class drawableClass) {
        return  new DrawableIterator(drawableClass, drawables);
    }

    public TimeAmpLabel getTimeAmpLabel(){
        DrawableIterator pi = new DrawableIterator(TimeAmpLabel.class, drawables);
        return (TimeAmpLabel)pi.next();
    }

    public void clearSelections(){
        Iterator it = drawables.iterator();
        while(it.hasNext()){
            Drawable current = (Drawable)it.next();
            if(current instanceof Selection){
                it.remove();
            }
        }
        repaint();
    }

    public void addSelection(Selection newSelection){
        if(!drawables.contains(newSelection)){
            drawables.add(newSelection);
            repaint();
        }
    }

    public void remove(Selection old){
        if(drawables.remove(old)){
            repaint();
        }
    }

    public void print(){
        parent.print();
    }
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
                Iterator it = drawables.iterator();
                while(it.hasNext()){
                    Drawable current = (Drawable)it.next();
                    if(current instanceof DrawableSeismogram){
                        if(((DrawableSeismogram)current).getSeismogram() == seismos[i]){
                            it.remove();
                            repaint();
                        }
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

    public void drawSeismograms(Graphics2D g2, Dimension size){
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Float(0,0, size.width, size.height));
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds("test", g2);
        Rectangle2D topLeftFilled = new Rectangle2D.Float(0,0,0,(float)stringBounds.getHeight());
        for (int i = 0; i < drawables.size(); i++){
            Drawable current = (Drawable)drawables.get(i);
            current.draw(g2, size, currentTimeEvent, currentAmpEvent);
            if(current instanceof TimeAmpLabel){
                TimeAmpLabel taPlotter = (TimeAmpLabel)current;
                g2.setFont(DisplayUtils.MONOSPACED_FONT);
                FontMetrics monoMetrics = g2.getFontMetrics();
                stringBounds = monoMetrics.getStringBounds(taPlotter.getText(), g2);
                taPlotter.drawName(g2,(int)(size.width - stringBounds.getWidth()),
                                   size.height - 3);
                g2.setFont(DisplayUtils.DEFAULT_FONT);
            }else if(current instanceof NamedDrawable){
                Rectangle2D drawnSize = ((NamedDrawable)current).drawName(g2, 5, (int)topLeftFilled.getHeight());
                topLeftFilled.setRect(0,0,
                                      drawnSize.getWidth(),
                                      topLeftFilled.getHeight() + drawnSize.getHeight());
            }
        }
        if(getCurrentTimeFlag()){
            currentTimeFlag.draw(g2, size, currentTimeEvent, currentAmpEvent);
        }
    }

    private class PlotPainter extends JComponent{
        public void paintComponent(Graphics g){
            drawSeismograms((Graphics2D)g, getSize());
        }
    }


    public void addSoundPlay(){
        try{
            drawables.add(new SoundPlay(this, new SeismogramContainer(getSeismograms()[0])));
        }
        catch(NullPointerException e){
            System.out.println("Sample Rate cannot be calculated, so sound is not permitted.");
            e.printStackTrace();
        }
    }

    public void removeSoundPlay(){
        Iterator it = drawables.iterator();
        while(it.hasNext()){
            Drawable current = (Drawable)it.next();
            if(current instanceof SoundPlay){
                it.remove();
            }
        }
    }
    public final static int PREFERRED_HEIGHT = 150;

    public final static int PREFERRED_WIDTH = 250;

    private SeismogramDisplay parent;

    private LinkedList seismograms = new LinkedList();

    private LinkedList drawables =new LinkedList();

    private TimeConfig tc;

    private AmpConfig ac;

    private PlotPainter plotPainter = new PlotPainter();

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private ScaleBorder scale;

    private TimeScaleCalc timeScaleMap;

    private AmpScaleMapper ampScaleMap;

    private DataSetSeismogram[] seismogramArray;

    private CurrentTimeFlag currentTimeFlag = new CurrentTimeFlag();

    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}// BasicSeismogramDisplay
