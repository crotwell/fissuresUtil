package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import edu.sc.seis.fissuresUtil.display.borders.DistanceBorder;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.display.drawable.CurrentTimeFlag;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.AmpListener;
import edu.sc.seis.fissuresUtil.display.registrar.BasicLayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutData;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutEvent;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutListener;
import edu.sc.seis.fissuresUtil.display.registrar.LayoutScaler;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RelativeTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeListener;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class RecordSectionDisplay extends SeismogramDisplay implements TimeListener, AmpListener, LayoutListener{
    public RecordSectionDisplay(){
        distBorder=new DistanceBorder(this);
        add(new TimeBorder(this, TimeBorder.BOTTOM), BOTTOM_CENTER);
        add(distBorder, CENTER_LEFT);
        setLayout(getNewLayoutConfig());
        setTimeConfig(new RelativeTimeConfig());
        setAmpConfig(new RMeanAmpConfig());
        seisToPixelMap = new HashMap();
    }

    public RecordSectionDisplay(DataSetSeismogram[] seismos, TimeConfig tc,
                                AmpConfig ac){
        this();
        setTimeConfig(tc);
        setAmpConfig(ac);
        add(seismos);
    }

    public SeismogramDisplayProvider createCenter() {
        DrawablePainter painter = new DrawablePainter();
        painter.addMouseMotionListener(getMouseMotionForwarder());
        painter.addMouseListener(getMouseForwarder());
        return painter;
    }

    public void scalingChanged(double newScaling){
        scaling = newScaling;
        if(layout != null){
            layout.setScale(newScaling/10);
        }
    }

    public synchronized void add(DataSetSeismogram[] seismos){
        tc.add(seismos);
        ac.add(seismos);
        for (int i = 0; i < seismos.length; i++){
            if(!contains(seismos[i])){
                drawables.add(new DrawableSeismogram(this, seismos[i], (Color)null));
            }
        }
        layout.add(seismos);      
        checkDrawHeight = true;
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

    public LayoutConfig getLayoutConfig(){ return layout; }

    protected LayoutConfig getNewLayoutConfig(){
        return new BasicLayoutConfig();
    }

    public void setLayout(LayoutConfig layout){
        if(this.layout != null){
            this.layout.removeListener(this);
            this.layout.remove(getSeismograms());
        }
        if(getSeismograms().length > 0){
            layout.add(getSeismograms());
        }
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
        layout.reset();
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismos) {
        tc.reset(seismos);
        ac.reset(seismos);
    }

    public synchronized void clear() {
        remove(getSeismograms());
        reset();
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
    public void storePixels(LayoutData layoutData, double topLeftX,double topLeftY,double bottomRightX,double bottomRightY){
        double[] pixelArray={topLeftX,topLeftY,bottomRightX,bottomRightY};
        seisToPixelMap.put(layoutData.getSeis().getRequestFilter().channel_id,pixelArray);
    }
    public HashMap getPixelMap() {
        return seisToPixelMap;
    }
    public void drawSeismograms(Graphics2D g2, Dimension size){
        synchronized(this){
            int width = size.width;
            int height = size.height;
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Float(0,0, width, height));
            Iterator it = curLayoutEvent.iterator();
            while(it.hasNext()){
                LayoutData current = (LayoutData)it.next();
                double midPoint = current.getStart() * height + ((current.getEnd() - current.getStart()) * height)/2;
                double drawHeight = (current.getEnd() - current.getStart())*height;
                double topLeftY=Math.abs(current.getStart()*height);
                double bottomRightY=current.getEnd()*height;
                double distBorderWidth=distBorder.getWidth();
                storePixels(current,distBorderWidth,topLeftY,width+distBorderWidth,bottomRightY);
                //If the draw height is less than 40, change the scale so that
                //it is
                if(drawHeight < 40 && checkDrawHeight){
                    if(drawHeight == 0) drawHeight = 1;
                    double percentIncreaseNeeded = 40/drawHeight;
                    if(scaler != null){
                        scaler.increaseScale(percentIncreaseNeeded);
                    }
                    checkDrawHeight = false;
                }
                double neededYPos = midPoint - drawHeight/2;
                if(neededYPos < 0){
                    neededYPos = 0;
                }
                g2.translate(0, neededYPos);
                Dimension drawSize = new Dimension(width, (int)drawHeight);
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


    private class DrawablePainter extends SeismogramDisplayProvider{
        public SeismogramDisplay provide(){ return RecordSectionDisplay.this; }

        public void paintComponent(Graphics g){
            drawSeismograms((Graphics2D)g,getSize());
        }
    }

    public void print() {
        JOptionPane.showMessageDialog(this,
                                      "Sorry!  Record section output to PDF is not available in this version of GEE.\nThis feature will be added in a future release.",
                                      "Not available in this version",
                                      JOptionPane.INFORMATION_MESSAGE);
        // TODO
    }

    public void setParticleAllowed(boolean allowed) {
        // TODO
    }

    public void setLayoutScaler(LayoutScaler scaler){ this.scaler = scaler; }

    private List drawables = new ArrayList();

    private TimeConfig tc;

    private AmpConfig ac;
    
    private DistanceBorder distBorder;
    
    private HashMap seisToPixelMap;

    private LayoutConfig layout;

    private AmpEvent ampEvent;

    private TimeEvent timeEvent;

    private LayoutEvent curLayoutEvent = LayoutEvent.EMPTY_EVENT;

    private double scaling = LayoutScaler.INITIAL_SCALE;

    private CurrentTimeFlag currentTimeFlag = new CurrentTimeFlag();

    private LayoutScaler scaler = null;

    private boolean checkDrawHeight = false;
}
