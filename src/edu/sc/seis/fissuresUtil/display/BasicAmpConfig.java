package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Category;

/**
 * BasicAmpConfig.java
 *
 *
 * Created: Fri Aug 30 10:35:20 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class BasicAmpConfig implements AmpConfig, SeisDataChangeListener{
    public BasicAmpConfig(DataSetSeismogram[] seismos){
        if(seismos == null || DisplayUtils.allNull(seismos)){
            throw new IllegalArgumentException("Some non null seismograms must be given to an amp config on instantiation");
        }
        add(seismos);
    }

    /**
     * <code>add</code> is the method used to add a seismogram to this object
     *
     * @param seismo the seismogram to be added
     */
    public synchronized void add(DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            if(!ampData.containsKey(seismos[i])){
                ampData.put(seismos[i], new AmpConfigData(seismos[i],
                                                          DisplayUtils.ONE_RANGE,
                                                          DisplayUtils.ONE_TIME,
                                                          shift, scale));
            }
            seismos[i].addSeisDataChangeListener(this);
            seismos[i].retrieveData(this);
        }
    }

    public void pushData(SeisDataChangeEvent sdce) {
        AmpConfigData dssData = (AmpConfigData)ampData.get(sdce.getSource());
        if ( dssData == null) {
            // must not be a DataSetSeismogram registered with us, ignore
            return;
        } // end of if ()
        synchronized(this){
            dssData.addSeismograms(sdce.getSeismograms());
        }
        this.seismos = null;
        if(listeners.size() > 0){
            calculateAmp();
            recalculateAmp();
            fireAmpEvent();
        }
    }

    public void finished(SeisDataChangeEvent sdce) {
    }

    public void error(SeisDataErrorEvent sdce) {
        //do nothing as someone else should handle error notification to user
        logger.warn("Error with data retrieval.",
                    sdce.getCausalException());
    }

    /**
     * <code>remove</code> removes seismograms from this object
     *
     * @param seismo the seismograms to be removed
     */
    public boolean remove(DataSetSeismogram[] seismos){
        boolean allRemoved = true;
        synchronized(this){
            for(int i = 0; i < seismos.length; i++){
                if(!ampData.containsKey(seismos[i])){
                    allRemoved = false;
                }else{
                    ampData.remove(seismos[i]);
                }
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
        if(listeners.size() > 0){
            currentTimeEvent = timeEvent;
            return calculateAmp();
        }
        return null;
    }

    protected synchronized AmpEvent calculateAmp(){
        Iterator e = ampData.keySet().iterator();
        boolean changed = false;
        while(e.hasNext()){
            AmpConfigData current = (AmpConfigData)ampData.get(e.next());
            if(current.setTime(getTime(current.getDSS()))){ //checks for the time update equaling the old time
                if(setAmpRange(current.getDSS())){ //checks if the new time changes the amp range
                    changed = true;// only generates a new amp event if the amp ranges have changed
                }
            }else if(current.hasNewData()){
                setAmpRange(current.getDSS());
                changed = true;
            }
        }
        if(changed || currentAmpEvent == null){
            recalculateAmp();
        }
        return currentAmpEvent;
    }

    protected synchronized AmpEvent recalculateAmp(){
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

    private synchronized boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);

        if ( data.getSeismograms().length == 0) {
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        } // end of if ()

        LocalSeismogramImpl[] seismograms = data.getSeismograms();
        double[] minMaxAll = new double[2];
        minMaxAll[0] = Double.POSITIVE_INFINITY;
        minMaxAll[1] = Double.NEGATIVE_INFINITY;
        boolean dataInTimeRange = false;//set to true if one of the seismograms
        //has data in the current time range
        for(int i = 0; i< seismograms.length; i++){
            LocalSeismogramImpl seismogram = seismograms[i];
            int[] seisIndex = DisplayUtils.getSeisPoints(seismogram, data.getTime());
            if(seisIndex[1] < 0 || seisIndex[0] >= seismogram.getNumPoints()) {
                //no data points in window, set range to 0
                break;
            }
            if(seisIndex[0] < 0){
                seisIndex[0] = 0;
            }
            if(seisIndex[1] >= seismogram.getNumPoints()){
                seisIndex[1] = seismogram.getNumPoints() -1;
            }
            double[] minMax =
                data.getStatistics(seismogram).minMaxMean(seisIndex[0], seisIndex[1]);
            if(minMax[0] < minMaxAll[0]){
                minMaxAll[0] = minMax[0];
            }
            if(minMax[1] > minMaxAll[1]){
                minMaxAll[1] = minMax[1];
            }
            dataInTimeRange = true;
        }
        if(dataInTimeRange){
            return data.setCleanRange(new UnitRangeImpl(minMaxAll[0], minMaxAll[1], UnitImpl.COUNT));
        }else{
            return data.setCleanRange(DisplayUtils.ZERO_RANGE);
        }
    }

    protected MicroSecondTimeRange getTime(DataSetSeismogram seismo){
        if(currentTimeEvent != null){
            return currentTimeEvent.getTime(seismo);
        }
        return new MicroSecondTimeRange(seismo.getRequestFilter());
    }

    protected Map ampData = new HashMap();

    private double scale = 1.0;

    private double shift = 0;

    private List listeners = new ArrayList();

    private DataSetSeismogram[] seismos;

    protected TimeEvent currentTimeEvent;

    protected AmpEvent currentAmpEvent;

    private static Category logger = Category.getInstance(BasicAmpConfig.class.getName());
}//BasicAmpConfig
