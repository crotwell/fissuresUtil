package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
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

public class BasicAmpConfig implements AmpConfig{
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
        boolean someAdded = false;
        for(int i = 0; i < seismos.length; i++){
            if(!ampData.containsKey(seismos[i])){
                ampData.put(seismos[i], new AmpConfigData(seismos[i], this));
                someAdded = true;
            }
        }
        if(someAdded){
            currentAmpEvent = null;
            this.seismos = null;
            fireAmpEvent();
        }
    }

    /**
     * <code>remove</code> removes seismograms from this object
     *
     * @param seismo the seismograms to be removed
     */
    public boolean remove(DataSetSeismogram[] seismos){
        boolean allRemoved = true;
        boolean someRemoved = false;
        synchronized(this){
            for(int i = 0; i < seismos.length; i++){
                if(!ampData.containsKey(seismos[i])){
                    allRemoved = false;
                }else{
                    someRemoved = true;
                    ampData.remove(seismos[i]);
                }
            }
        }
        if(someRemoved){
            this.seismos = null;
            currentAmpEvent = null;
            fireAmpEvent();
        }
        return allRemoved;
    }

    public DataSetSeismogram[] getSeismograms(){
        if(seismos == null){
            synchronized(this){
                seismos = (DataSetSeismogram[])ampData.keySet().toArray(new DataSetSeismogram[ampData.size()]);
            }
        }
        return seismos;
    }


    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public synchronized boolean contains(DataSetSeismogram seismo){
        if(ampData.containsKey(seismo)){
            return true;
        }
        return false;
    }

    public void reset(){
        reset(getSeismograms());
    }

    public void reset(DataSetSeismogram[] seismos){
        synchronized(this){
            for(int i = 0; i < seismos.length; i++){
                ((AmpConfigData)ampData.get(seismos[i])).reset();
            }
        }
        fireAmpEvent();
    }

    public void shaleAmp(double shift, double scale){
        shaleAmp(shift, scale, getSeismograms());
    }

    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos){
        synchronized(this){
            for(int i = 0; i < seismos.length; i++){
                ((AmpConfigData)ampData.get(seismos[i])).shale(shift, scale);
            }
        }
        fireAmpEvent();
    }

    public AmpEvent fireAmpEvent(){
        return  fireAmpEvent(calculateAmp());
    }

    private AmpEvent fireAmpEvent(AmpEvent event){
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

    private synchronized AmpEvent calculateAmp(){
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
            currentAmpEvent = recalculateAmp();
        }

        return currentAmpEvent;
    }


    protected synchronized AmpEvent recalculateAmp(){
        Iterator e = ampData.keySet().iterator();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        while(e.hasNext()){
            UnitRangeImpl current = ((AmpConfigData)ampData.get(e.next())).getRange();
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
        return new AmpEvent(seismos, amps);
    }

    protected boolean setAmpRange(DataSetSeismogram seismo){
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);
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
        AmpConfigData data = (AmpConfigData)ampData.get(seismo);
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

    protected Map ampData = new HashMap();

    private List listeners = new ArrayList();

    private DataSetSeismogram[] seismos;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private static Category logger = Category.getInstance(BasicAmpConfig.class.getName());
}//BasicAmpConfig


