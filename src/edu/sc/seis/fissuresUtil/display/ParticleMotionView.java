package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.*;
import java.awt.*;
import java.util.*;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.RequestFilterChangeListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JComponent;
import org.apache.log4j.Logger;

/**
 * ParticleMotionView.java
 *
 *
 * Created: Tue Jun 11 15:14:17 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionView extends JComponent implements TimeListener{
    
    public ParticleMotionView(ParticleMotionDisplay particleMotionDisplay) {
        this.pmd = particleMotionDisplay;
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
        repaint();
    }
    
    public void updateAmps(){
        AmpConfig activeAC = (AmpConfig)keysToAmpConfigs.get(displayKey);
        if(activeAC != null){
            vertRange = activeAC.getAmp();
            horizRange = activeAC.getAmp();
        }else{
            vertRange = DisplayUtils.ONE_RANGE;
            horizRange = DisplayUtils.ONE_RANGE;
        }
        pmd.updateHorizontalAmpScale(horizRange);
        pmd.updateVerticalAmpScale(vertRange);
    }
    
    public synchronized void paintComponent(Graphics g) {
        if(displayKey == null) return;
        Graphics2D graphics2D = (Graphics2D)g;
        //first draw the azimuth if one of the display is horizontal plane
        for(int counter = 0; counter < parMos.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)parMos.get(counter);
            if(!displayKey.equals(particleMotion.key)) continue;
            if(particleMotion.isHorizontalPlane()){
                drawAzimuth(particleMotion, graphics2D);
                break;
            }
        }
        for(int counter = 0; counter < parMos.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)parMos.get(counter);
            if(!displayKey.equals(particleMotion.key)) continue;
            particleMotion.draw(g, getSize());
        }
    }
    
    
    public synchronized void drawAzimuth(ParticleMotion particleMotion, Graphics2D graphics2D) {
        if(!particleMotion.isHorizontalPlane()) return;
        Shape sector = getSectorShape();
        graphics2D.setPaint(Color.LIGHT_GRAY);
        graphics2D.fill(sector);
        graphics2D.draw(sector);
        drawAzimuths(graphics2D);
        graphics2D.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
    }
    
    public void drawTitles(LocalSeismogramImpl hseis, LocalSeismogramImpl vseis) {
        pmd.setHorizontalTitle(hseis.getName());
        pmd.setVerticalTitle(vseis.getName());
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
    
    public void drawAzimuths(Graphics2D g2D) {
        Insets insets = getInsets();
        double  fmin = super.getSize().getWidth() - insets.left - insets.right;
        double fmax = super.getSize().getHeight() - insets.top - insets.bottom;
        int originx = (int)(fmin/2);
        int originy = (int)(fmax/2);
        int newx = originx;
        int newy =  originy;
        Iterator it = azimuths.keySet().iterator();
        g2D.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
        while(it.hasNext()){
            Double key = (Double)it.next();
            Color parMoCo =(Color)azimuths.get(key);
            g2D.setColor(new Color(parMoCo.getRed(), parMoCo.getGreen(),
                                   parMoCo.getBlue(), 96));
            GeneralPath generalPath = new GeneralPath();
            double degrees = key.doubleValue();
            int x = (int)(fmin * Math.cos(Math.toRadians(degrees)));
            int y = (int)(fmax * Math.sin(Math.toRadians(degrees)));
            generalPath.moveTo(newx+x, newy-y);
            generalPath.lineTo(newx-x, newy+y);
            g2D.draw(generalPath);
        }
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
                                                      TimeConfig tc,
                                                      Color color,
                                                      String key,
                                                      boolean horizPlane) {
        parMos.add(new ParticleMotion(hseis, vseis, tc, color, key,horizPlane));
        updateAmps();
    }
    
    public void addSector(double degreeone, double degreetwo) {
        sectors.add(new java.awt.geom.Point2D.Double(degreeone, degreetwo));
    }
    
    public void addAzimuthLine(double degrees, Color color) {
        azimuths.put(new Double(degrees), color);
    }
    
    /**
     * must be square
     * @param d a <code>Dimension</code> value
     */
    public void setSize(Dimension d) {
        if (d.width < d.height) {
            super.setSize(new Dimension(d.width,
                                        d.width));
        } else {
            super.setSize(new Dimension(d.height,
                                        d.height));
        }
    }
    
    public synchronized void updateTime(TimeEvent e) {
        updateAmps();
        repaint();
    }
    
    public void setDisplayKey(String key) {
        displayKey = key;
    }
    
    public ParticleMotion[] getSelectedParticleMotion() {
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < parMos.size(); counter++) {
            ParticleMotion particleMotion = (ParticleMotion)parMos.get(counter);
            if(displayKey.equals(particleMotion.key)) {
                arrayList.add(particleMotion);
            }
        }//end of for
        ParticleMotion[] rtnValues = new ParticleMotion[arrayList.size()];
        rtnValues = (ParticleMotion[])arrayList.toArray(rtnValues);
        return rtnValues;
    }
    
    private String displayKey;
    
    private Map keysToAmpConfigs = new HashMap();
    
    LinkedList parMos = new LinkedList();
    
    Map azimuths = new HashMap();
    
    List sectors = new LinkedList();
    
    UnitRangeImpl horizRange = DisplayUtils.ONE_RANGE;
    
    UnitRangeImpl vertRange = DisplayUtils.ONE_RANGE;
    
    Point2D.Float startPoint;
    
    Point2D.Float endPoint;
    
    private ParticleMotionDisplay pmd;
    
    class ParticleMotion implements TimeListener, AmpListener,
        SeismogramContainerListener, RequestFilterChangeListener{
        public ParticleMotion(DataSetSeismogram hSeis, DataSetSeismogram vSeis,
                              TimeConfig tc, Color color, String key,
                              boolean horizPlane) {
            //hSeis.addRequestFilterChangeListener(this);
            //vSeis.addRequestFilterChangeListener(this);
            DataSetSeismogram[] seis = { hSeis, vSeis};
            AmpConfig ac = (AmpConfig)keysToAmpConfigs.get(key);
            if(ac == null){
                ac = new RMeanAmpConfig();
                keysToAmpConfigs.put(key, ac);
            }
            ac.add(seis);
            ac.addListener(this);
            tc.addListener(ac);
            tc.addListener(this);
            tc.addListener(ParticleMotionView.this);
            tc.add(seis);
            horiz = new SeismogramContainer(this, seis[0]);
            vert = new SeismogramContainer(this, seis[1]);
            this.key = key;
            this.horizPlane = horizPlane;
            if(horizPlane){
                pmd.displayBackAzimuth(hSeis.getDataSet(),
                                       hSeis.getRequestFilter().channel_id,
                                       color);
            }
            this.color = color;
            tc.fireTimeEvent();
        }
        
        public void draw(Graphics g, Dimension size) {
            Graphics2D g2D = (Graphics2D) g;
            if(horiz.getIterator(tr).numPointsLeft() <= 0
               || vert.getIterator(tr).numPointsLeft() <= 0){
                return;
            }
            g2D.setColor(color);
            g2D.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            int[]hPixels =  SimplePlotUtil.getPlottableSimple(horiz.getIterator(tr),
                                                              ae.getAmp(horiz.getDataSetSeismogram()),
                                                              size);
            int[] vPixels = SimplePlotUtil.getPlottableSimple(vert.getIterator(tr),
                                                              ae.getAmp(vert.getDataSetSeismogram()),
                                                              size);
            SimplePlotUtil.flipArray(hPixels, size.width);
            g2D.draw(getParticleMotionPath(hPixels, vPixels));
        }
        
        public void updateData() {
            repaint();
        }
        
        //Particle motion view needs to keep the data for both of its data set
        //seismograms in sync, so it listens on their request filter changing,
        //and if they don't match, makes them match
        public void beginTimeChanged() {
            MicroSecondDate horizBegin = new MicroSecondDate(horiz.getDataSetSeismogram().getRequestFilter().start_time);
            MicroSecondDate vertBegin = new MicroSecondDate(vert.getDataSetSeismogram().getRequestFilter().start_time);
            if(horizBegin.before(vertBegin)){
                vert.getDataSetSeismogram().setBeginTime(horizBegin.getFissuresTime());
            }else if(vertBegin.before(horizBegin)){
                horiz.getDataSetSeismogram().setBeginTime(vertBegin.getFissuresTime());
            }
        }
        
        public void endTimeChanged() {
            MicroSecondDate horizEnd = new MicroSecondDate(horiz.getDataSetSeismogram().getRequestFilter().end_time);
            MicroSecondDate vertEnd = new MicroSecondDate(vert.getDataSetSeismogram().getRequestFilter().end_time);
            if(horizEnd.after(vertEnd)){
                vert.getDataSetSeismogram().setEndTime(horizEnd.getFissuresTime());
            }else if(vertEnd.after(horizEnd)){
                horiz.getDataSetSeismogram().setEndTime(vertEnd.getFissuresTime());
            }
        }
        
        
        public void updateAmp(AmpEvent event) {
            this.ae = event;
        }
        
        public void updateTime(TimeEvent timeEvent) {
            this.tr = timeEvent.getTime();
        }
        
        public boolean isHorizontalPlane() {
            return this.horizPlane;
        }
        
        private SeismogramContainer horiz, vert;
        
        public String key = new String();
        private MicroSecondTimeRange tr;
        private Color color;
        private boolean horizPlane = false;
        private AmpEvent ae;
    }
    
    private static int i = 0;
    
    private static Logger logger = Logger.getLogger(ParticleMotionView.class);
    
}// ParticleMotionView
