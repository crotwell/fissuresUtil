package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.util.*;

import edu.sc.seis.fissuresUtil.display.drawable.DisplayRemover;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;

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
        int min = 10;
        int max = 100;
        scalingSlider = new JSlider(JSlider.VERTICAL, min, max, (max - min)/2);
        scaling = (max - min)/2;
        scalingSlider.setMajorTickSpacing(10);
        scalingSlider.setPaintTicks(true);
        scalingSlider.setBorder(BorderFactory.createRaisedBevelBorder());

        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(min), new JLabel("Normal"));
        labelTable.put(new Integer(max), new JLabel("Huge"));
        scalingSlider.setLabelTable(labelTable);
        scalingSlider.setPaintLabels(true);
        scalingSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent ce) {
                        scalingChanged(((JSlider)ce.getSource()).getValue());
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

    private void scalingChanged(double newScaling){
        scaling = newScaling;
        if(layout != null){
            layout.setScale(newScaling/10);
        }
    }

    public synchronized void add(DataSetSeismogram[] seismos){
        updating = true;
        if(tc == null){
            setTimeConfig(new BasicTimeConfig());
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
            drawable.add(new DrawableSeismogram(this, seismos[i]));
        }
        updating = false;
        if(displayRemover == null){
            displayRemover = new SeismogramDisplayRemovalBorder(this);
            Border etchedRemoval = BorderFactory.createCompoundBorder(etched,
                                                                      displayRemover);
            Border lowerScaleBorder = BorderFactory.createCompoundBorder(border,
                                                                         loweredBevel);
            setBorder(BorderFactory.createCompoundBorder(etchedRemoval,
                                                         lowerScaleBorder));
            painter = new DrawablePainter();
            add(painter, BorderLayout.CENTER);
            add(scalingSlider, BorderLayout.EAST);
            resize();
        }
        repaint();
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
        return drawableToDataSet(drawable);
    }


    public DrawableIterator iterator(Class drawableClass) {
        return new DrawableIterator(drawableClass, drawable);
    }

    private DataSetSeismogram[] drawableToDataSet(List drawable){
        DataSetSeismogram[] seis = new DataSetSeismogram[drawable.size()];
        Iterator it = drawable.iterator();
        int i = 0;
        while(it.hasNext()){
            DrawableSeismogram cur = (DrawableSeismogram)it.next();
            seis[i++] = cur.getSeismogram();
        }
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
        drawable.clear();
        displayRemover = null;
        setBorder(BorderFactory.createEmptyBorder());
        painter = null;
        super.removeAll();
    }

    public synchronized void remove(DataSetSeismogram[] seismos) {
        List removed = new ArrayList();
        for (int i = 0; i < seismos.length; i++){
            Iterator it = drawable.iterator();
            while(it.hasNext()){
                DrawableSeismogram current = (DrawableSeismogram)it.next();
                if(current.getSeismogram().equals(seismos[i])){
                    removed.add(current);
                }
            }
        }
        drawable.removeAll(removed);
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
    private class DrawablePainter extends JComponent{
        public void paintComponent(Graphics g){
            if(updating){
                return;
            }
            Graphics2D g2 = (Graphics2D)g;
            synchronized(this){
                Dimension size = getSize();
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
                    double neededYPos = midPoint - drawHeight/2;
                    g2.translate(0, neededYPos);
                    Dimension drawSize = new Dimension(width, drawHeight);
                    DrawableSeismogram cur = toDrawable(current.getSeis());
                    cur.draw(g2, drawSize, timeEvent, ampEvent);
                    g2.translate(0, -neededYPos);
                    cur.drawName(g2, 5, (int)(neededYPos + drawHeight/2));
                }
            }
        }

        public DrawableSeismogram toDrawable(DataSetSeismogram seis){
            Iterator it = drawable.iterator();
            DrawableSeismogram current = null;
            while(it.hasNext()){
                current = (DrawableSeismogram)it.next();
                if(current.getSeismogram().equals(seis)){
                    return current;
                }
            }
            return current;
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
        Rectangle newPainterBounds = new Rectangle(insets.left, insets.right,
                                                   d.width, d.height);
        if(painter != null) painter.setBounds(newPainterBounds);
        if(timeScaleMap != null) timeScaleMap.setTotalPixels(d.width - scalingSlider.getSize().width);
        if(distanceScaler != null) distanceScaler.setTotalPixels(d.height);
    }

    public void setCurrentTimeFlag(boolean visible){
        //TODO
    }

    public void setOriginalVisibility(boolean visible) {
        // TODO
    }

    public boolean getOriginalVisibility() {
        return true;
    }

    public void applyFilter(ColoredFilter filter) {
        // TODO
    }

    public void removeFilter(ColoredFilter filter){
        //TODO
    }

    public void print() {
        // TODO
    }

    public void setParticleAllowed(boolean allowed) {
        // TODO
    }

    private SeismogramDisplayRemovalBorder displayRemover;

    private List drawable = new ArrayList();

    private TimeConfig tc;

    private AmpConfig ac;

    private LayoutConfig layout;

    private AmpEvent ampEvent;

    private TimeEvent timeEvent;

    private LayoutEvent curLayoutEvent = LayoutEvent.EMPTY_EVENT;

    private TimeScaleCalc timeScaleMap;

    private DistanceScaleMapper distanceScaler;

    private ScaleBorder border;

    private boolean updating;

    private DrawablePainter painter;

    private JSlider scalingSlider;

    private double scaling;

    private Border etched  = BorderFactory.createEtchedBorder();

    private Border loweredBevel = BorderFactory.createLoweredBevelBorder();
}

