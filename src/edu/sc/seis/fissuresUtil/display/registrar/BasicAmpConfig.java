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
        add(seismos);
    }

    /**
     * <code>add</code> is the method used to add a seismogram to this object
     *
     * @param seismo the seismogram to be added
     */
    public void add(DataSetSeismogram[] seismos){
        boolean someAdded = false;
        synchronized(ampData){
            for(int i = 0; i < seismos.length; i++){
                if(!contains(seismos[i])){
                    ampData.add(new AmpConfigData(seismos[i], this));
                    someAdded = true;
                }
            }

            if(someAdded){
                dataArray = null;
                currentAmpEvent = null;
                seisArray = null;
            }
        }
        if(someAdded){
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
        synchronized(ampData){
            for(int i = 0; i < seismos.length; i++){
                ListIterator lit = ampData.listIterator();
                while(lit.hasNext()){
                    if(((AmpConfigData)lit.next()).getDSS().equals(seismos[i])){
                        lit.remove();
                        someRemoved = true;
                    }
                }
            }
            if(someRemoved){
                dataArray = null;
                currentAmpEvent = null;
                seisArray = null;
            }
        }
        if(someRemoved){
            fireAmpEvent();
        }
    }

    public void clear(){
        remove(getSeismograms());
    }

    public DataSetSeismogram[] getSeismograms(){
        return getSeismograms(getAmpData());
    }

    public DataSetSeismogram[] getSeismograms(AmpConfigData[] ampData){
        if(seisArray == null){
            seisArray = AmpConfigData.getSeismograms(ampData);
        }
        return seisArray;
    }

    //TODO make get seis and amp data method so that cached copies of each can be
    //returned in sync
    public AmpConfigData[] getAmpData(){
        synchronized(ampData){
            if(dataArray == null){
                dataArray = new AmpConfigData[ampData.size()];
                Iterator it = ampData.iterator();
                int i = 0;
                while(it.hasNext()){
                    dataArray[i] = (AmpConfigData)it.next();
                    i++;
                }
            }
            return dataArray;
        }
    }

    public AmpConfigData getAmpData(DataSetSeismogram seis){
        synchronized(ampData){
            ListIterator it = ampData.listIterator();
            while(it.hasNext()){
                if(((AmpConfigData)it.next()).equals(seis)){
                    return (AmpConfigData)it.previous();
                }
            }
        }
        return null;
    }


    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public boolean contains(DataSetSeismogram seismo){
        if(getAmpData(seismo) != null) return true;
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
        return  fireAmpEvent(calculateAmp());//new LazyAmpEvent(this));
    }

    private AmpEvent fireAmpEvent(AmpEvent event){
        AmpListener[] al = getAmpListeners();
        for (int i = 0; i < al.length; i++){
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
        return calculateAmp();//LazyAmpEvent(this);
    }

    public AmpEvent calculateAmp(){
        boolean changed = false;
        AmpConfigData[] ad = getAmpData();
        for (int i = 0; i < ad.length; i++){
            if(ad[i]!=null && //checks for the seismogram being removed between getSeismograms and here
               ad[i].setTime(getTime(ad[i].getDSS()))){ //checks for the time update equaling the old time
                if(setAmpRange(ad[i])){ //checks if the new time changes the amp range
                    changed = true;// only generates a new amp event if the amp ranges have changed
                }
            }else if(ad[i] != null && ad[i].hasNewData()){
                setAmpRange(ad[i]);
                changed = true;
            }
        }

        if(changed || currentAmpEvent == null){
            currentAmpEvent = recalculateAmp();
        }

        return currentAmpEvent;
    }


    protected AmpEvent recalculateAmp(){
        AmpConfigData[] ad = getAmpData();
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < ad.length; i++){
            UnitRangeImpl current = ad[i].getRange();
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
        UnitRangeImpl[] amps = new UnitRangeImpl[ad.length];
        for(int i = 0; i < ad.length; i++){
            amps[i] = fullRange;
        }
        return new BasicAmpEvent(getSeismograms(ad), amps);
    }

    protected boolean setAmpRange(AmpConfigData data){
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



    private void checkSeismogramUnits(AmpConfigData data){
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

    private List listeners = Collections.synchronizedList(new ArrayList());

    private List ampData = Collections.synchronizedList(new ArrayList());

    private AmpConfigData[] dataArray;

    private AmpListener[] ampListeners;

    private DataSetSeismogram[] seisArray;

    private TimeEvent currentTimeEvent;

    private AmpEvent currentAmpEvent;

    private static Category logger = Category.getInstance(BasicAmpConfig.class.getName());
}//BasicAmpConfig

