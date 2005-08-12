package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplayProvider;
import edu.sc.seis.fissuresUtil.display.borders.AmpBorder;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.SeismogramRemover;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.AmpListener;
import edu.sc.seis.fissuresUtil.display.registrar.BasicAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeListener;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author hedx Created on Jun 23, 2005
 */
public class ParticleMotionDisplay extends SeismogramDisplay implements
        TimeListener, AmpListener {

    public ParticleMotionDisplay(TimeConfig tc) {
        this(tc, new BasicAmpConfig());
    }

    public ParticleMotionDisplay(TimeConfig tc, AmpConfig ac) {
        setBorder(BorderFactory.createEtchedBorder());
        add(new AmpBorder(this, LEFT, false), CENTER_LEFT);
        add(new AmpBorder(this, BOTTOM, false), BOTTOM_CENTER);
        right = new ParticleMotionDirectionBorder(LEFT, ASCENDING, this);
        add(right, CENTER_RIGHT);
        top = new ParticleMotionDirectionBorder(TOP, ASCENDING, this);
        add(top, TOP_CENTER);
        setTimeConfig(tc);
        setAmpConfig(ac);
    }

    public SeismogramDisplayProvider createCenter() {
        ParticleMotionProvider pmp = new ParticleMotionProvider();
        pmp.addMouseListener(getMouseForwarder());
        pmp.addMouseMotionListener(getMouseMotionForwarder());
        pmp.setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        return pmp;
    }

    public void add(DataSetSeismogram horizontal, DataSetSeismogram vertical) {
        add(new SeisContainer(horizontal, vertical));
    }

    public void add(DataSetSeismogram[] seismos) {
    //no longer in use
    }

    public void add(SeisContainer cont) {
        if(cont == null) {
            return;
        }
        if(!contains(cont)) {
            seismograms.add(cont);
            ParticleMotionDisplayDrawable tempDraw = new ParticleMotionDisplayDrawable(this,
                                                                                       cont,
                                                                                       getNextColor(ParticleMotionDisplayDrawable.class));
            drawables.add(tempDraw);
            tempDraw.setTopTitle(top);
            tempDraw.setRightTitle(right);
            getTimeConfig().add(getSeismograms());
            getAmpConfig().add(getSeismograms());
        }
        initialized = true;
    }

    private boolean contains(SeisContainer cont) {
        Iterator it = seismograms.iterator();
        while(it.hasNext()) {
            SeisContainer seis = (SeisContainer)it.next();
            if(seis.equals(cont)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @deprecated
     */

    public void remove(Drawable drawable) {
    //no longer in use
    }
    
    
    /**
     * @deprecated
     */
    public void add(Drawable drawable) {
    /*
     * if(!drawables.contains(drawable)) { drawables.add(drawable);
     * if(hasConfiguredColors(drawable.getClass())) {
     * drawable.setColor(getNextColor(drawable.getClass())); } repaint(); }
     */
    //no longer in use
    }

    public DrawableIterator getDrawables(MouseEvent e) {
        return new DrawableIterator(Drawable.class, new ArrayList());
    }

    public DataSetSeismogram[] getSeismograms() {
        DataSetSeismogram[] returnArray = new DataSetSeismogram[seismograms.size() * 2];
        Iterator iter = seismograms.iterator();
        int n = -1;
        while(iter.hasNext()) {
            SeisContainer tempSeis = (SeisContainer)iter.next();
            returnArray[++n] = tempSeis.getHorz();
            returnArray[++n] = tempSeis.getVert();
        }
        return returnArray;
    }

    public void reset() {
        tc.reset();
        ac.reset();
    }

    public void reset(DataSetSeismogram[] seismos) {
        tc.reset(seismos);
        ac.reset(seismos);
    }

    public void setAmpConfig(AmpConfig ampConfig) {
        if(this.ac != null) {
            this.ac.removeListener(this);
            tc.removeListener(this.ac);
            this.ac.remove(getSeismograms());
        }
        this.ac = ampConfig;
        ac.addListener(this);
        tc.addListener(ac);
        ac.add(getSeismograms());
    }

    public void setGlobalizedAmpConfig(AmpConfig ampConfig) {
        setAmpConfig(ac);
    }

    public void setIndividualizedAmpConfig(AmpConfig ampConfig) {
        setAmpConfig(new IndividualizedAmpConfig(ac));
    }

    public AmpConfig getAmpConfig() {
        return ac;
    }

    public void setTimeConfig(TimeConfig timeConfig) {
        if(this.tc != null) {
            this.tc.removeListener(this);
            this.tc.removeListener(ac);
            this.tc.add(getSeismograms());
        }
        this.tc = timeConfig;
        tc.addListener(this);
        tc.addListener(ac);
        tc.add(getSeismograms());
    }

    public TimeConfig getTimeConfig() {
        return tc;
    }

    public DrawableIterator iterator(Class drawableClass) {
        return new DrawableIterator(drawableClass, drawables);
    }
    /**
     * @deprecated
     */
    public void print() {
    //this is a stub, just to satisfy the compiler.
    }

    public void remove(SeisContainer seismos) {
        if(seismograms.contains(seismos)) {
            seismograms.remove(seismos);
        }
        Iterator iter = drawables.iterator();
        while(iter.hasNext()) {
            ParticleMotionDisplayDrawable draw = (ParticleMotionDisplayDrawable)iter.next();
            if(draw.equals(this, seismos)) {
                iter.remove();
            }
        }
        if(seismograms.size() == 0) {
            clear();
        }
        tc.remove(seismos.getSeismograms());
        ac.remove(seismos.getSeismograms());
    }

    public void remove(DataSetSeismogram[] seismos) {
        for(int i = 0; i < seismos.length; i++) {
            if(seismos[i] != null && seismograms.contains(seismos[i])) {
                seismograms.remove(seismos[i]);
            }
        }
        removeDrawable(seismos);
        if(seismograms.size() == 0) {
            clear();
        }
        tc.remove(seismos);
        ac.remove(seismos);
    }

    private void removeDrawable(DataSetSeismogram[] seismos) {
        Iterator iter = drawables.iterator();
        for(int i = 0; i < seismos.length - 1; i += 2) {
            if(seismos[i] != null && seismos[i + 1] != null) {
                SeisContainer cont = new SeisContainer(seismos[i],
                                                       seismos[i + 1]);
                while(iter.hasNext()) {
                    Drawable current = (Drawable)iter.next();
                    if(current instanceof ParticleMotionDisplayDrawable) {
                        if(((ParticleMotionDisplayDrawable)current).getSeismogram()
                                .equals(cont)) {
                            iter.remove();
                            repaint();
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        tc.remove(getSeismograms());
        ac.remove(getSeismograms());
    }

    public boolean initialized() {
        return initialized;
    }

    private class ParticleMotionProvider extends SeismogramDisplayProvider {

        public SeismogramDisplay provide() {
            return ParticleMotionDisplay.this;
        }

        public void paintComponent(Graphics g) {
            count++;
            System.out.println("count is " + count);
            drawSeismograms((Graphics2D)g, getSize());
        }
    }

    public void drawSeismograms(Graphics2D g2, Dimension size) {
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Float(0, 0, size.width, size.height));
        for(int i = 0; i < drawables.size(); i++) {
            Drawable parMoDrawable = (Drawable)drawables.get(i);
            parMoDrawable.draw(g2, size, timeEvent, ampEvent);
        }
    }

    private boolean isNumberOdd(int n) {
        if(n % 2 == 0) {
            return false;
        }
        return true;
    }

    public MicroSecondTimeRange getTime() {
        return timeEvent.getTime();
    }

    public void updateTime(TimeEvent event) {
        timeEvent = event;
        repaint();
    }

    public void updateAmp(AmpEvent event) {
        ampEvent = event;
        repaint();
    }

    public boolean contains(DataSetSeismogram seismo) {
        //this is a stub just to make the compiler happy
        return false;
    }

    public void removeTitle(ParticleMotionDisplayDrawable draw) {
        top.removeTitle(draw);
        right.removeTitle(draw);
    }

    public LinkedList getPoints() {
        return points;
    }

    public Point2D getLastAdded() {
        return (Point2D)points.getLast();
    }

    public void setPoints(LinkedList points) {
        this.points = points;
    }

    public static int Count() {
        return count;
    }

    public final static int PREFERRED_HEIGHT = 150;

    public final static int PREFERRED_WIDTH = 250;

    private LinkedList drawables = new LinkedList();

    private TimeConfig tc;

    private AmpConfig ac;

    private TimeEvent timeEvent;

    private AmpEvent ampEvent;

    private List seismograms = new ArrayList();

    private boolean initialized = false;

    public static final int ASCENDING = 0, DESCENDING = 4;

    private Color color = Color.BLACK;

    private ParticleMotionDirectionBorder top, right;

    private SeismogramRemover remover;

    public static final int LEFT = 0, RIGHT = 1, TOP = 2, BOTTOM = 3;

    private static int count = 0;

    private LinkedList points;
    
}
