package edu.sc.seis.fissuresUtil.display;


import java.util.LinkedList;
import java.util.Iterator;
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
	this.timeConfig = new BoundedTimeConfig(this);
    }

    public TimeConfigRegistrar(TimeRangeConfig timeConfig){
	this.timeConfig = timeConfig;
    }

    
    public TimeConfigRegistrar(TimeConfigRegistrar timeConfig){
	this.timeConfig = timeConfig;
	timeConfig.addTimeSyncListener(this);
    }
    
    
    public void setRegistrar(TimeConfigRegistrar tr){
	if(timeConfig instanceof TimeConfigRegistrar){
	    ((TimeConfigRegistrar)timeConfig).removeTimeSyncListener(this);
	}
	tr.addTimeSyncListener(this);
	timeConfig = tr;
	updateTimeSyncListeners();
    }
    
    public void setTimeConfig(TimeRangeConfig timeConfig){ 
	this.timeConfig = timeConfig; 
    }

    public TimeRangeConfig getTimeConfig(){ return timeConfig.getTimeConfig(); }

    /**
     * Add the values in this seismogram to the configuration
     *
     * @param seis the seismogram to be added
     */
    public void addSeismogram(LocalSeismogram seis){
	timeConfig.addSeismogram(seis);
    }

    public void addSeismogram(LocalSeismogram seis, MicroSecondDate b){
	timeConfig.addSeismogram(seis, b);
    }

    /**
     * Remove the values from this seismogram from the configuration
     *
     * @param seis the seismogram to be removed
     */
    public void removeSeismogram(LocalSeismogram seis){ 
	timeConfig.removeSeismogram(seis);
    }

    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis){
	return timeConfig.getTimeRange(seis);
    }

    public MicroSecondTimeRange getTimeRange(){ return timeConfig.getTimeRange(); }

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
	this.updateTimeSyncListeners(); 
    }

    public TimeSnapshot takeSnapshot(){
	return timeConfig.takeSnapshot();
    }

    public void setDisplayInterval(TimeInterval t){ timeConfig.setDisplayInterval(t); }

    public void setBeginTime(MicroSecondDate b){ timeConfig.setBeginTime(b); }
    
    public void setAllBeginTime(MicroSecondDate b){ timeConfig.setAllBeginTime(b); }

    public void fireTimeRangeEvent(TimeSyncEvent event){ 
	logger.debug("firing time event");
	timeConfig.fireTimeRangeEvent(event); 
    }

    protected TimeRangeConfig timeConfig;

    protected LinkedList timeListeners = new LinkedList();

    protected Category logger = Category.getInstance(TimeConfigRegistrar.class.getName());
}// TimeConfigRegistrar
