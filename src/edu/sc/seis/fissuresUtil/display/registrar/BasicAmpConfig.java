package edu.sc.seis.fissuresUtil.display.registrar;
import java.util.*;

import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
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
    public BasicAmpConfig(){    }

    private static int j = 0;

    private int i = j++;

    public BasicAmpConfig(DataSetSeismogram[] seismos){
        add(seismos);
    }

    public String toString(){ return "Amp Config " + i; }

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
                ampEvent = new LazyAmpEvent(this);
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
                ampEvent = new LazyAmpEvent(this);
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

    public UnitRangeImpl getAmp() { return ampEvent.getAmp(); }

    public UnitRangeImpl getAmp(DataSetSeismogram seis) {
        return ampEvent.getAmp(seis);
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
            Iterator it = ampData.iterator();
            while(it.hasNext()){
                AmpConfigData current = (AmpConfigData)it.next();
                if(current.getDSS().equals(seis)){
                    return current;
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

    public void fireAmpEvent(){ fireAmpEvent(new LazyAmpEvent(this)); }

    private void fireAmpEvent(AmpEvent event){
        AmpListener[] al = getAmpListeners();
        for (int i = 0; i < al.length; i++){
            al[i].updateAmp(event);
        }
    }

    public void addListener(AmpListener listener){
        if(listener != null){
            synchronized(listeners){
                listeners.add(listener);
                ampListeners = null;
            }
            fireAmpEvent();
        }
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

    public void updateTime(TimeEvent timeEvent){
        currentTimeEvent = timeEvent;
        ampEvent = new LazyAmpEvent(this);
        fireAmpEvent(ampEvent);
    }

    public AmpEvent calculate(){
        boolean changed = false;
        AmpConfigData[] ad = getAmpData();
        for (int i = 0; i < ad.length; i++){
            if(ad[i]!=null && //checks for the seismogram being removed between getSeismograms and here
               ad[i].setTime(getTime(ad[i].getDSS()))){//checks for the time update equaling the old time
                if(setAmpRange(ad[i])){ //checks  if the new time changes the amp range
                    changed = true;// only generates a new amp event if the amp ranges have changed
                }
            }else if(ad[i] != null && ad[i].hasNewData()){
                setAmpRange(ad[i]);
                changed = true;
            }
        }

        if(changed || ampEvent instanceof LazyAmpEvent){
            ampEvent = recalculate();
        }

        return ampEvent;
    }


    public AmpEvent recalculate(){
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
        UnitRangeImpl genericRange = DisplayUtils.ONE_RANGE;
        if(ad.length == 1) {
            genericRange = UnitDisplayUtil.getRealWorldUnitRange(fullRange, ad[0].getDSS());
        }
        return new BasicAmpEvent(getSeismograms(ad), amps, genericRange);
    }

    protected boolean setAmpRange(AmpConfigData data){
        SeismogramIterator it = data.getIterator();
        if (!it.hasNext()) {
        //the iterator on this data has no next point, there is no amp data here
            return data.setRange(DisplayUtils.ONE_RANGE);
        }
        double[] minMaxMean = it.minMaxMean();
        return data.setRange(UnitDisplayUtil.getBestForDisplay(new UnitRangeImpl(minMaxMean[0],
                                                                                 minMaxMean[1],
                                                                                 it.getUnit())));

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

    private Set listeners = Collections.synchronizedSet(new HashSet());

    private List ampData = Collections.synchronizedList(new ArrayList());

    private AmpConfigData[] dataArray;

    private AmpListener[] ampListeners;

    private DataSetSeismogram[] seisArray;

    private TimeEvent currentTimeEvent;

    private AmpEvent ampEvent = new LazyAmpEvent(this);

    private static Category logger = Category.getInstance(BasicAmpConfig.class.getName());
}//BasicAmpConfig



