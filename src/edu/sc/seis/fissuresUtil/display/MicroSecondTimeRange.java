package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
/**
 * MicroSecondTimeRanges are objects to set the time range for a seismogram and assorted widgets that go with them.  It implements TimeSyncListener
 * so that it may be notified of time sync events.
 *
 * Created: Wed May 22 16:19:18 2002
 *
 * @author Charlie Groves
 * @version
 */

public class MicroSecondTimeRange{
    
    /**
     * Creates a new MicroSecondTimeRange
     *
     * @param beginTime the beginning time
     * @param endTime the ending time
     */
    public MicroSecondTimeRange(MicroSecondDate beginTime, MicroSecondDate endTime){
	this.beginTime = beginTime;
	this.endTime = endTime;
     }
    
    /**
     * Changes this time range
     *
     */
    public void setTimeRange(MicroSecondDate newBeginTime, MicroSecondDate newEndTime){
	this.beginTime = newBeginTime;
	this.endTime = newEndTime;
    }
    
    /**
     * Returns the beginning time for this range
     *
     * 
     */
    public MicroSecondDate getBeginTime(){ return beginTime;}
    
    /**
     * Sets the beginning time for this range
     *
     * 
     */
    public void setBeginTime(MicroSecondDate newBeginTime ){ this.beginTime = newBeginTime; }
    
    /**
     * Returns the ending time for this range
     *
     * 
     */
    public MicroSecondDate getEndTime(){ return endTime; }
    
    /**
     * Sets the ending time for this range
     *
     * 
     */
    public void setEndTime(MicroSecondDate newEndTime){ this.endTime = newEndTime; }
    
    /**
     * Returns the interval that this range comprises
     *
     * 
     */
    public TimeInterval getInterval(){ return new TimeInterval(beginTime, endTime); }

    public MicroSecondTimeRange getOversizedTimeRange(int scale){
	long extraTime = scale*(endTime.getMicroSecondTime() - beginTime.getMicroSecondTime());
	return new MicroSecondTimeRange(new MicroSecondDate(beginTime.getMicroSecondTime() - extraTime),
			     new MicroSecondDate(endTime.getMicroSecondTime() + extraTime));
    }

    private MicroSecondDate beginTime;
    
    private MicroSecondDate endTime;

}// MicroSecondTimeRange
