package edu.sc.seis.fissuresUtil.display;

import java.util.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import org.apache.log4j.*;

/**
 * TimeConfigRegistrar.java
 *
 *
 * Created: Mon Jul  1 13:56:36 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class TimeConfigRegistrar implements TimeRangeConfig, TimeSyncListener{
    public TimeConfigRegistrar(){ 
	this(new BoundedTimeConfig(), null); 
    }

    public TimeConfigRegistrar(TimeSyncListener creator){ 
	this(new BoundedTimeConfig(), creator); 
    }
    
    public TimeConfigRegistrar(TimeRangeConfig timeConfig, TimeSyncListener creator){
	this.timeConfig = timeConfig;
	timeConfig.addTimeSyncListener(this);
	if(creator != null){
	    this.addTimeSyncListener(creator);
	}
	timeFinder = timeConfig.getTimeFinder();
	snapshot = new TimeSnapshot(seismos, null);
    }
    
    public void setTimeConfig(TimeRangeConfig newTimeConfig){ 
	timeConfig.removeTimeSyncListener(this);
	Iterator e = seismos.keySet().iterator();
	timeFinder = newTimeConfig.getTimeFinder();
	newTimeConfig.addTimeSyncListener(this);
	while(e.hasNext()){
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    timeConfig.removeSeismogram(current);
	    this.addSeismogram(current);
	    newTimeConfig.addSeismogram(current, (MicroSecondDate)seismos.get(current));
	    seismos.put(current, newTimeConfig.getTimeRange(current));
	}
	timeConfig = newTimeConfig;
    }

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(DataSetSeismogram seis){
	timeConfig.addSeismogram(seis, timeFinder.getBeginTime(seis));
	seismos.put(seis, timeConfig.getTimeRange(seis));	
    }

    public void addSeismogram(DataSetSeismogram seis, MicroSecondDate b){
	timeConfig.addSeismogram(seis, b);
	seismos.put(seis, timeConfig.getTimeRange(seis));
    }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(DataSetSeismogram seis){ 
	timeConfig.removeSeismogram(seis);
    }

    public boolean contains(DataSetSeismogram seis){
	return seismos.containsKey(seis);
    }

    public MicroSecondTimeRange getTimeRange(DataSetSeismogram seis){
	return (MicroSecondTimeRange)seismos.get(seis);
    }

    public MicroSecondTimeRange getTimeRange(){ 
	return timeConfig.getTimeRange();
    }
    
    /**
     * Adds a time sync listener to the list to be informed when a time sync event occurs
     * 
     * @param t the time sync listener to be added
     */
    public void addTimeSyncListener(TimeSyncListener t){ timeListeners.add(t); }

    /**
     * Removes a TimeSyncListener from the update list
     *
     * @param t the time sync listener to be removed
     */
    public void removeTimeSyncListener(TimeSyncListener t){ timeListeners.remove(t); }
	
    
    /**
     * Fire an event to all of the time sync listeners to update their time ranges
     *
     */
    public void updateTimeSyncListeners(){
	Iterator e = timeListeners.iterator();
	while(e.hasNext()){
	    ((TimeSyncListener)e.next()).updateTimeRange();
	}
    }

    public void updateTimeRange(){
	if(!taken){
	    snapshot.update(seismos, timeConfig.getTimeRange());
	}
	this.updateTimeSyncListeners(); 
    }

    public TimeSnapshot takeSnapshot(){
	taken = true;
	return snapshot;
    }

    public void returnSnapshot(){
	taken = false;
	snapshot.update(seismos, timeConfig.getTimeRange());
    }
    
    public void unregister(){ timeConfig.removeTimeSyncListener(this); }

    public void setDisplayInterval(TimeInterval t){ timeConfig.setDisplayInterval(t); }

    public void setBeginTime(MicroSecondDate b){ timeConfig.setBeginTime(b); }
    
    public void setBeginTime(DataSetSeismogram seismo, MicroSecondDate b){
	timeConfig.setBeginTime(seismo, b);
    }
    
    public void setAllBeginTime(MicroSecondDate b){ 
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    timeConfig.setBeginTime((DataSetSeismogram)e.next(), b);
	}
    }

    public void set(MicroSecondDate begin, TimeInterval displayInterval){ timeConfig.set(begin, displayInterval); }
    

    public void fireTimeRangeEvent(TimeSyncEvent event){ timeConfig.fireTimeRangeEvent(event);  }

    public TimeFinder getTimeFinder(){ return timeFinder; }

    public void setTimeFinder(TimeFinder tf){ timeFinder = tf; }

    protected boolean taken = false;

    protected HashMap seismos = new HashMap();

    protected TimeRangeConfig timeConfig;

    protected Set timeListeners = new HashSet();

    protected TimeSnapshot snapshot;
    
    protected TimeFinder timeFinder;

    protected Category logger = Category.getInstance(TimeConfigRegistrar.class.getName());
}// TimeConfigRegistrar
