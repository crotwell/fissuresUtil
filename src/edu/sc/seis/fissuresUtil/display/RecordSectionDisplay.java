package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.util.*;

import edu.sc.seis.fissuresUtil.display.drawable.DisplayRemove;
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

public class RecordSectionDisplay extends SeismogramDisplay implements ConfigListener, LayoutListener{

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


    private void scalingChanged(int newScaling){
        scaling = newScaling;
        if(layout != null){
            layout.setScale(newScaling/10);
        }
    }


    public RecordSectionDisplay(DataSetSeismogram[] seismos, TimeConfig tc,
                                AmpConfig ac){
        this();
        setRegistrar(new Registrar(seismos, tc, ac));
        add(seismos);
    }

    public synchronized void add(DataSetSeismogram[] seismos){
        updating = true;
        if(registrar == null){
            setRegistrar(new Registrar(seismos,
                                       new BasicTimeConfig(),
                                       new IndividualizedAmpConfig(new RMeanAmpConfig())));
        }else{
            registrar.add(seismos);
        }
        if(layout == null){
            setLayout(new BasicLayoutConfig(seismos));
        }else{
            layout.add(seismos);
        }
        for (int i = 0; i < seismos.length; i++){
            dssPlotter.put(seismos[i], new DrawableSeismogram(this, seismos[i]));
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
            painter = new PlotPainter();
            add(painter, BorderLayout.CENTER);
            add(scalingSlider, BorderLayout.EAST);
            resize();
        }
        repaint();
    }

    public void setRegistrar(Registrar registrar) {
        if(this.registrar != null){
            this.registrar.removeListener(this);
            this.registrar.remove(getSeismograms());
        }
        timeScaleMap = new TimeScaleCalc(getSize().width, registrar);
        border.setBottomScaleMapper(timeScaleMap);
        registrar.add(getSeismograms());
        registrar.addListener(this);
        this.registrar = registrar;
    }

    public Registrar getRegistrar() {
        return registrar;
    }

    public void setTimeConfig(TimeConfig tc){
        if(registrar != null){
            registrar.setTimeConfig(tc);
        }else{
            setRegistrar(new Registrar(getSeismograms(), tc, new RMeanAmpConfig()));
        }
    }

    public TimeConfig getTimeConfig(){ return registrar.getTimeConfig(); }

    public void setAmpConfig(AmpConfig ac){
        if(registrar != null){
            registrar.setAmpConfig(ac);
        }else{
            setRegistrar(new Registrar(getSeismograms(), new BasicTimeConfig(), ac));
        }
    }

    public void setGlobalizedAmpConfig(AmpConfig ac){
        setAmpConfig(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig(){ return registrar.getAmpConfig(); }

    public void setLayout(LayoutConfig layout){
        if(this.layout != null){
            this.layout.removeListener(this);
            this.layout.remove(getSeismograms());
        }
        layout.add(getSeismograms());
        distanceScaler= new DistanceScaleMapper(getSize().height, 4, layout);
        border.setLeftScaleMapper(distanceScaler);
        scalingChanged(scaling);
        layout.addListener(this);
        this.layout = layout;
    }

    public synchronized DataSetSeismogram[] getSeismograms() {
        return (DataSetSeismogram[])dssPlotter.keySet().toArray(new DataSetSeismogram[dssPlotter.keySet().size()]);
    }

    public void reset() {
        registrar.reset();
    }

    public void reset(DataSetSeismogram[] seismos) {
        registrar.reset(seismos);
    }

    public synchronized void clear() {
        removeAll();
    }

    public synchronized void removeAll(){
        layout = null;
        registrar = null;
        dssPlotter.clear();
        displayRemover = null;
        setBorder(BorderFactory.createEmptyBorder());
        painter = null;
        super.removeAll();
    }

    public synchronized void remove(DataSetSeismogram[] seismos) {
        List removed = new ArrayList();
        for (int i = 0; i < seismos.length; i++){
            Iterator it = dssPlotter.keySet().iterator();
            while(it.hasNext()){
                DataSetSeismogram current = (DataSetSeismogram)it.next();
                if(current.equals(seismos[i])){
                    System.out.println("removing " + current);
                    removed.add(current);
                }
            }
        }
        Iterator it = removed.iterator();
        while(it.hasNext()){
            dssPlotter.remove(it.next());
        }
        DataSetSeismogram[] removedSeis = new DataSetSeismogram[removed.size()];
        removed.toArray(removedSeis);
        registrar.remove(removedSeis);
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

    public void update(ConfigEvent event) {
        curTimeEvent = event.getTimeEvent();
        curAmpEvent = event.getAmpEvent();
        repaint();
    }

    public void updateTime(TimeEvent event) {
        curTimeEvent = event;
        repaint();
    }

    public void updateAmp(AmpEvent event) {
        curAmpEvent = event;
        repaint();
    }

    public void updateLayout(LayoutEvent event) {
        curLayoutEvent = event;
        repaint();
    }
    private class PlotPainter extends JComponent{
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
                    int drawHeight = (int)((current.getEnd() - current.getStart())*height);// * scaling);
                    double neededYPos = midPoint - drawHeight/2;
                    g2.translate(0, neededYPos);
                    Dimension drawSize = new Dimension(width, drawHeight);
                    DrawableSeismogram cur = (DrawableSeismogram)dssPlotter.get(current.getSeis());
                    cur.draw(g2, drawSize, curTimeEvent, curAmpEvent);
                    g2.translate(0, -neededYPos);
                    cur.drawName(g2, 5, (int)(neededYPos + drawHeight/2));
                }
            }
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

    private Map dssPlotter = new HashMap();

    private Registrar registrar;

    private LayoutConfig layout;

    private AmpEvent curAmpEvent;

    private TimeEvent curTimeEvent;

    private LayoutEvent curLayoutEvent = LayoutEvent.EMPTY_EVENT;

    private TimeScaleCalc timeScaleMap;

    private DistanceScaleMapper distanceScaler;

    private ScaleBorder border;

    private boolean updating;

    private PlotPainter painter;

    private JSlider scalingSlider;

    private int scaling;

    private Border etched  = BorderFactory.createEtchedBorder();

    private Border loweredBevel = BorderFactory.createLoweredBevelBorder();
}

