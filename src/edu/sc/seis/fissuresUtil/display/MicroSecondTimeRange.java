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
	this.interval = new TimeInterval(beginTime, endTime);
     }

    public MicroSecondTimeRange(MicroSecondDate beginTime, TimeInterval interval){
	this.beginTime = beginTime;
	this.endTime = beginTime.add(interval);
	this.interval = interval;
    }
    
    public MicroSecondTimeRange shale(double shift, double scale){
	if(shift == 0 && scale == 1){
	    return this;
	}
	TimeInterval timeShift = (TimeInterval)interval.multiplyBy(Math.abs(shift));
	MicroSecondDate newBeginTime;
	if(shift < 0){
	    newBeginTime = beginTime.subtract(timeShift);
	}else{
	    newBeginTime = beginTime.add(timeShift);
	}
	return new MicroSecondTimeRange(newBeginTime, (TimeInterval)interval.multiplyBy(scale));
    }
    
    public MicroSecondTimeRange shift(TimeInterval shift){
	return new MicroSecondTimeRange(beginTime.add(shift),
					endTime.add(shift));
    }

    public MicroSecondTimeRange shift(double percentage){
	if(percentage == 0){
	    return this;
	}
	TimeInterval shift = (TimeInterval)interval.multiplyBy(Math.abs(percentage));
	if(percentage < 0){
	    return new MicroSecondTimeRange(beginTime.subtract(shift), endTime.subtract(shift));
	}else{
	    return new MicroSecondTimeRange(beginTime.add(shift), endTime.add(shift));
	}
    }

    /**
     * Returns the beginning time for this range
     *
     * 
     */
    public MicroSecondDate getBeginTime(){ return beginTime;}
        
    /**
     * Returns the ending time for this range
     *
     * 
     */
    public MicroSecondDate getEndTime(){ return endTime; }
    
    /**
     * Returns the interval that this range comprises
     *
     * 
     */
    public TimeInterval getInterval(){ return interval; }

    public boolean equals(MicroSecondTimeRange otherTime){
	if(this == otherTime){
	    return true;
	}
	return false;
    }

    public String toString(){ return beginTime + " " + endTime; }

    private final MicroSecondDate beginTime;
    
    private final MicroSecondDate endTime;

    private final TimeInterval interval;

}// MicroSecondTimeRange
