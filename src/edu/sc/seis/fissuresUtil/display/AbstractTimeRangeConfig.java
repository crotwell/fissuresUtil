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
    
    public AbstractTimeRangeConfig(){
	timeFinder = new EdgeTimeFinder(this);
    }

    public AbstractTimeRangeConfig(TimeConfigRegistrar registrar){
	timeListeners.add(registrar);
    }

    /**
     * Takes the information from the passed seismogram and uses it along with the information already taken from other seismograms 
     * along with an internal set of calculations to determine the amount of time the passed seismogram will be displayed
     *
     * @param seis the seismogram to be displayed
     * @return the time it will be displayed
     */
    public abstract MicroSecondTimeRange getTimeRange(DataSetSeismogram seis);

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
    public synchronized void addSeismogram(DataSetSeismogram seis){ 
	seismos.put(seis, ((LocalSeismogramImpl)seis.getSeismogram()).getBeginTime()); 
    }

    /** Adds a seismogram that has a reference set by the user
     */
    public synchronized void addSeismogram(DataSetSeismogram seis, MicroSecondDate time){ 
	seismos.put(seis, time);
	updateTimeSyncListeners();
    }

    /**
     * Remove the values of this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(DataSetSeismogram seis){ seismos.remove(seis); }
    
    
    
    public boolean contains(DataSetSeismogram seis){
	return seismos.containsKey(seis);
    }
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public synchronized void updateTimeSyncListeners(){
	Iterator e = timeListeners.iterator();
	while(e.hasNext()){
	    ((TimeSyncListener)e.next()).updateTimeRange();
	}
    }

    public void addTimeSyncListener(TimeSyncListener t){ timeListeners.add(t); }
    
    public void removeTimeSyncListener(TimeSyncListener t){ timeListeners.remove(t); }	

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
	    DataSetSeismogram curr = ((DataSetSeismogram)e.next());
	    this.addSeismogram(curr, ((MicroSecondDate)newData.get(curr)));
	}
	updateTimeSyncListeners();
    } 
    
    public synchronized void setRelativeTime(DataSetSeismogram seis, MicroSecondDate time){
	((MicroSecondTimeRange)seismos.get(seis)).setBeginTime(time);
	updateTimeSyncListeners();
    }

    public synchronized  void setDisplayInterval(TimeInterval t){
	displayInterval = t;
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    MicroSecondTimeRange current = (MicroSecondTimeRange)seismos.get(e.next());
	    current.setEndTime(current.getBeginTime().add(t));
	}
	updateTimeSyncListeners();
    }

    public synchronized void setBeginTime(MicroSecondDate b){ 
	beginTime = b;
	updateTimeSyncListeners();
    }

    public synchronized void setBeginTime(DataSetSeismogram seis, MicroSecondDate b){
	((MicroSecondTimeRange)seismos.get(seis)).setBeginTime(b);
	updateTimeSyncListeners();
    }

    public synchronized void setAllBeginTime(MicroSecondDate b){
	beginTime = b;
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    MicroSecondTimeRange current = (MicroSecondTimeRange)seismos.get(e.next());
	    current.setBeginTime(b);
	    current.setEndTime(b.add(displayInterval));
	}
	updateTimeSyncListeners();
    }

    public synchronized void set(MicroSecondDate b, TimeInterval t){
	beginTime = b;
	displayInterval = t;
	setDisplayInterval(t);
	setAllBeginTime(b);
	updateTimeSyncListeners();
    }

    public TimeFinder getTimeFinder(){ return timeFinder; }

    public void setTimeFinder(TimeFinder tf){ timeFinder = tf; }

    public synchronized TimeSnapshot takeSnapshot(){
	return new TimeSnapshot((HashMap)seismos.clone(), this.getTimeRange());
    }
	
    protected TimeFinder timeFinder;

    protected MicroSecondDate beginTime;
    
    protected TimeInterval displayInterval;

    protected HashMap seismos = new HashMap();
    
    protected Set timeListeners = new HashSet();

}// AbstractTimeRangeConfig
