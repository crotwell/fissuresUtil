package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.UnsupportedDataEncoding;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.*;

/**
 * BasicAmpConfig.java
 *
 *
 * Created: Fri Aug 30 10:35:20 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class BasicAmpConfig implements AmpConfig{
    public BasicAmpConfig(DataSetSeismogram[] seismos){
        add(seismos);
    }
    
    /**
     * <code>add</code> is the method used to add a seismogram to this object
     *
     * @param seismo the seismogram to be added
     */
    public void add(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            ampData.put(seismos[i], new AmpConfigData(seismos[i], null, null, shift, scale));
            DisplayUtils.statCache.put(seismos[i], new Statistics(seismos[i].getSeismogram()));
        }
        this.seismos = null;
        calculateAmp();
        recalculateAmp();
        fireAmpEvent();
    }
    
    /**
     * <code>remove</code> removes seismograms from this object
     *
     * @param seismo the seismograms to be removed
     */
    public boolean remove(DataSetSeismogram[] seismos){
        boolean allRemoved = true;
        for(int i = 0; i < seismos.length; i++){
            if(!ampData.containsKey(seismos[i])){
                allRemoved = false;
            }else{
                ampData.remove(seismos[i]);
            }
        }
        this.seismos = null;
        recalculateAmp();
        fireAmpEvent();
        return allRemoved;
    }
    
    public DataSetSeismogram[] getSeismograms(){
        if(seismos == null){
            seismos = (DataSetSeismogram[])ampData.keySet().toArray(new DataSetSeismogram[ampData.size()]);
        }
        return seismos;
    }
    
    
    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public boolean contains(DataSetSeismogram seismo){
        if(ampData.containsKey(seismo)){
            return true;
        }
        return false;
    }
    
    public void reset(){
        reset(getSeismograms());
    }
    
    public void reset(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            ((AmpConfigData)ampData.get(seismos[i])).reset();
        }
        fireAmpEvent();
    }
    
    public void shaleAmp(double shift, double scale){
        shaleAmp(shift, scale, getSeismograms());
    }
    
    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            ((AmpConfigData)ampData.get(seismos[i])).shale(shift, scale);
        }
        fireAmpEvent();
    }
    
    public AmpEvent fireAmpEvent(){
        AmpEvent event = calculateAmp();
        Iterator e = listeners.iterator();
        while(e.hasNext()){
            ((AmpListener)e.next()).updateAmp(event);
        }
        return event;
    }
    
    public void addListener(AmpListener listener){
        listeners.add(listener);
        fireAmpEvent();
    }
    
    public void removeListener(AmpListener listener){
        listeners.remove(listener);
    }
    
    public AmpEvent updateAmpTime(TimeEvent timeEvent){
        currentTimeEvent = timeEvent;
        return calculateAmp();
    }
    
    protected AmpEvent calculateAmp(){
        Iterator e = ampData.keySet().iterator();
        boolean changed = false;
        while(e.hasNext()){
            AmpConfigData current = (AmpConfigData)ampData.get(e.next());
            if(current.setTime(getTime(current.getSeismogram()))){ //checks for the time update equaling the old time
                if(setAmpRange(current.getSeismogram())){ //checks if the new time changes the amp range
                    changed = true;// only generates a new amp event if the amp ranges have changed
                }
            }
        }
        if(changed || currentAmpEvent == null){
            recalculateAmp();
        }
        return currentAmpEvent;
    }
    
    protected AmpEvent recalculateAmp(){
        Iterator e = ampData.keySet().iterator();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        while(e.hasNext()){
            UnitRangeImpl current = ((AmpConfigData)ampData.get(e.next())).getShaledRange();
            if(current != null){
                if(current.getMaxValue() > max){
                    max = current.getMaxValue();
                }
                if(current.getMinValue() < min){
                    min = current.getMinValue();
                }
            }
        }
        UnitRangeImpl fullRange = new UnitRangeImpl(min, max, UnitImpl.COUNT);
        DataSetSeismogram[] seismos = getSeismograms();
        UnitRangeImpl[] amps = new UnitRangeImpl[seismos.length];
        for(int i = 0; i < seismos.length; i++){
            amps[i] = fullRange;
        }
        currentAmpEvent = new AmpEvent(seismos, amps);
        return currentAmpEvent;
    }
    
    private boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);
        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismo.getSeismogram();
        int[] seisIndex = DisplayUtils.getSeisPoints(seis, data.getTime());
        if(seisIndex[0] == seisIndex[1]) {
            //no data points in window, set range to 0
            data.setCalcIndex(seisIndex);
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        }
        if(seisIndex[0] < 0){
            seisIndex[0] = 0;
        }
        if(seisIndex[1] >= seis.getNumPoints()){
            seisIndex[1] = seis.getNumPoints() -1;
        }
        double[] minMax = ((Statistics)DisplayUtils.statCache.get(seismo)).minMaxMean(seisIndex[0], seisIndex[1]);
        data.setCalcIndex(seisIndex);
        return data.setCleanRange(new UnitRangeImpl(minMax[0], minMax[1], UnitImpl.COUNT));
    }
    
    protected MicroSecondTimeRange getTime(DataSetSeismogram seismo){
        if(currentTimeEvent != null){
            return currentTimeEvent.getTime(seismo);
        }
        LocalSeismogramImpl iSeis = seismo.getSeismogram();
        return new MicroSecondTimeRange(iSeis.getBeginTime(), iSeis.getEndTime());
    }
    
    protected Map ampData = new HashMap();
    
    private double scale = 1.0;
    
    private double shift = 0;
    
    private List listeners = new ArrayList();
    
    private DataSetSeismogram[] seismos;
    
    protected TimeEvent currentTimeEvent;
    
    protected AmpEvent currentAmpEvent;
    
    private static Category logger = Category.getInstance(BasicSeismogramDisplay.class.getName());
}//BasicAmpConfig
