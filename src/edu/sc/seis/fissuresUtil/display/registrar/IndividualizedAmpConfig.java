package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndividualizedAmpConfig implements AmpConfig, AmpListener{
    public IndividualizedAmpConfig(AmpConfig wrapped){
        this.wrapped = wrapped;
        wrapped.addListener(this);
    }

    public IndividualizedAmpConfig(AmpConfig wrapped, DataSetSeismogram[] seismos){
        this.wrapped = wrapped;
        wrapped.addListener(this);
        add(seismos);
    }

    public synchronized AmpEvent recalculateAmp(){
        DataSetSeismogram[] seismos = getSeismograms();
        UnitRangeImpl[] amps = new UnitRangeImpl[seismos.length];
        for (int i = 0; i < seismos.length; i++){
            amps[i] = wrapped.getAmpData(seismos[i]).getRange();
        }
        AmpEvent individualizedAmps = new AmpEvent(seismos, amps);
        individualizedAmps.setAmp(DisplayUtils.ONE_RANGE);
        return individualizedAmps;
    }

    public void clear() {
        wrapped.clear();
    }

    public void reset(DataSetSeismogram[] seismos) {
        wrapped.reset();
    }

    public boolean contains(DataSetSeismogram seismo) {
        return wrapped.contains(seismo);
    }

    public void add(DataSetSeismogram[] seismos) {
        wrapped.add(seismos);
    }

    public void reset() {
        wrapped.reset();
    }

    public void removeListener(AmpListener listener) {
        listeners.remove(listener);
    }

    public AmpEvent updateAmpTime(TimeEvent event) {
        wrapped.updateAmpTime(event);
        return recalculateAmp();
    }

    public void addListener(AmpListener listener) {
        listeners.add(listener);
    }

    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos) {
        wrapped.shaleAmp(shift, scale, seismos);
    }

    public void remove(DataSetSeismogram[] seismos) {
        wrapped.remove(seismos);
    }

    public void shaleAmp(double shift, double scale) {
        wrapped.shaleAmp(shift, scale);
    }

    public AmpConfigData getAmpData(DataSetSeismogram seis){
        return wrapped.getAmpData(seis);
    }

    public AmpEvent fireAmpEvent() {
        AmpEvent current = recalculateAmp();
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((AmpListener)it.next()).updateAmp(current);
        }
        return current;
    }

    public DataSetSeismogram[] getSeismograms() {
        return wrapped.getSeismograms();
    }

    public void updateAmp(AmpEvent e){
        fireAmpEvent();
    }

    private AmpConfig wrapped;

    private List listeners = new ArrayList();
}

