package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.mouse.SDMouseForwarder;
import edu.sc.seis.fissuresUtil.display.mouse.SDMouseMotionForwarder;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public abstract class SeismogramDisplay extends BorderedDisplay implements
        DataSetSeismogramReceptacle {

    public SeismogramDisplay() {
        this(mouseForwarder, motionForwarder);
    }

    public SeismogramDisplay(SDMouseForwarder mf, SDMouseMotionForwarder mmf) {
        mouseForwarder = mf;
        motionForwarder = mmf;
        if(mouseForwarder == null || motionForwarder == null) {
            mouseForwarder = new SDMouseForwarder();
            motionForwarder = new SDMouseMotionForwarder();
        }
        add(createCenter(), CENTER);
        colors = COLORS;
    }

    public void add(SeismogramDisplayListener listener) {
        listeners.add(listener);
    }

    public void remove(SeismogramDisplayListener listener) {
        listeners.remove(listener);
    }

    public SeismogramDisplayProvider getCenter() {
        return (SeismogramDisplayProvider)get(CENTER);
    }

    public abstract SeismogramDisplayProvider createCenter();

    public void renderToGraphics(Graphics2D g, Dimension size) {
        PRINTING = true;
        boolean allHere = true;
        long totalWait = 0;
        Iterator seisIt = iterator(DrawableSeismogram.class);
        while(seisIt.hasNext()) {
            DrawableSeismogram cur = (DrawableSeismogram)seisIt.next();
            cur.addToTimeAndAmp();
            if(cur.getDataStatus() == SeismogramContainer.GETTING_DATA) {
                cur.getData();
                allHere = false;
            }
        }
        while(!allHere && totalWait < TWO_MIN) {
            seisIt = iterator(DrawableSeismogram.class);
            allHere = true;
            while(seisIt.hasNext()) {
                DrawableSeismogram cur = (DrawableSeismogram)seisIt.next();
                if(cur.getDataStatus() == SeismogramContainer.GETTING_DATA) {
                    try {
                        Thread.sleep(100);
                        totalWait += 100;
                        if(totalWait % 10000 == 0 && totalWait != 0) {
                            logger.debug("Waiting for data to show before rendering.  We've waited "
                                    + totalWait + " millis");
                        }
                    } catch(InterruptedException e) {}
                    allHere = false;
                }
            }
        }
        logger.debug("Rendering to graphics after waiting " + totalWait
                + " millis for data to arrive");
        if(totalWait >= TWO_MIN) {
            logger.debug("GAVE UP WAITING ON DATA TO RENDER TO GRAPHICS!  SOMEONE IS LYING OR REALLY REALLY SLOW! OR BOTH!!");
        }
        super.renderToGraphics(g, size);
        PRINTING = false;
    }

    private static final long TWO_MIN = 2 * 60 * 1000;

    public Color getColor() {
        return null;
    }
    public void setColors(Color[] colors){
        this.colors = colors;
    }

    public Color getNextColor(Class colorGroupClass) {
        int[] usages = new int[colors.length];
        for(int i = 0; i < colors.length; i++) {
            Iterator it = iterator(colorGroupClass);
            while(it.hasNext()) {
                Drawable cur = (Drawable)it.next();
                if(cur.getColor().equals(colors[i])) usages[i]++;
                if(cur instanceof DrawableSeismogram) {
                    DrawableSeismogram curSeis = (DrawableSeismogram)cur;
                    Iterator childIterator = curSeis.iterator(colorGroupClass);
                    while(childIterator.hasNext()) {
                        Drawable curChild = (Drawable)childIterator.next();
                        if(curChild.getColor().equals(colors[i])) usages[i]++;
                    }
                }
            }
        }
        for(int minUsage = 0; minUsage >= 0; minUsage++) {
            for(int i = 0; i < usages.length; i++) {
                if(usages[i] == minUsage) return colors[i];
            }
        }
        return colors[i++ % colors.length];
    }

    private int i = 0;

    public abstract void add(Drawable drawable);

    public abstract void remove(Drawable drawable);

    public abstract DrawableIterator getDrawables(MouseEvent e);

    public abstract DrawableIterator iterator(Class drawableClass);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void print();

    public void remove(Selection selection) {}

    public static void setMouseMotionForwarder(SDMouseMotionForwarder mf) {
        motionForwarder = mf;
    }

    public static SDMouseMotionForwarder getMouseMotionForwarder() {
        return motionForwarder;
    }

    public static void setMouseForwarder(SDMouseForwarder mf) {
        mouseForwarder = mf;
    }

    public static SDMouseForwarder getMouseForwarder() {
        return mouseForwarder;
    }

    public static Set getActiveFilters() {
        return activeFilters;
    }

    public static void setCurrentTimeFlag(boolean visible) {
        currentTimeFlag = visible;
    }

    public static boolean getCurrentTimeFlag() {
        return currentTimeFlag;
    }

    private static SDMouseMotionForwarder motionForwarder;

    private static SDMouseForwarder mouseForwarder;

    private List listeners = new ArrayList();

    private static boolean currentTimeFlag = false;

    protected static Set activeFilters = new HashSet();
    
    public Color[] colors;

    public static final Color[] COLORS = {Color.BLUE,
                                          new Color(217, 91, 23),
                                          new Color(179, 182, 46),
                                          new Color(141, 18, 69),
                                          new Color(65, 200, 115),
                                          new Color(27, 36, 138),
                                          new Color(130, 145, 230),
                                          new Color(54, 72, 21),
                                          new Color(119, 17, 136)};

    private static final Logger logger = Logger.getLogger(SeismogramDisplay.class);

    public static boolean PRINTING = false;
}