package edu.sc.seis.fissuresUtil.display.registrar;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registrar.java
 *
 *
 * Created: Tue Sep  3 13:08:34 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class Registrar implements TimeConfig, AmpConfig, AmpListener, TimeListener{
    //TODO add checks for existance of time and amp listeners
    //and only create amp and time configs based on that
    /**
     * Creates a new <code>Registrar</code> instance with a BasicTimeConfig and a RMeanAmpConfig.
     *
     * @param seismo an intial <code>DataSetSeismogram</code>
     */
    public Registrar(DataSetSeismogram[] seismos){
        this(seismos, new BasicTimeConfig(seismos), new RMeanAmpConfig(seismos));
    }

    /**
     * Creates a new <code>Registrar</code> instance with the passed TimeConfig and a RMeanAmpConfig.
     *
     * @param seismo an initial <code>DataSetSeismogram</code>
     * @param timeConfig this Registrar's <code>TimeConfig</code>
     */
    public Registrar(DataSetSeismogram[] seismos, TimeConfig timeConfig){
        this(seismos, timeConfig, new RMeanAmpConfig(seismos));
    }

    /**
     * Creates a new <code>Registrar</code> instance with the passed AmpConfig and a BasicTimeConfig.
     *
     * @param seismo an initial <code>DataSetSeismogram</code>
     * @param ampConfig this Registrar's <code>AmpConfig</code>
     */
    public Registrar(DataSetSeismogram[] seismos, AmpConfig ampConfig){
        this(seismos, new BasicTimeConfig(seismos), ampConfig);
    }

    /**
     * Creates a new <code>Registrar</code> instance with the passed AmpConfig and TimeConfig.
     * It sets this Registrar to use the TimeConfig and AmpConfig and then adds the seismogram
     * to all 3 objects.
     * @param seismo the initial <code>DataSetSeismogram</code>
     *
     */
    public Registrar(DataSetSeismogram[] seismos, TimeConfig timeConfig, AmpConfig ampConfig){
        setTimeConfig(timeConfig);
        setAmpConfig(ampConfig);
        add(seismos);
    }

    /**
     * <code>setTimeConfig</code> sets this registrar to use the passed time config and
     * adds this registrar's seismograms to it.  It also removes the seismograms from the
     * previous time config if it exists, and unregisters the registrar as a listener
     * @param newConfig a <code>TimeConfig</code> value
     */
    public void setTimeConfig(TimeConfig newConfig){
        DataSetSeismogram[] seismosArray = getSeismograms();
        if(seismosArray.length > 0){
            timeConfig.removeListener(this);
            removeFromTimeConfig(seismosArray);
        }
        timeConfig = newConfig;
        addToTimeConfig(seismosArray);
        timeConfig.addListener(this);
    }

    public synchronized TimeConfig getTimeConfig(){ return timeConfig; }

    /**
     * <code>setAmpConfig</code> sets this registrar to use the passed AmpConfig and
     * adds this registrar's seismograms to it.  If there is already an AmpConfig for
     * this registrar, all of this registrar's seismograms are removed from it, and
     * the registrar removes itself as a listener from it.
     * @param newConfig the new <code>AmpConfig</code> for this Registrar
     */
    public void setAmpConfig(AmpConfig newConfig){
        DataSetSeismogram[] seismosArray = getSeismograms();
        if(ampConfig != null){
            if(seismosArray.length > 0){
                ampConfig.removeListener(this);
                removeFromAmpConfig(seismosArray);
            }
        }
        ampConfig = newConfig;
        addToAmpConfig(seismosArray);
        newConfig.addListener(this);
    }

    public AmpConfig getAmpConfig(){ return ampConfig; };

    public TimeEvent getLatestTime(){ return timeEvent; }

    public AmpEvent getLatestAmp(){ return ampEvent; }

    //Implementation of DataSetSeismogramRegistrar

    /**
     *  <code>add</code> adds the seismogram to the registrar and the time and amp
     * configs in a block.  Should be faster than adding each individually.
     * @param newSeismos an array of <code>DataSetSeismograms</code> to be added
     */
    public void add(DataSetSeismogram[] newSeismos){
        boolean someAdded = false;
        synchronized(seismos){
            for(int i = 0; i < newSeismos.length; i++){
                if(!seismos.contains(newSeismos[i])){
                    seismos.add(newSeismos[i]);
                    seismosArray = null;
                    someAdded = true;
                }
            }
        }
        if(someAdded){
            addToTimeConfig(newSeismos);
            addToAmpConfig(newSeismos);
        }
    }

    /**
     * <code>remove</code> removes the array of seismograms from this
     * registrar and its associated configs.
     * @param oldSeismos the <code>DataSetSeismogram[]</code> to be removed
     * @return true if the seismograms are all removed.
     */
    public void remove(DataSetSeismogram[] oldSeismos){
        boolean someRemoved = false;
        synchronized(seismos){
            for(int i = 0; i < oldSeismos.length; i++){
                seismos.remove(oldSeismos[i]);
                seismosArray = null;
            }
        }
        if(someRemoved){
            removeFromAmpConfig(oldSeismos);
            removeFromTimeConfig(oldSeismos);
        }
    }

    public void clear(){
        synchronized(seismos){
            seismos.clear();
            seismosArray = null;
        }
        clearAmpConfig();
        clearTimeConfig();
    }

    /**
     * <code>contains</code> checks if the registrar contains a particular
     * seismogram
     *
     * @return true if the registrar has the given seismogram
     */
    public boolean contains(DataSetSeismogram seismo){
        synchronized(seismos){
            if(seismos.contains(seismo)){
                return true;
            }
        }
        return false;
    }

    /**
     * returns all of the seismograms held by this registrar
     *
     * @return an array containing all of this registrar's seismograms
     */
    public DataSetSeismogram[] getSeismograms(){
        if(seismosArray == null){
            synchronized(seismos){
                seismosArray = new DataSetSeismogram[seismos.size()];
                seismos.toArray(seismosArray);
            }
        }
        return seismosArray;
    }

    public AmpConfigData[] getAmpData(){ return ampConfig.getAmpData(); }

    public AmpConfigData getAmpData(DataSetSeismogram seis){
        return ampConfig.getAmpData(seis);
    }

    /**
     * <code>reset</code> calls reset() on this registrar's configs.
     *
     */
    public void reset(){
        resetTimeConfig();
        resetAmpConfig();
    }

    /**
     * <code>reset</code> calls reset(DataSetSeismogram[]) on this registrar's configs
     *
     * @param seismos the seismograms to be reset
     */
    public void reset(DataSetSeismogram[] seismos){
        resetAmpConfig(seismos);
        resetTimeConfig(seismos);
    }

    //End of implementation of DataSetSeismogramRegistrar
    //Implementation of TimeConfig

    /**
     * <code>shaleTime</code> merely passes the shale onto the timeConfig
     *
     *
     */
    public void shaleTime(double shift, double scale){ timeConfig.shaleTime(shift, scale); }

    /**
     * passes the shale onto the time config
     *
     */
    public void shaleTime(double shift, double scale, DataSetSeismogram[] seismos){
        timeConfig.shaleTime(shift, scale, seismos);
    }

    /**
     * <code>add</code> adds the listener to the group of objects that are
     * updated when this time registrar changes
     * @param listener a <code>TimeListener</code> that will be updated
     * as this config changes
     */
    public void addListener(TimeListener listener){
        synchronized(timeListeners){
            timeListeners.add(listener);
            tlArray = null;
        }
        fireTimeEvent();
    }

    /**
     * <code>remove</code> removes listener from the update group
     *
     * @param listener a <code>TimeListener</code> that will no longer
     * receive updates from this registrar
     */
    public void removeListener(TimeListener listener){
        synchronized(timeListeners){
            timeListeners.remove(listener);
            tlArray = null;
        }
    }

    public TimeListener[] getTimeListeners(){
        if(tlArray == null){
            synchronized(timeListeners){
                tlArray = new TimeListener[timeListeners.size()];
                timeListeners.toArray(tlArray);
            }
        }
        return tlArray;
    }

    /**
     * <code>fireTimeEvent</code> causes the registrar to fire its latest TimeEvent
     * to all of its <code>TimeListener</code> objects
     *
     * @return the ConfigEvent fired
     */
    public TimeEvent fireTimeEvent(){
        return fireTimeEvent(timeEvent);
    }

    /**
     * Describe <code>fireTimeEvent</code> method here.
     *
     * @param event a <code>ConfigEvent</code> value
     * @return a <code>ConfigEvent</code> value
     */
    public TimeEvent fireTimeEvent(TimeEvent event){
        fireSolelyTimeEvent(event);
        ConfigListener[] cl = getGlobalListeners();
        for (int i = 0; i < cl.length; i++){
            cl[i].updateTime(event);
        }
        return event;
    }

    private void fireSolelyTimeEvent(TimeEvent event){
        TimeListener[] tl = getTimeListeners();
        for (int i = 0; i < tl.length; i++){
            tl[i].updateTime(event);
        }
    }


    //End of implementation of TimeConfig
    //Implementation of AmpConfig

    /**
     * passes the shale onto the amp config
     */
    public void shaleAmp(double shift, double scale){ ampConfig.shaleAmp(shift, scale); }

    /**
     *  passes the shale onto the amp config
     */
    public void shaleAmp(double shift, double scale, DataSetSeismogram[] seismos){
        ampConfig.shaleAmp(shift, scale, seismos);
    }


    public void addListener(AmpListener listener){
        synchronized(ampListeners){
            ampListeners.add(listener);
            alArray = null;
        }
        fireAmpEvent();
    }

    public void removeListener(AmpListener listener){
        synchronized(ampListeners){
            ampListeners.remove(listener);
            alArray = null;
        }
    }

    private AmpListener[] getAmpListeners(){
        if(alArray == null){
            synchronized(ampListeners){
                alArray = new AmpListener[ampListeners.size()];
                ampListeners.toArray(alArray);
            }
        }
        return alArray;
    }

    public AmpEvent fireAmpEvent(){
        return fireAmpEvent(ampEvent);
    }

    public AmpEvent fireAmpEvent(AmpEvent event){
        fireSolelyAmpEvent(event);
        ConfigListener[] cl = getGlobalListeners();
        for (int i = 0; i < cl.length; i++){
            cl[i].updateAmp(event);
        }
        return event;
    }

    public AmpEvent updateAmpTime(TimeEvent event){
        return ampConfig.updateAmpTime(event);
    }

    private void fireSolelyAmpEvent(AmpEvent event){
        AmpListener[] al = getAmpListeners();
        for (int i = 0; i < al.length; i++){
            al[i].updateAmp(event);
        }
    }

    //End of implementation of AmpConfig
    //Implementation of TimeEventListener

    public void updateTime(TimeEvent tEvent){
        if(ampConfig != null && (getAmpListeners().length > 0 ||getGlobalListeners().length > 0)){
            AmpEvent aEvent = ampConfig.updateAmpTime(tEvent);
            if(aEvent != null){
                fireGlobalEvent(new ConfigEvent(getSeismograms(), tEvent, aEvent));
                fireSolelyAmpEvent(aEvent);
                fireSolelyTimeEvent(tEvent);
                ampEvent = aEvent;
            }
        }else{
            fireTimeEvent(tEvent);
        }
        timeEvent = tEvent;
    }

    //End of implementation of TimeListener
    //Implementation of AmpListener

    public void updateAmp(AmpEvent event){
        fireAmpEvent(event);
        ampEvent = event;
    }


    //End of implementation of AmpEventListener


    public void addListener(ConfigListener listener){
        synchronized(configListeners){
            configListeners.add(listener);
            clArray = null;
        }
        fireGlobalEvent(new ConfigEvent(getSeismograms(), timeEvent, ampEvent));
    }

    public void removeListener(ConfigListener listener){
        synchronized(configListeners){
            configListeners.remove(listener);
            clArray = null;
        }
    }

    public ConfigListener[] getGlobalListeners(){
        if(clArray == null){
            synchronized(configListeners){
                clArray = new ConfigListener[configListeners.size()];
                configListeners.toArray(clArray);
            }
        }
        return clArray;
    }

    public ConfigEvent fireGlobalEvent(ConfigEvent event){
        ConfigListener[] cl = getGlobalListeners();
        for (int i = 0; i < cl.length; i++){
            cl[i].update(event);
        }
        return event;
    }


    /* Private methods to allow Registrars to communicate with other registrars to
     * specify if a mutator method on a time config or amp config is for an ampconfig
     * or timeconfig.
     */
    private void addToTimeConfig(DataSetSeismogram[] seismograms){
        if(timeConfig instanceof Registrar){
            ((Registrar)timeConfig).addToTimeConfig(seismograms);
        }else{
            timeConfig.add(seismograms);
        }
    }

    private void removeFromTimeConfig(DataSetSeismogram[] seismograms){
        if(timeConfig instanceof Registrar){
            ((Registrar)timeConfig).removeFromTimeConfig(seismograms);
        }else{
            timeConfig.remove(seismograms);
        }
    }

    private void resetTimeConfig(){
        if(timeConfig instanceof Registrar){
            ((Registrar)timeConfig).resetTimeConfig();
        }else{
            timeConfig.reset();
        }
    }

    private void resetTimeConfig(DataSetSeismogram [] seismograms){
        if(timeConfig instanceof Registrar){
            ((Registrar)timeConfig).resetTimeConfig(seismograms);
        }else{
            timeConfig.reset(seismograms);
        }
    }

    private void clearTimeConfig(){
        if(timeConfig instanceof Registrar){
            ((Registrar)timeConfig).clearTimeConfig();
        }else{
            timeConfig.clear();
        }
    }

    private void addToAmpConfig(DataSetSeismogram[] seismograms){
        if(ampConfig instanceof Registrar){
            ((Registrar)ampConfig).addToAmpConfig(seismograms);
        }else{
            ampConfig.add(seismograms);
        }
    }

    private void removeFromAmpConfig(DataSetSeismogram[] seismograms){
        if(ampConfig instanceof Registrar){
            ((Registrar)ampConfig).removeFromAmpConfig(seismograms);
        }else{
            ampConfig.remove(seismograms);
        }
    }

    private void resetAmpConfig(){
        if(ampConfig instanceof Registrar){
            ((Registrar)ampConfig).resetAmpConfig();
        }else{
            ampConfig.reset();
        }
    }

    private void resetAmpConfig(DataSetSeismogram [] seismograms){
        if(ampConfig instanceof Registrar){
            ((Registrar)ampConfig).resetAmpConfig(seismograms);
        }else{
            ampConfig.reset(seismograms);
        }
    }

    private void clearAmpConfig(){
        if(ampConfig instanceof Registrar){
            ((Registrar)ampConfig).clearAmpConfig();
        }else{
            ampConfig.clear();
        }
    }

    private TimeConfig timeConfig;

    private AmpConfig ampConfig;

    private List seismos = Collections.synchronizedList(new ArrayList());

    private DataSetSeismogram[] seismosArray;

    private List timeListeners = Collections.synchronizedList(new ArrayList());

    private TimeListener[] tlArray;

    private List ampListeners = Collections.synchronizedList(new ArrayList());

    private AmpListener[] alArray;

    private List configListeners = Collections.synchronizedList(new ArrayList());

    private ConfigListener[] clArray;

    private TimeEvent timeEvent = new EmptyTimeEvent(DisplayUtils.ONE_TIME);

    private AmpEvent ampEvent;
}// Registrar

