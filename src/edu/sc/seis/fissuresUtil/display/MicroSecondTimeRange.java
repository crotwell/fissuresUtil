package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import org.apache.log4j.Category;
/**
 * MicroSecondTimeRanges are objects to set the time range for a seismogram and assorted widgets that go with them.  It implements TimeSyncListener
 * so that it may be notified of time sync events.
 *
 * Created: Wed May 22 16:19:18 2002
 *
 * @author Charlie Groves
 * @version
 */

public class MicroSecondTimeRange extends UnitRangeImpl{

    public MicroSecondTimeRange(RequestFilter rf){
        this(new MicroSecondDate(rf.start_time),
             new MicroSecondDate(rf.end_time));
    }

    public MicroSecondTimeRange(TimeRange timeRange){
        this(new MicroSecondDate(timeRange.start_time),
             new MicroSecondDate(timeRange.end_time));
    }

    /**
     * Creates a new MicroSecondTimeRange.  The order of the times passed in
     * doesn't matter
     *
     */
    public MicroSecondTimeRange(MicroSecondDate time, MicroSecondDate anotherTime){
        super(time.getTime(), anotherTime.getTime(), UnitImpl.MILLISECOND);
        if(time.before(anotherTime)){
            this.beginTime = time;
            this.endTime = anotherTime;
        }else{
            this.beginTime = anotherTime;
            this.endTime = time;
        }
        this.interval = new TimeInterval(beginTime, endTime);
    }

    public MicroSecondTimeRange(MicroSecondDate beginTime, TimeInterval interval){
        this(beginTime, beginTime.add(interval));
    }

    public boolean intersects(MicroSecondDate newTime) {
        if((beginTime.before(newTime) || beginTime.equals(newTime) ) &&
               (endTime.after(newTime) || endTime.equals(newTime)))
            return true;
        return false;
    }

    public boolean intersects(MicroSecondTimeRange time) {
        if(endTime.after(time.getBeginTime()) && beginTime.before(time.getEndTime()))
            return true;
        return false;
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

    public boolean equals(Object other){
        if(this == other)return true;
        if(getClass() != other.getClass()) return false;
        MicroSecondTimeRange mstrTime = (MicroSecondTimeRange)other;
        if(beginTime.equals(mstrTime.getBeginTime()) &&
           endTime.equals(mstrTime.getEndTime())){
            return true;
        }
        return false;
    }

    public int hashCode(){
        int result = 17;
        result = 37*result + beginTime.hashCode();
        result = 37*result + endTime.hashCode();
        return result;
    }

    public String toString(){ return beginTime + " to " + endTime; }

    private final MicroSecondDate beginTime;

    private final MicroSecondDate endTime;

    private final TimeInterval interval;

    private static Category logger = Category.getInstance(MicroSecondTimeRange.class.getName());
}// MicroSecondTimeRange
