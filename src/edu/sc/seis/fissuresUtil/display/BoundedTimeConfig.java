package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
/**
 * BoundedTimeConfig is a TimeRangeConfig implementation that allows absolute time ranges to be set for the display.  It only displays
 * whatever is inside of a user defined time range.  If none is specified, it displays the entire time range of the first seismogram
 * it receives.
 *
 *
 * Created: Mon May 27 09:18:10 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class BoundedTimeConfig extends AbstractTimeRangeConfig{

    public synchronized MicroSecondTimeRange getTimeRange(DataSetSeismogram seis){
	return (MicroSecondTimeRange)seismos.get(seis);
    }

    public synchronized MicroSecondTimeRange getTimeRange(){
	return new MicroSecondTimeRange(beginTime, beginTime.add(displayInterval));
    }

    public synchronized void addSeismogram(DataSetSeismogram seis){
	if(beginTime == null){
	    beginTime = timeFinder.getBeginTime(seis);
	   
	}
 	if(displayInterval == null){
	    displayInterval = timeFinder.getDisplayInterval(seis);
	}
	seismos.put(seis, new MicroSecondTimeRange(beginTime, beginTime.add(displayInterval)));
	super.updateTimeSyncListeners();
    }	

    public synchronized void addSeismogram(DataSetSeismogram seis, MicroSecondDate time){
	if(beginTime == null)
	    beginTime = time;
	if(displayInterval == null || displayInterval.getValue() == 0)
	    displayInterval = timeFinder.getDisplayInterval(seis);
	seismos.put(seis, new MicroSecondTimeRange(beginTime, beginTime.add(displayInterval)));
	super.updateTimeSyncListeners();
    }
    
    /**  When BoundedTimeConfig receives a TimeSyncEvent, it merely changes the time range by the percentages contained in the 
     *   TimeSyncEvent
     */
    public synchronized void fireTimeRangeEvent(TimeSyncEvent e){
	double begin = e.getBegin();
	double end = e.getEnd();
	double intv = displayInterval.getValue();
	MicroSecondDate endTime = new MicroSecondDate((long)(beginTime.add(displayInterval).getMicroSecondTime() + intv*end));
	double difference;
	if((long)(beginTime.getMicroSecondTime() + intv*begin) > 0){
	    difference = intv * begin;
	    beginTime = new MicroSecondDate((long)(beginTime.getMicroSecondTime() + intv*begin));
	}else{
	    difference = -beginTime.getMicroSecondTime();
	    beginTime = new MicroSecondDate(0);
	}
	displayInterval = new TimeInterval(beginTime, endTime);
	Iterator f = seismos.keySet().iterator();
	while(f.hasNext()){
	    MicroSecondTimeRange currTime = (MicroSecondTimeRange)seismos.get(f.next());
	    currTime.setBeginTime(new MicroSecondDate((long)(currTime.getBeginTime().getMicroSecondTime() + difference))); 
	    currTime.setEndTime(currTime.getBeginTime().add(displayInterval));
	}
	super.updateTimeSyncListeners();
    }
    
}// BoundedTimeConfig
