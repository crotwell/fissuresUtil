package edu.sc.seis.fissuresUtil.display;
import java.awt.*;
import java.awt.event.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.JComponent;
import org.apache.log4j.Category;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;

/**
 * ParticleMotionView.java
 *
 *
 * Created: Tue Jun 11 15:14:17 20022002-07-05 12:49:37,661 DEBUG main vsnexplorer.CommonAccess - Inactive task: EQexplorerMode
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionView extends JComponent{

    public ParticleMotionView(ParticleMotionDisplay particleMotionDisplay) {
        this.particleMotionDisplay = particleMotionDisplay;
        addListeners();
    }

    public void addListeners() {
        this.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        int clickCount = 0;
                        if(zoomIn)  {
                            clickCount = 1;
                        }
                        if(zoomOut) {
                            clickCount = 2;
                        }
                        zoomInParticleMotionDisplay(clickCount, me.getX(), me.getY());
                    }
                });
        this.addComponentListener(new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        resize();
                    }
                    public void componentShown(ComponentEvent e) {
                        resize();
                    }
                });
    }

    public synchronized void resize() {
        setSize(super.getSize());
        recalculateValues = true;
        repaint();
    }

    public synchronized void zoomInParticleMotionDisplay(int clickCount, int mx, int my) {
        double hmin = hunitRangeImpl.getMinValue();
        double hmax = hunitRangeImpl.getMaxValue();
        double vmin = vunitRangeImpl.getMinValue();
        double vmax = vunitRangeImpl.getMaxValue();
        if(hmin > hmax) { double temp = hmax; hmax = hmin; hmin = temp;}
        if(vmin > vmax) { double temp = vmax; vmax = vmin; vmin = temp;}
        Insets insets = getInsets();
        double width = getSize().getWidth() - insets.left - insets.right;
        double height = getSize().getHeight() - insets.top - insets.bottom;
        int centerx, centery;
        if(clickCount == 1) {
            centerx = (int)((hmax - hmin) / 4);
            centery = (int)((vmax - vmin) / 4);
        } else {
            centerx = (int)((hmax - hmin) / 4);
            centery = (int)((hmax - hmin) / 4);
        }
        int xone = (int)(((hmax - hmin)/width * mx) + hmin);
        int yone = (int)(((vmin - vmax)/height * my) + vmax);
        if(xone < 0) centerx = -centerx;
        if(yone < 0) centery = -centery;
        int xa, xs, ya, ys;
        if(clickCount == 1) {

            xa = xone - centerx;
            xs = xone + centerx;
            ya = yone - centery;
            ys = yone + centery;
        } else {
            if(centerx < 0) centerx = -centerx;
            if(centery < 0) centery = -centery;
            xa = (int)hmin - centerx;
            xs = (int)hmax + centerx;
            ya = (int)vmin - centery;
            ys = (int)vmax + centery;
            if((xs - xa) < 50){ xs = xs + 50; xa = xa - 50;}
            if((ys - ya) < 50) { ys = ys + 50; ya = ya - 50;}
        }
        if(xa > xs) { int temp = xs; xs = xa; xa = temp;}
        if(ya > ys) {int temp = ys; ys = ya; ya = temp;}
        particleMotionDisplay.updateHorizontalAmpScale(new UnitRangeImpl(xa, xs, UnitImpl.COUNT));
        particleMotionDisplay.updateVerticalAmpScale(new UnitRangeImpl(ya, ys, UnitImpl.COUNT));
        vunitRangeImpl = new UnitRangeImpl(ya, ys, UnitImpl.COUNT);
        hunitRangeImpl = new UnitRangeImpl(xa, xs, UnitImpl.COUNT);
        particleMotionDisplay.shaleAmp(0, (ys-ya));
    }

    public synchronized void paintComponent(Graphics g) {
        if(displayKeys.size() == 0) return;
        Graphics2D graphics2D = (Graphics2D)g;
        vunitRangeImpl = new UnitRangeImpl(getMinVerticalAmplitude(),
                                           getMaxVerticalAmplitude(),
                                           UnitImpl.COUNT);
        hunitRangeImpl = new UnitRangeImpl(getMinHorizontalAmplitude(),
                                           getMaxHorizontalAmplitude(),
                                           UnitImpl.COUNT);
        //first draw the azimuth if one of the display is horizontal plane
        for(int counter = 0; counter < displays.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            drawAzimuth(particleMotion, graphics2D);
        }
        for(int counter = 0; counter < displays.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            if(particleMotion.isSelected()) continue;
            drawParticleMotion(particleMotion, graphics2D);
        }//end of for
        for(int counter = 0; counter < displays.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            if(particleMotion.isSelected()) {
                particleMotion.setSelected(false);
                drawParticleMotion(particleMotion, g);
            }
        }
        //graphics2D.setStroke(new BasicStroke(1.0f));//TODO figure out what this was for
    }

    public synchronized void drawAzimuth(ParticleMotion particleMotion, Graphics2D graphics2D) {
        if(!particleMotion.isHorizontalPlane()) return;
        Shape sector = getSectorShape();
        graphics2D.setColor(new Color(100, 160, 140));
        graphics2D.fill(sector);
        graphics2D.draw(sector);
        graphics2D.setStroke(new BasicStroke(2.0f));
        graphics2D.setColor(Color.green);
        Shape azimuth = getAzimuthPath();
        graphics2D.draw(azimuth);
        graphics2D.setStroke(new BasicStroke(1.0f));
    }

    public synchronized void drawLabels(ParticleMotion particleMotion, Graphics2D graphics2D) {
        Color color = new Color(0, 0, 0, 128);
        graphics2D.setColor(color);
        java.awt.Dimension dimension = getSize();
        float fontSize = dimension.width / 20;
        if(fontSize < 4) fontSize = 4;
        else if(fontSize > 32) fontSize = 32;
        Font font = new Font("serif", Font.BOLD, (int)fontSize);
        graphics2D.setFont(font);
        String labelStr = new String();
        labelStr = particleMotion.hseis.toString();
        int x = (dimension.width - (int)(labelStr.length()*fontSize)) / 2  - getInsets().left - getInsets().right;
        int y = dimension.height  - 4;
        graphics2D.drawString(labelStr, x, y);
        labelStr = particleMotion.vseis.toString();
        x = font.getSize();
        y = (dimension.height - (int)(labelStr.length()*fontSize)) / 2  -  getInsets().top - getInsets().bottom;
        //get the original AffineTransform
        AffineTransform oldTransform = graphics2D.getTransform();
        AffineTransform ct =  AffineTransform.getTranslateInstance(x, y);
        graphics2D.transform(ct);
        graphics2D.transform(AffineTransform.getRotateInstance(Math.PI/2));
        graphics2D.drawString(labelStr, 0, 0);
        //restore the original AffineTransform
        graphics2D.setTransform(oldTransform);
    }

    public void drawTitles(LocalSeismogramImpl hseis, LocalSeismogramImpl vseis) {
        particleMotionDisplay.setHorizontalTitle(hseis.getName());
        particleMotionDisplay.setVerticalTitle(vseis.getName());
    }

    public synchronized void drawParticleMotion(ParticleMotion particleMotion, Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        if(!recalculateValues) {
            recalculateValues = false;
            graphics2D.draw(particleMotion.getShape());
            return;
        }
        Dimension dimension = super.getSize();
        LocalSeismogramImpl hseis = particleMotion.getLocalHSeis();
        LocalSeismogramImpl vseis = particleMotion.getLocalVSeis();
        if(hseis == null || vseis == null){
            return;
        }
        Color color = particleMotion.getColor();
        if(color == null) {
            color = COLORS[RGBCOLOR];
            particleMotion.setColor(color);
            RGBCOLOR++;
            if(RGBCOLOR == COLORS.length) RGBCOLOR = 0;
        }
        graphics2D.setColor(color);
        try {
            MicroSecondTimeRange microSecondTimeRange = null;
            if(particleMotion.registrar == null) {
                microSecondTimeRange = new MicroSecondTimeRange(new MicroSecondDate(hseis.getBeginTime()),
                                                                new MicroSecondDate(hseis.getEndTime()));
            } else {

                microSecondTimeRange = particleMotion.getTimeRange();
            }
            int[][] hPixels =  SimplePlotUtil.getPlottableSimple(hseis,
                                                                 hunitRangeImpl,
                                                                 microSecondTimeRange,
                                                                 dimension);
            int[][] vPixels = SimplePlotUtil.getPlottableSimple(vseis,
                                                                vunitRangeImpl,
                                                                microSecondTimeRange,
                                                                dimension);
            SimplePlotUtil.flipArray(vPixels[1], dimension.height);
            Shape shape = getParticleMotionPath(hPixels[1], vPixels[1]);
            particleMotion.setShape(shape);
            if(shape == null) logger.debug("The shape is null");
            graphics2D.draw(shape);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized Shape getParticleMotionPath(int[] x, int[] y) {
        int len = x.length;
        if(y.length < len) { len = y.length;}
        GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        if(len != 0) {
            generalPath.moveTo(x[0], y[0]);
        }
        for(int counter = 1; counter < len; counter++) {
            generalPath.lineTo(x[counter], y[counter]);
        }
        return (Shape)generalPath;
    }

    public Shape getAzimuthPath() {
        int size = azimuths.size();
        Insets insets = getInsets();
        double  fmin = super.getSize().getWidth() - insets.left - insets.right;
        double fmax = super.getSize().getHeight() - insets.top - insets.bottom;
        int originx = (int)(fmin/2);
        int originy = (int)(fmax/2);
        int newx = originx;
        int newy =  originy;
        GeneralPath generalPath = new GeneralPath();
        for(int counter = 0; counter < size; counter++) {
            double degrees = ((Double)azimuths.get(counter)).doubleValue();
            degrees = degrees;
            int x = (int)(fmin * Math.cos(Math.toRadians(degrees)));
            int y = (int)(fmax * Math.sin(Math.toRadians(degrees)));
            generalPath.moveTo(newx+x, newy-y);
            generalPath.lineTo(newx-x, newy+y);
        }
        return (Shape)generalPath;
    }

    public synchronized Shape getSectorShape() {
        Insets insets = getInsets();
        double  fmin = super.getSize().getWidth() - insets.left - insets.right;
        double fmax = super.getSize().getHeight() - insets.top - insets.bottom;
        int originx = (int)(fmin/2);
        int originy = (int)(fmax/2);
        int newx = originx;
        int newy = originy;
        GeneralPath generalPath = new GeneralPath();
        int size = sectors.size();
        for(int counter = 0; counter < size; counter++) {
            Point2D.Double point = (Point2D.Double)sectors.get(counter);
            double degreeone = point.getX();
            double degreetwo = point.getY();
            int xone = (int)(fmin * Math.cos(Math.toRadians(degreeone)));
            int yone = (int)(fmax * Math.sin(Math.toRadians(degreeone)));
            generalPath.moveTo(newx+xone, newy-yone);
            generalPath.lineTo(newx-xone, newy+yone);
            int xtwo = (int)(fmin * Math.cos(Math.toRadians(degreetwo)));
            int ytwo = (int)(fmax * Math.sin(Math.toRadians(degreetwo)));
            generalPath.lineTo(newx-xtwo, newy+ytwo);
            generalPath.lineTo(newx+xtwo, newy-ytwo);
            generalPath.lineTo(newx+xone, newy-yone);
        }
        return (Shape)generalPath;
    }

    public synchronized void addParticleMotionDisplay(DataSetSeismogram hseis,
                                                      DataSetSeismogram vseis,
                                                      Registrar registrar,
                                                      Color color,
                                                      String key,
                                                      boolean horizPlane) {
        ParticleMotion particleMotion = new ParticleMotion(hseis,
                                                           vseis,
                                                           registrar,
                                                           color, key,
                                                           horizPlane);
        displays.add(particleMotion);
        hunitRangeImpl = new UnitRangeImpl(getMinHorizontalAmplitude(),
                                           getMaxHorizontalAmplitude(),
                                           UnitImpl.COUNT);
        vunitRangeImpl = new UnitRangeImpl(getMinVerticalAmplitude(),
                                           getMaxVerticalAmplitude(),
                                           UnitImpl.COUNT);
        particleMotionDisplay.updateHorizontalAmpScale(hunitRangeImpl);
        particleMotionDisplay.updateVerticalAmpScale(vunitRangeImpl);

    }

    public void addSector(double degreeone, double degreetwo) {
        sectors.add(new java.awt.geom.Point2D.Double(degreeone, degreetwo));
    }

    public double getMinHorizontalAmplitude() {
        int size = displays.size();
        double min = Double.POSITIVE_INFINITY;
        for(int counter = 0; counter < size; counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            Registrar ampRangeConfig = particleMotion.registrar;
            UnitRangeImpl unitRangeImpl = ampRangeConfig.getLatestAmp().getAmp(particleMotion.hseis);
            if(min > unitRangeImpl.getMinValue()) { min = unitRangeImpl.getMinValue();}
        }
        return min;
    }

    public double getMaxHorizontalAmplitude() {
        int size = displays.size();
        double max = Double.NEGATIVE_INFINITY;
        for(int counter = 0; counter < size; counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            Registrar ampRangeConfig = particleMotion.registrar;
            UnitRangeImpl unitRangeImpl = ampRangeConfig.getLatestAmp().getAmp(particleMotion.hseis);
            if(max < unitRangeImpl.getMaxValue()) { max = unitRangeImpl.getMaxValue();}
        }
        return max;
    }

    /**
     * Describe <code>getMinVerticalAmplitude</code> method here.
     *
     * @return a <code>double</code> value
     */
    public double getMinVerticalAmplitude() {
        int size = displays.size();
        double min = Double.POSITIVE_INFINITY;
        for(int counter = 0; counter < size; counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            Registrar ampRangeConfig = particleMotion.registrar;
            UnitRangeImpl unitRangeImpl = ampRangeConfig.getLatestAmp().getAmp(particleMotion.vseis);
            if( min > unitRangeImpl.getMinValue()) { min = unitRangeImpl.getMinValue();}
        }
        return min;
    }

    public double getMaxVerticalAmplitude() {
        int size = displays.size();
        double max = Double.NEGATIVE_INFINITY;
        for(int counter = 0; counter < size; counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            if(!displayKeys.contains(particleMotion.key)) continue;
            Registrar ampRangeConfig = particleMotion.registrar;
            UnitRangeImpl unitRangeImpl = ampRangeConfig.getLatestAmp().getAmp(particleMotion.vseis);
            if(max < unitRangeImpl.getMaxValue()) { max = unitRangeImpl.getMaxValue();}
        }
        return max;
    }

    public void addAzimuthLine(double degrees) {

        azimuths.add(new Double(degrees));
    }

    /**
     * must be square
     * @param d a <code>Dimension</code> value
     */
    public void setSize(Dimension d) {
        logger.debug("Setting the size");

        if (d.width < d.height) {
            super.setSize(new Dimension(d.width,
                                        d.width));
        } else {
            super.setSize(new Dimension(d.height,
                                        d.height));
        }
    }

    public void setZoomIn() {
        this.zoomIn  = true;
        this.zoomOut = false;
    }

    public void setZoomOut() {
        this.zoomIn = false;
        this.zoomOut = true;
    }

    /*** updates the timeRange****/
    public synchronized void updateTime() {
        hunitRangeImpl = new UnitRangeImpl(getMinHorizontalAmplitude(),
                                           getMaxHorizontalAmplitude(),
                                           UnitImpl.COUNT);
        vunitRangeImpl = new UnitRangeImpl(getMinVerticalAmplitude(),
                                           getMaxVerticalAmplitude(),
                                           UnitImpl.COUNT);
        particleMotionDisplay.updateHorizontalAmpScale(hunitRangeImpl);
        particleMotionDisplay.updateVerticalAmpScale(vunitRangeImpl);
    }

    /**
     * sets the display key **
     * @param key a <code>String</code> value
     */
    public void setDisplayKey(String key) {
        this.displayKey = key;
    }

    public void addDisplayKey(String key) {
        if(!this.displayKeys.contains(key)) {
            this.displayKeys.add(key);
        }
    }

    public void removeDisplaykey(String key) {
        this.displayKeys.remove(key);
    }

    /**
     * gets the display key *
     * @return a <code>String</code> value
     */
    public String getDisplayKey() {
        return this.displayKey;
    }

    public ParticleMotion[] getSelectedParticleMotion() {
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < displays.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)displays.get(counter);
            //if(!getDisplayKey().equals(particleMotion.key)) continue;
            if(displayKeys.contains(particleMotion.key)) {
                arrayList.add(particleMotion);
            }
        }//end of for
        ParticleMotion[] rtnValues = new ParticleMotion[arrayList.size()];
        rtnValues = (ParticleMotion[])arrayList.toArray(rtnValues);
        return rtnValues;
    }

    private Vector displayKeys = new Vector();
    private String displayKey =  new String();
    private boolean zoomIn = false;
    private boolean zoomOut = false;
    private boolean recalculateValues = true;

    LinkedList displays = new LinkedList();
    LinkedList azimuths = new LinkedList();
    LinkedList sectors = new LinkedList();
    UnitRangeImpl hunitRangeImpl = null;
    UnitRangeImpl vunitRangeImpl = null;
    java.awt.geom.Point2D.Float startPoint;
    java.awt.geom.Point2D.Float endPoint;

    private static int RGBCOLOR = 0;
    private ParticleMotionDisplay particleMotionDisplay;

    private static final Color[]  COLORS = { Color.red,
            Color.magenta,
            Color.cyan,
            Color.blue,
            Color.white,
            Color.black};

    class ParticleMotion implements TimeListener, SeisDataChangeListener{
        public ParticleMotion(final DataSetSeismogram hseis,
                              DataSetSeismogram vseis,
                              Registrar registrar,
                              Color color,
                              String key,
                              boolean horizPlane) {

            this.hseis = hseis;
            hseis.addSeisDataChangeListener(this);
            hseis.retrieveData(this);
            this.vseis = vseis;
            vseis.addSeisDataChangeListener(this);
            vseis.retrieveData(this);
            this.registrar = registrar;
            this.key = key;
            this.horizPlane = horizPlane;
            setColor(color);
            if(this.registrar != null) {
                this.registrar.addListener(this);
            }
        }

        public void pushData(SeisDataChangeEvent sdce) {
            if(sdce.getSource() == hseis){
                hSeisLocal = sdce.getSeismograms()[0];
            }else if(sdce.getSource() == vseis){
                vSeisLocal = sdce.getSeismograms()[0];
            }
        }

        public void finished(SeisDataChangeEvent sdce) {
        }

        public void updateTime(edu.sc.seis.fissuresUtil.display.TimeEvent timeEvent) {
            this.microSecondTimeRange = timeEvent.getTime();
        }

        public MicroSecondTimeRange getTimeRange() {
            return this.microSecondTimeRange;
        }
        public boolean isHorizontalPlane() {
            return this.horizPlane;
        }
        public void setShape(Shape shape) {
            this.shape = shape;
        }

        public Shape getShape() {
            return shape;
        }
        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            if(selected) return Color.cyan;
            return this.color;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean value) {
            this.selected = value;
        }

        public LocalSeismogramImpl getLocalHSeis(){ return hSeisLocal; }

        public LocalSeismogramImpl getLocalVSeis(){ return vSeisLocal; }

        public DataSetSeismogram hseis;
        public DataSetSeismogram vseis;
        private LocalSeismogramImpl hSeisLocal;
        private LocalSeismogramImpl vSeisLocal;
        public Registrar registrar;
        public String key = new String();
        private MicroSecondTimeRange microSecondTimeRange;
        private Shape shape;
        private Color color = null;
        private boolean selected = false;
        private boolean horizPlane = false;
    }

    static Category logger =
        Category.getInstance(ParticleMotionView.class.getName());

}// ParticleMotionView
