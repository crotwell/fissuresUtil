package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.drawable.Drawable;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

public abstract class SeismogramDisplay extends JComponent implements DataSetSeismogramReceptacle{
    public SeismogramDisplay(){
        this(mouseForwarder, motionForwarder);
    }

    public SeismogramDisplay(MouseForwarder mf, MouseMotionForwarder mmf){
        mouseForwarder = mf;
        motionForwarder = mmf;
        if(mouseForwarder == null || motionForwarder == null){
            throw new IllegalStateException("The mouse forwarders on SeismogramDisplay must be set before any seismogram displays are invoked");
        }
    }

    public void add(SeismogramDisplayListener listener){
        listeners.add(listener);
    }

    public void remove(SeismogramDisplayListener listener){
        listeners.remove(listener);
    }

    public void switchDisplay(SeismogramDisplay to){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((SeismogramDisplayListener)it.next()).switching(this, to);
        }
    }

    public abstract void add(Drawable drawable);

    public abstract void remove(Drawable drawable);

    public static Set getActiveFilters(){ return activeFilters; }

    public abstract DrawableIterator iterator(Class drawableClass);

    public abstract void setCurrentTimeFlag(boolean visible);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void setOriginalVisibility(boolean visible);

    public abstract boolean getOriginalVisibility();

    public abstract void print();

    public void remove(Selection selection){}

    public static void setMouseMotionForwarder(MouseMotionForwarder mf){
        motionForwarder = mf;
    }

    public static MouseMotionForwarder getMouseMotionForwarder(){
        return motionForwarder;
    }

    public static void setMouseForwarder(MouseForwarder mf){
        mouseForwarder = mf;
    }

    public static MouseForwarder getMouseForwarder(){ return mouseForwarder; }

    private static MouseMotionForwarder motionForwarder;

    private static MouseForwarder mouseForwarder;

    private List listeners = new ArrayList();

    protected static Set activeFilters = new HashSet();
}

