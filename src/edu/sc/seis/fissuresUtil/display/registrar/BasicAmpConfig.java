package edu.sc.seis.fissuresUtil.display.registrar;
import java.util.*;

import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
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

public class BasicAmpConfig implements AmpConfig{
    public BasicAmpConfig(){}

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
    public void add(DataSetSeismogram[] seismos){
        boolean someAdded = false;
        synchronized(this.seismos){
            for(int i = 0; i < seismos.length; i++){
                if(!contains(seismos[i])){
                    this.seismos.add(seismos[i]);
                    ampData.put(seismos[i], new AmpConfigData(seismos[i], this));
                    someAdded = true;
                }
            }
        }
        if(someAdded){
            seisArray = null;
            currentAmpEvent = null;
            fireAmpEvent();
        }
    }

    /**
     * <code>remove</code> removes seismograms from this object
     *
     * @param seismo the seismograms to be removed
     */
    public void remove(DataSetSeismogram[] seismos){
        boolean someRemoved = false;
        synchronized(this.seismos){
            for(int i = 0; i < seismos.length; i++){
                if(this.seismos.remove(seismos[i])){
                    ampData.remove(seismos[i]);
                    someRemoved = true;
                }
            }
        }
        if(someRemoved){
            seisArray = null;
            currentAmpEvent = null;
            fireAmpEvent();
        }
    }

    public void clear(){
        remove(getSeismograms());
    }

    public DataSetSeismogram[] getSeismograms(){
        if(seisArray == null){
            synchronized(seismos){
                seisArray = new DataSetSeismogram[seismos.size()];
                seismos.toArray(seisArray);
            }
        }
        return seisArray;
    }

    public AmpConfigData getAmpData(DataSetSeismogram seis){
        return (AmpConfigData)ampData.get(seis);
    }


    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public boolean contains(DataSetSeismogram seismo){
        synchronized(seismos){
            if(seismos.contains(seismo)){
                return true;
            }
        }
        return false;
    }

    public void reset(){
        reset(getSeismograms());
    }

    public void reset(DataSetSeismogram[] seismos){
        boolean someReset = false;
        for(int i = 0; i < seismos.length; i++){
            if(contains(seismos[i])){
                getAmpData(seismos[i]).reset();
                someReset = true;
            }
        }
        if(someReset){
            fireAmpEvent();
        }
    }

    public void shaleAmp(double shift, double scale){
        shaleAmp(shift, scale, getSeismograms());
    }

    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos){
        for(int i = 0; i < seismos.length; i++){
            getAmpData(seismos[i]).shale(shift, scale);
        }
        fireAmpEvent();
    }

    public AmpEvent fireAmpEvent(){
        return  fireAmpEvent(calculateAmp());
    }

    private AmpEvent fireAmpEvent(AmpEvent event){
        AmpListener[] al = getAmpListeners();
        for (int i = 0; i < ampListeners.length; i++){
            al[i].updateAmp(event);
        }
        return event;
    }

    public void addListener(AmpListener listener){
        synchronized(listeners){
            listeners.add(listener);
            ampListeners = null;
        }
        fireAmpEvent();
    }

    public void removeListener(AmpListener listener){
        synchronized(listeners){
            listeners.remove(listener);
            ampListeners = null;
        }
    }

    public AmpListener[] getAmpListeners(){
        synchronized(listeners){
            if(ampListeners == null){
                ampListeners = new AmpListener[listeners.size()];
                listeners.toArray(ampListeners);
            }
        }
        return ampListeners;
    }

    public AmpEvent updateAmpTime(TimeEvent timeEvent){
        currentTimeEvent = timeEvent;
        return calculateAmp();
    }

    private AmpEvent calculateAmp(){
        boolean changed = false;
        DataSetSeismogram[] seis = getSeismograms();
        for (int i = 0; i < seis.length; i++){
            AmpConfigData current = getAmpData(seis[i]);

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
            currentAmpEvent = recalculateAmp();
        }

        return currentAmpEvent;
    }


    protected AmpEvent recalculateAmp(){
        DataSetSeismogram[] seis = getSeismograms();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < seis.length; i++){
            UnitRangeImpl current = getAmpData(seis[i]).getRange();
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
        UnitRangeImpl[] amps = new UnitRangeImpl[seis.length];
        for(int i = 0; i < seis.length; i++){
            amps[i] = fullRange;
        }
        return new AmpEvent(seis, amps);
    }

    protected boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = getAmpData(seismo);
        SeismogramIterator it = data.getIterator();
        if (!it.hasNext()) {//if the iterator on this time range has no next
            return data.setRange(DisplayUtils.ZERO_RANGE);//point, there is
        } // no amp data here
        double[] minMaxMean = it.minMaxMean();
        return data.setRange(new UnitRangeImpl(minMaxMean[0],
                                               minMaxMean[1],
                                               UnitImpl.COUNT));

    }

    private MicroSecondTimeRange getTime(DataSetSeismogram seismo){
        if(currentTimeEvent != null){
            return currentTimeEvent.getTime(seismo);
        }
        return new MicroSecondTimeRange(seismo.getRequestFilter());
    }



    private void checkSeismogramUnits(DataSetSeismogram seismo){
        AmpConfigData data = getAmpData(seismo);
        LocalSeismogramImpl[] seismograms = data.getIterator().getSeismograms();
        UnitImpl seisUnit = null;
        for(int i = 0; i < seismograms.length; i++){
            LocalSeismogramImpl seis = seismograms[i];
            if (seisUnit == null) {
                seisUnit = seis.getUnit();
            }
            if ( ! seis.getUnit().equals(seisUnit)) {
                // very unusuall for seismograms not to have the same unit
                // recorded from the same channel
                throw new IllegalArgumentException("Seismograms in the same DataSetSeismogram do not have the same units!");
            }
        }
    }

    private Map ampData = new HashMap();

    private List listeners = Collections.synchronizedList(new ArrayList());

    private List seismos = Collections.synchronizedList(new ArrayList());

    private DataSetSeismogram[] seisArray;

    AmpListener[] ampListeners;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private static Category logger = Category.getInstance(BasicAmpConfig.class.getName());
}//BasicAmpConfig
