package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
/**
 * BoundedTimeConfig is a MicroSecondTimeRangeConfig implementation that allows absolute time ranges to be set for the display.  It only displays
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
    
    /** Merely returns the time range that has been set
     */
    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis){
	return new MicroSecondTimeRange(this.beginTime, this.endTime);
    }

    public MicroSecondTimeRange getTimeRange(){
	return new MicroSecondTimeRange(this.beginTime, this.endTime);
    }

    public void addSeismogram(LocalSeismogram seis){
	if(beginTime == null)
	    this.beginTime = ((LocalSeismogramImpl)seis).getBeginTime();
	if(endTime == null)
	    this.endTime = ((LocalSeismogramImpl)seis).getEndTime();
	seismos.add(seis);
	this.updateTimeSyncListeners();
    }	
    
    /** This method allows the time range for display to be set by the user
     *
     *
     */
    public MicroSecondTimeRange setTimeRange(MicroSecondDate newBeginTime, MicroSecondDate newEndTime){
	this.beginTime = newBeginTime;
	this.endTime = newEndTime;
	return new MicroSecondTimeRange(beginTime, endTime);
    }

    /**  When BoundedTimeConfig receives a TimeSyncEvent, it merely changes the time range by the percentages contained in the 
     *   TimeSyncEvent
     */
    public void fireTimeRangeEvent(TimeSyncEvent e){
	double begin = e.getBegin();
	double end = e.getEnd();
	long intv = endTime.getMicroSecondTime() - beginTime.getMicroSecondTime();
	endTime = new MicroSecondDate((long)(endTime.getMicroSecondTime() + intv*end));
	beginTime = new MicroSecondDate((long)(beginTime.getMicroSecondTime() + intv*begin));
	this.updateTimeSyncListeners();
    }
    
}// BoundedTimeConfig
