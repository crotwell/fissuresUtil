package edu.sc.seis.fissuresUtil.display.registrar;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

    public synchronized AmpEvent calculate(){
        ((BasicAmpConfig)wrapped).calculate();
        AmpConfigData[] ad = getAmpData();
        UnitRangeImpl[] amps = new UnitRangeImpl[ad.length];
        for (int i = 0; i < ad.length; i++){
            amps[i] = ad[i].getRange();
        }
        return new BasicAmpEvent(AmpConfigData.getSeismograms(ad), amps, DisplayUtils.ONE_RANGE);
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

    public void updateTime(TimeEvent event) {
        wrapped.updateTime(event);
        fireAmpEvent();
    }

    public void addListener(AmpListener listener) {
        if(listener != null){
            listeners.add(listener);
        }
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

    public UnitRangeImpl getAmp() {
        return wrapped.getAmp();
    }

    public UnitRangeImpl getAmp(DataSetSeismogram seis) {
        return wrapped.getAmp(seis);
    }

    public AmpConfigData getAmpData(DataSetSeismogram seis){
        return wrapped.getAmpData(seis);
    }

    public AmpConfigData[] getAmpData(){ return wrapped.getAmpData(); }

    public void fireAmpEvent() {
        AmpEvent current = calculate();
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((AmpListener)it.next()).updateAmp(current);
        }
    }

    public DataSetSeismogram[] getSeismograms() {
        return wrapped.getSeismograms();
    }

    public void updateAmp(AmpEvent e){
        fireAmpEvent();
    }

    private AmpConfig wrapped;

    private Set listeners = new HashSet();
}


