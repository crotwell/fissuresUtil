package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import edu.iris.Fissures.model.UnitRangeImpl;
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

public class RecordSectionDisplay extends SeismogramDisplay implements
        TimeListener, AmpListener, LayoutListener {

    public RecordSectionDisplay() {
        this(false);
    }

    public RecordSectionDisplay(boolean swapAxes) {
        distBorder = new DistanceBorder(this);
        timeBorder = new TimeBorder(this, TimeBorder.BOTTOM);
        add(timeBorder, BOTTOM_CENTER);
        add(distBorder, CENTER_LEFT);
        setLayout(new BasicLayoutConfig());
        setTimeConfig(new RelativeTimeConfig());
        setAmpConfig(new RMeanAmpConfig());
        seisToPixelMap = new HashMap();
        this.swapAxes = swapAxes;
    }

    public SeismogramDisplayProvider createCenter() {
        DrawablePainter painter = new DrawablePainter();
        painter.addMouseMotionListener(getMouseMotionForwarder());
        painter.addMouseListener(getMouseForwarder());
        return painter;
    }

    public void scalingChanged(double newScaling) {
        scaling = newScaling;
        if(layout != null) {
            layout.setScale(newScaling / 10);
        }
    }

    public UnitRangeImpl getDistance() {
        return getLayoutConfig().getLayout().getDistance();
    }

    public synchronized void add(DataSetSeismogram[] seismos) {
        tc.add(seismos);
        ac.add(seismos);
        for(int i = 0; i < seismos.length; i++) {
            if(!contains(seismos[i])) {
                drawables.add(new DrawableSeismogram(this,
                                                     seismos[i],
                                                     (Color)null));
            }
        }
        layout.add(seismos);
        checkDrawHeight = true;
        revalidate();
    }

    public void setDistBorder(DistanceBorder distanceBorder, int position) {
        if(this.distBorder != null) {
            this.remove(distBorder);
        }
        this.distBorder = distanceBorder;
        add(distBorder, position);
    }

    public void setTimeBorder(TimeBorder timeBorder, int position) {
        if(this.timeBorder != null) {
            this.remove(this.timeBorder);
        }
        this.timeBorder = timeBorder;
        add(timeBorder, position);
    }

    public void add(Drawable drawable) {
        if(!drawables.contains(drawable)) {
            drawables.add(drawable);
            repaint();
        }
    }

    public void remove(Drawable drawable) {
        drawables.remove(drawable);
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        Insets insets = getInsets();
        return getDrawables(e.getX() - insets.left, e.getY() - insets.top);
    }

    public void setTimeConfig(TimeConfig tc) {
        checkDrawHeight = true;
        if(this.tc != null) {
            this.tc.removeListener(this);
            this.tc.removeListener(ac);
            this.tc.remove(getSeismograms());
        }
        tc.add(getSeismograms());
        tc.addListener(this);
        tc.addListener(ac);
        this.tc = tc;
    }

    public TimeConfig getTimeConfig() {
        return tc;
    }

    public void setAmpConfig(AmpConfig ac) {
        checkDrawHeight = true;
        if(this.ac != null) {
            this.ac.removeListener(this);
            this.tc.removeListener(ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ac;
        if(tc != null) {
            tc.addListener(ac);
        }
        ac.addListener(this);
        ac.add(getSeismograms());
    }

    public void setGlobalizedAmpConfig(AmpConfig ac) {
        setAmpConfig(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac) {
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig() {
        return ac;
    }

    public LayoutConfig getLayoutConfig() {
        return layout;
    }

    public void setLayout(LayoutConfig layout) {
        if(this.layout != null) {
            this.layout.removeListener(this);
            this.layout.remove(getSeismograms());
        }
        if(getSeismograms().length > 0) {
            layout.add(getSeismograms());
        }
        layout.setScale(scaling / 10);
        layout.addListener(this);
        this.layout = layout;
    }

    public synchronized DataSetSeismogram[] getSeismograms() {
        return drawableToDataSet(drawables);
    }

    public DrawableIterator iterator(Class drawableClass) {
        return new DrawableIterator(drawableClass, drawables);
    }

    private DataSetSeismogram[] drawableToDataSet(List drawables) {
        List dataSetSeis = new ArrayList();
        Iterator it = new DrawableIterator(DrawableSeismogram.class, drawables);
        while(it.hasNext()) {
            dataSetSeis.add(((DrawableSeismogram)it.next()).getSeismogram());
        }
        DataSetSeismogram[] seis = new DataSetSeismogram[dataSetSeis.size()];
        dataSetSeis.toArray(seis);
        return seis;
    }

    public void reset() {
        checkDrawHeight = true;
        layout.reset();
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismos) {
        checkDrawHeight = true;
        tc.reset(seismos);
        ac.reset(seismos);
    }

    public synchronized void clear() {
        remove(getSeismograms());
        reset();
    }

    public synchronized void remove(DataSetSeismogram[] seismos) {
        checkDrawHeight = true;
        List removed = new ArrayList();
        for(int i = 0; i < seismos.length; i++) {
            Iterator it = new DrawableIterator(DrawableSeismogram.class,
                                               drawables);
            while(it.hasNext()) {
                DrawableSeismogram current = (DrawableSeismogram)it.next();
                if(current.getSeismogram().equals(seismos[i])) {
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
        for(int i = 0; i < seismos.length; i++) {
            if(seismos[i].equals(seismo)) {
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

    public void storePixels(LayoutData layoutData,
                            double topLeftX,
                            double topLeftY,
                            double bottomRightX,
                            double bottomRightY) {
        double[] pixelArray = {topLeftX, topLeftY, bottomRightX, bottomRightY};
        seisToPixelMap.put(layoutData.getSeis().getRequestFilter().channel_id,
                           pixelArray);
    }

    public HashMap getPixelMap() {
        return seisToPixelMap;
    }

    public boolean getSwapAxes() {
        return this.swapAxes;
    }

    public int getMinSeisPixelHeight() {
        return minSeisPixelHeight;
    }

    public void setMinSeisPixelHeight(int minPixelHeight) {
        checkDrawHeight = true;
        this.minSeisPixelHeight = minPixelHeight;
    }

    public void checkSeismogramHeight(Dimension size) {
        Iterator it = curLayoutEvent.iterator();
        int height = size.height;
        if(it.hasNext()) {
            LayoutData current = (LayoutData)it.next();
            double drawHeight = (current.getEnd() - current.getStart())
                    * height;
            // If the draw height is less than minPixelHeight, change the scale
            // so that it is
            if(drawHeight < minSeisPixelHeight && checkDrawHeight) {
                if(drawHeight == 0) {
                    drawHeight = 1;
                    logger.warn("pixel height from LayoutConfig is 0, should always be >0");
                }
                double percentIncreaseNeeded = minSeisPixelHeight / drawHeight;
                if(scaler != null) {
                    scaler.increaseScale(percentIncreaseNeeded);
                } else {
                    scalingChanged(scaling * percentIncreaseNeeded);
                }
                checkDrawHeight = false;
            }
        }
    }

    public void drawSeismograms(Graphics2D g2, Dimension size) {
        synchronized(this) {
            checkSeismogramHeight(size);
            int width = size.width;
            int height = size.height;
            if(swapAxes) {
                AffineTransform at = new AffineTransform();
                at.translate(width / 2, height / 2);
                at.rotate(-Math.PI / 2);
                at.translate(-height / 2, -width / 2);
                g2.transform(at);
                int temp = width;
                width = height;
                height = temp;
            }
            g2.setColor(Color.WHITE);
            g2.fill(new Rectangle2D.Float(0, 0, width, height));
            Iterator it = curLayoutEvent.iterator();
            while(it.hasNext()) {
                LayoutData current = (LayoutData)it.next();
                double curEnd = current.getEnd();
                double curStart = current.getStart();
                double drawHeight = (curEnd - curStart) * height;
                double midPoint = curStart * height + drawHeight / 2;
                double topLeftY = Math.abs(curStart * height);
                double bottomRightY = curEnd * height;
                double distBorderWidth = distBorder.getWidth();
                storePixels(current, distBorderWidth, topLeftY, width
                        + distBorderWidth, bottomRightY);
                double neededYPos = midPoint - drawHeight / 2;
                if(neededYPos < 0 && !swapAxes) {
                    neededYPos = 0;
                }
                g2.translate(0, neededYPos);
                if(PDF) {
                    g2.scale(.25, .25);
                }
                Dimension drawSize = new Dimension((PDF ? 4 : 1) * width,
                                                   (int)((PDF ? 4.0 : 1.0) * drawHeight));
                DrawableSeismogram cur = toDrawable(current.getSeis());
                cur.draw(g2, drawSize, timeEvent, ampEvent);
                if(PDF) {
                    g2.scale(4.0, 4.0);
                }
                g2.translate(0, -neededYPos);
                if(drawNamesForNamedDrawables) {
                    cur.drawName(g2, 5, (int)(neededYPos + drawHeight / 2));
                }
                int[] yPos = {(int)neededYPos, (int)(neededYPos + drawHeight)};
                drawablePositions.put(cur, yPos);
            }
            it = drawables.iterator();
            while(it.hasNext()) {
                Drawable current = (Drawable)it.next();
                if(!(current instanceof DrawableSeismogram)) {
                    current.draw(g2, size, timeEvent, ampEvent);
                }
            }
            if(getCurrentTimeFlag()) {
                currentTimeFlag.draw(g2, size, timeEvent, ampEvent);
            }
        }
    }

    public DrawableIterator getDrawables(int x, int y) {
        Iterator it = drawablePositions.keySet().iterator();
        List drawablesIntersected = new ArrayList();
        while(it.hasNext()) {
            Object cur = it.next();
            int[] yPositions = (int[])drawablePositions.get(cur);
            if(yPositions[0] <= y && yPositions[1] >= y) {
                drawablesIntersected.add(cur);
            }
        }
        return new DrawableIterator(Drawable.class, drawablesIntersected);
    }

    private Map drawablePositions = new HashMap();

    public DrawableSeismogram toDrawable(DataSetSeismogram seis) {
        Iterator it = new DrawableIterator(DrawableSeismogram.class, drawables);
        DrawableSeismogram current = null;
        while(it.hasNext()) {
            current = (DrawableSeismogram)it.next();
            if(current.getSeismogram().equals(seis)) {
                return current;
            }
        }
        return current;
    }

    private class DrawablePainter extends SeismogramDisplayProvider {

        public SeismogramDisplay provide() {
            return RecordSectionDisplay.this;
        }

        public void paintComponent(Graphics g) {
            drawSeismograms((Graphics2D)g, getSize());
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

    public void setLayoutScaler(LayoutScaler scaler) {
        this.scaler = scaler;
    }

    private boolean swapAxes = false;

    private List drawables = new ArrayList();

    private TimeConfig tc;

    private AmpConfig ac;

    private DistanceBorder distBorder;

    private TimeBorder timeBorder;

    private HashMap seisToPixelMap;

    private LayoutConfig layout;

    private AmpEvent ampEvent;

    private TimeEvent timeEvent;

    private LayoutEvent curLayoutEvent = LayoutEvent.EMPTY_EVENT;

    private double scaling = LayoutScaler.INITIAL_SCALE;

    private int minSeisPixelHeight = 40;

    private CurrentTimeFlag currentTimeFlag = new CurrentTimeFlag();

    private LayoutScaler scaler = null;

    private boolean checkDrawHeight = false;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RecordSectionDisplay.class);
}