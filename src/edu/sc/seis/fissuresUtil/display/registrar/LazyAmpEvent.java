package edu.sc.seis.fissuresUtil.display.registrar;

/**
 * LazyAmpEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private synchronized void calculate(){
        if(!calculated){
            event = config.calculate();
            calculated = true;
            Iterator it = calculateListeners.iterator();
            while(it.hasNext()){
                ((AmpListener)it.next()).updateAmp(event);
            }
        }
    }

    /**lets an amp listener know when this lazy amp event's information is
     * actually calculated by firing updateAmp on the listener with the
     *info
     */
    public synchronized void addCalculateListener(AmpListener listener){
        if(!calculated){
            calculateListeners.add(listener);
        }else{
            listener.updateAmp(event);
        }
    }
    public DataSetSeismogram[] getSeismograms() {
        calculate();
        return event.getSeismograms();
    }

    private boolean calculated = false;

    private BasicAmpConfig config;

    private AmpEvent event;

    private List calculateListeners = new ArrayList();
}


