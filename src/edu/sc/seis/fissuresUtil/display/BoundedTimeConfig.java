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
	return new MicroSecondTimeRange(((MicroSecondDate)seismos.get(seis)), ((MicroSecondDate)seismos.get(seis)).add(displayInterval));
    }

    public synchronized MicroSecondTimeRange getTimeRange(){
	return new MicroSecondTimeRange(this.beginTime, this.beginTime.add(displayInterval));
    }

    public synchronized void addSeismogram(DataSetSeismogram seis){
	if(beginTime == null){
	    this.beginTime = ((LocalSeismogramImpl)seis.getSeismogram()).getBeginTime();
	    seismos.put(seis, ((LocalSeismogramImpl)seis.getSeismogram()).getBeginTime());
	}else
	    seismos.put(seis, this.beginTime);
	if(displayInterval == null)
	    this.displayInterval = new TimeInterval(((LocalSeismogramImpl)seis.getSeismogram()).getBeginTime(), 
						    ((LocalSeismogramImpl)seis.getSeismogram()).getEndTime());
	
	super.updateTimeSyncListeners();
    }	

    public synchronized void addSeismogram(DataSetSeismogram seis, MicroSecondDate time){
	if(beginTime == null)
	    this.beginTime = time;
	if(displayInterval == null)
	    this.displayInterval = new TimeInterval(time, ((LocalSeismogramImpl)seis.getSeismogram()).getEndTime());
	seismos.put(seis, time);
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
	Iterator f = seismos.keySet().iterator();
	while(f.hasNext()){
	    DataSetSeismogram current = ((DataSetSeismogram)f.next());
	    seismos.put(current, new MicroSecondDate((long)(((MicroSecondDate)seismos.get(current)).getMicroSecondTime() + difference)));
	}
	displayInterval = new TimeInterval(beginTime, endTime);
	super.updateTimeSyncListeners();
    }
    
}// BoundedTimeConfig
