package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;
import java.util.*;

/**
 * RelativeTimeConfig configures a set of seismograms over a user defined time interval.  Each seismogram has a reference time which 
 * states when in its own time range it should begin display, and it uses the interval defined for the config to determine how long it 
 * should go from that beginning.  If no time reference is specified for a seismogram to begin display, it uses its own beginning time 
 * as a default.
 *
 *
 * Created: Mon May 27 12:59:56 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class RelativeTimeConfig extends AbstractTimeRangeConfig{
    
    public RelativeTimeConfig(){
	beginTime = new MicroSecondDate(0);
	endTime = new MicroSecondDate(3000000);
    }
    
    /** Returns a time range that starts at the reference time for this seismogram and ends at the reference time plus the time interval
     */
    public MicroSecondTimeRange getTimeRange(LocalSeismogram seis){ 
	MicroSecondDate curr = (MicroSecondDate)(seismos.get(seis));
	return new MicroSecondTimeRange(new MicroSecondDate(curr.getMicroSecondTime() + beginTime.getMicroSecondTime()),
			     new MicroSecondDate(curr.getMicroSecondTime() + endTime.getMicroSecondTime()));
    }

    /**Returns the time interval
     */
    public MicroSecondTimeRange getTimeRange(){ return new MicroSecondTimeRange(beginTime, endTime); }

    /** Adds a seismogram that has a reference set by the user
     */
    public void addSeismogram(LocalSeismogram seis, MicroSecondDate time){ 
	seismos.put(seis, time);
	this.updateTimeSyncListeners();
    }

    /**Adds a seismogram with its reference set to its begin time
     */
    public void addSeismogram(LocalSeismogram seis){ 
	seismos.put(seis, ((LocalSeismogramImpl)seis).getBeginTime()); 
	this.updateTimeSyncListeners();
    }
    
    /** On a time sync event, the time interval is zoomed by the percentages in the event
     */
    public void fireTimeRangeEvent(TimeSyncEvent e){
	double begin = e.getBegin();
	double end = e.getEnd();
	long intv = endTime.getMicroSecondTime() - beginTime.getMicroSecondTime();
	endTime = new MicroSecondDate((long)(endTime.getMicroSecondTime() + intv*end));
	if(beginTime.getMicroSecondTime() + intv*begin <= 0)
	    beginTime = new MicroSecondDate(0);
	else
	    beginTime = new MicroSecondDate((long)(beginTime.getMicroSecondTime() + intv*begin));
	this.updateTimeSyncListeners();
    }
    
    /** Sets the references for a particular seismogram
     */
    public void setReference(LocalSeismogram seis, MicroSecondDate ref){
	seismos.put(seis, ref);
	this.updateTimeSyncListeners();
    }
    
    public MicroSecondDate getReference(LocalSeismogram seis){
	return (MicroSecondDate)seismos.get(seis);
    }

    /** Sets the display interval for this config to be from the 0 time to the time in length
     */
    public void setDisplayInterval(MicroSecondDate length){ 
	beginTime = new MicroSecondDate(0);
	endTime=  length;
	this.updateTimeSyncListeners();
    }

    protected HashMap seismos = new HashMap();
    
}// RelativeTimeConfig
