package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.util.*;

import edu.sc.seis.fissuresUtil.display.drawable.CurrentTimeFlag;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RecordSectionDisplay extends SeismogramDisplay implements TimeListener, AmpListener, LayoutListener{

    public RecordSectionDisplay(){
        setLayout(new BorderLayout());
        addMouseMotionListener(SeismogramDisplay.getMouseMotionForwarder());
        addMouseListener(SeismogramDisplay.getMouseForwarder());
        border = new ScaleBorder();
        addComponentListener(new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        resize();
                    }
                });
    }

    public RecordSectionDisplay(DataSetSeismogram[] seismos, TimeConfig tc,
                                AmpConfig ac){
        this();
        setTimeConfig(tc);
        setAmpConfig(ac);
        add(seismos);
    }

    public void scalingChanged(double newScaling){
        scaling = newScaling;
        if(layout != null){
            layout.setScale(newScaling/10);
        }
    }

    public synchronized void add(DataSetSeismogram[] seismos){
        if(tc == null){
            setTimeConfig(new RelativeTimeConfig());
        }
        tc.add(seismos);
        if(ac == null){
            setAmpConfig(new RMeanAmpConfig());
        }
        ac.add(seismos);
        if(layout == null){
            setLayout(new BasicLayoutConfig());
        }
        layout.add(seismos);
        for (int i = 0; i < seismos.length; i++){
            if(!contains(seismos[i])){
                drawables.add(new DrawableSeismogram(this, seismos[i], (Color)null));
            }
        }
        if(displayRemover == null){
            displayRemover = new SeismogramDisplayRemovalBorder(this);
            Border etchedRemoval = BorderFactory.createCompoundBorder(etched,
                                                                      displayRemover);
            Border lowerScaleBorder = BorderFactory.createCompoundBorder(border,
                                                                         loweredBevel);
            setBorder(BorderFactory.createCompoundBorder(etchedRemoval,
                                                         lowerScaleBorder));
            painter = new DrawablePainter();
            add(painter);
            resize();
        }
        revalidate();
    }

    public void add(Drawable drawable){
        if(!drawables.contains(drawable)){
            drawables.add(drawable);
            repaint();
        }
    }

    public void remove(Drawable drawable){
        drawables.remove(drawable);
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        Insets insets = getInsets();
        return getDrawables(e.getX() - insets.left,
                            e.getY() - insets.top);
    }

    public void setTimeConfig(TimeConfig tc) {
        if(this.tc != null){
            this.tc.removeListener(this);
            this.tc.removeListener(ac);
            this.tc.remove(getSeismograms());
        }
        timeScaleMap = new TimeScaleCalc(getSize().width, tc);
        border.setBottomScaleMapper(timeScaleMap);
        tc.add(getSeismograms());
        tc.addListener(this);
        tc.addListener(ac);
        resize();
        this.tc = tc;
    }

    public TimeConfig getTimeConfig(){ return tc; }

    public void setAmpConfig(AmpConfig ac){
        if(this.ac != null){
            this.ac.removeListener(this);
            this.tc.removeListener(ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ac;
        if(tc != null){
            tc.addListener(ac);
        }
        ac.addListener(this);
        ac.add(getSeismograms());
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){
        setAmpConfig(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig(){ return ac; }

    public void setLayout(LayoutConfig layout){
        if(this.layout != null){
            this.layout.removeListener(this);
            this.layout.remove(getSeismograms());
        }
        if(getSeismograms().length > 0){
            layout.add(getSeismograms());
        }
        distanceScaler= new DistanceScaleMapper(getSize().height, 4, layout);
        border.setLeftScaleMapper(distanceScaler);
        layout.setScale(scaling/10);
        layout.addListener(this);
        this.layout = layout;
    }

    public synchronized DataSetSeismogram[] getSeismograms() {
        return drawableToDataSet(drawables);
    }


    public DrawableIterator iterator(Class drawableClass) {
        return new DrawableIterator(drawableClass, drawables);
    }

    private DataSetSeismogram[] drawableToDataSet(List drawables){
        List dataSetSeis = new ArrayList();
        Iterator it = new DrawableIterator(DrawableSeismogram.class, drawables);
        while(it.hasNext()){
            dataSetSeis.add(((DrawableSeismogram)it.next()).getSeismogram());
        }
        DataSetSeismogram[] seis = new DataSetSeismogram[dataSetSeis.size()];
        dataSetSeis.toArray(seis);
        return seis;
    }

    public void reset() {
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismos) {
        tc.reset(seismos);
        ac.reset(seismos);
    }

    public synchronized void clear() {
        removeAll();
    }

    public synchronized void removeAll(){
        layout = null;
        tc = null;
        ac = null;
        drawables.clear();
        displayRemover = null;
        setBorder(BorderFactory.createEmptyBorder());
        painter = null;
        super.removeAll();
    }

    public synchronized void remove(DataSetSeismogram[] seismos) {
        List removed = new ArrayList();
        for (int i = 0; i < seismos.length; i++){
            Iterator it = new DrawableIterator(DrawableSeismogram.class,
                                               drawables);
            while(it.hasNext()){
                DrawableSeismogram current = (DrawableSeismogram)it.next();
                if(current.getSeismogram().equals(seismos[i])){
                    removed.add(current);
                }
            }
        }
        drawables.removeAll(removed);
        DataSetSeismogram[] removedSeis = drawableToDataSet(removed);
        tc.remove(removedSeis);
        ac.remove(removedSeis);
        layout.remove(removedSeis);
    }

    public synchronized boolean contains(DataSetSeismogram seismo) {
        DataSetSeismogram[] seismos = getSeismograms();
        for (int i = 0; i < seismos.length; i++){
            if(seismos[i].equals(seismo)){
                return true;
            }
        }
        return false;
    }

    public void updateTime(TimeEvent event) {
        timeEvent = event;
        repaint();
    }

    public void updateAmp(AmpEvent event) {
        ampEvent = event;
        repaint();
    }

    public void updateLayout(LayoutEvent event) {
        curLayoutEvent = event;
        repaint();
    }

    public void drawSeismograms(Graphics2D g2, Dimension size){
        synchronized(this){
            int width = size.width;
            int height = size.height;
            if(displayRemover != null){
                g2.setColor(Color.WHITE);
                g2.fill(new Rectangle2D.Float(0,0, width, height));
            }
            Iterator it = curLayoutEvent.iterator();
            while(it.hasNext()){
                LayoutData current = (LayoutData)it.next();
                double midPoint = current.getStart() * height + ((current.getEnd() - current.getStart()) * height)/2;
                int drawHeight = (int)((current.getEnd() - current.getStart())*height);
                if(drawHeight < 20){
                    drawHeight = 20;
                }
                double neededYPos = midPoint - drawHeight/2;
                if(neededYPos < 0){
                    neededYPos = 0;
                }
                g2.translate(0, neededYPos);
                Dimension drawSize = new Dimension(width, drawHeight);
                DrawableSeismogram cur = toDrawable(current.getSeis());
                cur.draw(g2, drawSize, timeEvent, ampEvent);
                g2.translate(0, -neededYPos);
                cur.drawName(g2, 5, (int)(neededYPos + drawHeight/2));
                int[] yPos = {(int)neededYPos, (int)(neededYPos + drawHeight)};
                drawablePositions.put(cur, yPos);
            }
            it = drawables.iterator();
            while(it.hasNext()){
                Drawable current = (Drawable)it.next();
                if(!(current instanceof DrawableSeismogram)){
                    current.draw(g2, size, timeEvent, ampEvent);
                }
            }
            if(getCurrentTimeFlag()){
                currentTimeFlag.draw(g2, size, timeEvent, ampEvent);
            }
        }
    }

    public DrawableIterator getDrawables(int x, int y){
        Iterator it = drawablePositions.keySet().iterator();
        List drawablesIntersected = new ArrayList();
        while(it.hasNext()){
            Object cur = it.next();
            int[] yPositions = (int[])drawablePositions.get(cur);
            if(yPositions[0] <= y && yPositions[1] >= y){
                drawablesIntersected.add(cur);
            }
        }
        return new DrawableIterator(Drawable.class, drawablesIntersected);
    }

    private Map drawablePositions = new HashMap();

    public DrawableSeismogram toDrawable(DataSetSeismogram seis){
        Iterator it = new DrawableIterator(DrawableSeismogram.class,
                                           drawables);
        DrawableSeismogram current = null;
        while(it.hasNext()){
            current = (DrawableSeismogram)it.next();
            if(current.getSeismogram().equals(seis)){
                return current;
            }
        }
        return current;
    }


    private class DrawablePainter extends JComponent{
        public void paintComponent(Graphics g){
            drawSeismograms((Graphics2D)g,getSize());
        }

    }

    protected synchronized void resize(){
        Insets insets = getInsets();
        Dimension d = getSize();
        d = new Dimension(d.width - insets.left - insets.right,
                          d.height - insets.top - insets.bottom);
        if(d.height <= 0 || d.width <= 0){
            return;
        }
        if(timeScaleMap != null) timeScaleMap.setTotalPixels(d.width);
        if(distanceScaler != null) distanceScaler.setTotalPixels(d.height);
    }

    public void print() {
        // TODO
    }

    public void setParticleAllowed(boolean allowed) {
        // TODO
    }

    private SeismogramDisplayRemovalBorder displayRemover;

    private List drawables = new ArrayList();

    private TimeConfig tc;

    private AmpConfig ac;

    private LayoutConfig layout;

    private AmpEvent ampEvent;

    private TimeEvent timeEvent;

    private LayoutEvent curLayoutEvent = LayoutEvent.EMPTY_EVENT;

    private TimeScaleCalc timeScaleMap;

    private DistanceScaleMapper distanceScaler;

    private ScaleBorder border;

    private DrawablePainter painter;

    private double scaling = LayoutScaler.INITIAL_SCALE;

    private CurrentTimeFlag currentTimeFlag = new CurrentTimeFlag();

    private Border etched  = BorderFactory.createEtchedBorder();

    private Border loweredBevel = BorderFactory.createLoweredBevelBorder();
}
