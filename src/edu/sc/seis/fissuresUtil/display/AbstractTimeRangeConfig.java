package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
/**
 * AbstractTimeRangeConfig.java
 *
 *
 * Created: Mon Jun  3 12:51:35 2002
 *
 * @author Charlie Groves
 * @version
 */

public abstract class AbstractTimeRangeConfig implements TimeRangeConfig{
    
    public AbstractTimeRangeConfig(){}

    public AbstractTimeRangeConfig(TimeConfigRegistrar registrar){
	this.registrar = registrar;
    }

    public void setRegistrar(TimeConfigRegistrar newRegistrar){ registrar = newRegistrar; }

    public TimeConfigRegistrar getRegistrar(){ return registrar; }

    /**
     * Takes the information from the passed seismogram and uses it along with the information already taken from other seismograms 
     * along with an internal set of calculations to determine the amount of time the passed seismogram will be displayed
     *
     * @param seis the seismogram to be displayed
     * @return the time it will be displayed
     */
    public abstract MicroSecondTimeRange getTimeRange(LocalSeismogram seis);

    /**
     * Get the total display time regardless of a particular seismogram, for things such as axes
     *
     * @return the time range being displayed
     */
    public abstract MicroSecondTimeRange getTimeRange();

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public synchronized void addSeismogram(LocalSeismogram seis){ seismos.put(seis, ((LocalSeismogramImpl)seis).getBeginTime()); }

    /** Adds a seismogram that has a reference set by the user
     */
    public synchronized void addSeismogram(LocalSeismogram seis, MicroSecondDate time){ 
	seismos.put(seis, time);
	registrar.updateTimeSyncListeners();
    }

    /**
     * Remove the values of this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(LocalSeismogram seis){ seismos.remove(seis); }
    
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public synchronized void updateTimeSyncListeners(){
	registrar.updateTimeSyncListeners();
    }

    /**
     * Takes the information from the TimeSyncEvent, adjusts the MicroSecondTimeRange, and updates according to the information in the 
     * event
     *
     */
    public abstract void fireTimeRangeEvent(TimeSyncEvent e);

    public HashMap getData(){ return this.seismos; }

    public void setData(HashMap newData){ 
	Iterator e = newData.keySet().iterator();
	while(e.hasNext()){
	    LocalSeismogram curr = ((LocalSeismogram)e.next());
	    this.addSeismogram(curr, ((MicroSecondDate)newData.get(curr)));
	}
	registrar.updateTimeSyncListeners();
    } 
    
    public synchronized void setRelativeTime(LocalSeismogram seis, MicroSecondDate time){
	seismos.put(seis, time);
	registrar.updateTimeSyncListeners();
    }

    public synchronized  void setDisplayInterval(TimeInterval t){
	displayInterval = t;
	registrar.updateTimeSyncListeners();
    }

    public synchronized void setBeginTime(MicroSecondDate b){ 
	beginTime = b;
	registrar.updateTimeSyncListeners();
    }

    public synchronized void setAllBeginTime(MicroSecondDate b){
	beginTime = b;
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext())
	    seismos.put(e.next(), b);
	registrar.updateTimeSyncListeners();
    }

    public TimeRangeConfig getTimeConfig(){ return this; } 
    
    protected MicroSecondDate beginTime;
    
    protected TimeInterval displayInterval;

    protected HashMap seismos = new HashMap();
    
    protected TimeConfigRegistrar registrar;

}// AbstractTimeRangeConfig
