package edu.sc.seis.fissuresUtil.display.registrar;

/**
 * LazyAmpEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class LazyAmpEvent implements AmpEvent{
    public LazyAmpEvent(BasicAmpConfig config){
        this.config = config;
    }

    public UnitRangeImpl getAmp(DataSetSeismogram seismo){
        calculate();
        return event.getAmp(seismo);
    }

    public boolean contains(DataSetSeismogram seismo){
        calculate();
        return event.contains(seismo);
    }

    public UnitRangeImpl getAmp(){
        calculate();
        return event.getAmp();
    }

    public void setAmp(UnitRangeImpl amp) {
        calculate();
        event.setAmp(amp);
    }

    private synchronized void calculate(){
        if(!calculated){
            event = config.calculateAmp();
            calculated = true;
        }
    }

    public DataSetSeismogram[] getSeismograms() {
        calculate();
        return event.getSeismograms();
    }

    private boolean calculated = false;

    private BasicAmpConfig config;

    private AmpEvent event;
}

