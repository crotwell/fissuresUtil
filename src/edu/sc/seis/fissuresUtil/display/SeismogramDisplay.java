package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.drawable.Selection;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.DataSetSeismogramReceptacle;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
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

    public abstract void setCurrentTimeFlag(boolean visible);

    public abstract void setTimeConfig(TimeConfig timeConfig);

    public abstract TimeConfig getTimeConfig();

    public abstract void setAmpConfig(AmpConfig ampConfig);

    public abstract void setGlobalizedAmpConfig(AmpConfig ampConfig);

    public abstract void setIndividualizedAmpConfig(AmpConfig ampConfig);

    public abstract AmpConfig getAmpConfig();

    public abstract DataSetSeismogram[] getSeismograms();

    public abstract void setRegistrar(Registrar registrar);

    public abstract Registrar getRegistrar();

    public abstract void applyFilter(ColoredFilter filter);

    public abstract void setOriginalVisibility(boolean visible);

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
}

