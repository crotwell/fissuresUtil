package edu.sc.seis.fissuresUtil.time;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * MicroSecondTimeRanges are objects to set the time range for a seismogram and
 * assorted widgets that go with them. It implements TimeSyncListener so that it
 * may be notified of time sync events. Created: Wed May 22 16:19:18 2002
 * 
 * @author Charlie Groves
 * @version
 */
public class MicroSecondTimeRange {

    public MicroSecondTimeRange(LocalSeismogramImpl ls) {
        this(ls.getBeginTime(), ls.getEndTime());
    }

    public MicroSecondTimeRange(RequestFilter rf) {
        this(rf.start_time, rf.end_time);
    }

    public MicroSecondTimeRange(TimeRange timeRange) {
        this(timeRange.start_time, timeRange.end_time);
    }

    public MicroSecondTimeRange(Time time, Time anotherTime) {
        this(new MicroSecondDate(time), new MicroSecondDate(anotherTime));
    }

    /**
     * Creates a new MicroSecondTimeRange. The order of the times passed in
     * doesn't matter
     */
    public MicroSecondTimeRange(MicroSecondDate time,
                                MicroSecondDate anotherTime) {
        if(time.before(anotherTime)) {
            this.beginTime = time;
            this.endTime = anotherTime;
        } else {
            this.beginTime = anotherTime;
            this.endTime = time;
        }
        this.interval = new TimeInterval(beginTime, endTime);
    }

    public MicroSecondTimeRange(MicroSecondDate beginTime, TimeInterval interval) {
        this(beginTime, beginTime.add(interval));
    }

    public MicroSecondTimeRange(MicroSecondTimeRange timeRange,
                                MicroSecondTimeRange timeRange2) {
        this(timeRange.getBeginTime().before(timeRange2.getBeginTime()) ? timeRange.getBeginTime()
                     : timeRange2.getBeginTime(),
             timeRange.getEndTime().after(timeRange2.getEndTime()) ? timeRange.getEndTime()
                     : timeRange2.getEndTime());
    }

    /**
     *@deprecated - just calls contains(MicroSecondDate)
     */
    public boolean intersects(MicroSecondDate newTime) {
        return contains(newTime);
    }


    public boolean contains(MicroSecondDate newTime) {
        return (beginTime.before(newTime) || beginTime.equals(newTime))
                && (endTime.after(newTime) || endTime.equals(newTime));
    }

    public boolean intersects(MicroSecondTimeRange time) {
        return endTime.after(time.getBeginTime())
                && beginTime.before(time.getEndTime());
    }
    
    public MicroSecondTimeRange intersection(MicroSecondTimeRange time) {
        if (intersects(time)) {
            return new MicroSecondTimeRange(beginTime.after(time.getBeginTime())?beginTime:time.getBeginTime(),
                    endTime.before(time.getEndTime())?endTime:time.getEndTime());
        }
        return null;
    }

    public MicroSecondTimeRange shale(double shift, double scale) {
        if(shift == 0 && scale == 1) {
            return this;
        }
        TimeInterval timeShift = (TimeInterval)interval.multiplyBy(Math.abs(shift));
        MicroSecondDate newBeginTime;
        if(shift < 0) {
            newBeginTime = beginTime.subtract(timeShift);
        } else {
            newBeginTime = beginTime.add(timeShift);
        }
        return new MicroSecondTimeRange(newBeginTime,
                                        (TimeInterval)interval.multiplyBy(scale));
    }

    public MicroSecondTimeRange shift(TimeInterval shift) {
        return new MicroSecondTimeRange(beginTime.add(shift),
                                        endTime.add(shift));
    }

    public MicroSecondTimeRange shift(double percentage) {
        if(percentage == 0) {
            return this;
        }
        TimeInterval shift = (TimeInterval)interval.multiplyBy(Math.abs(percentage));
        if(percentage < 0) {
            return new MicroSecondTimeRange(beginTime.subtract(shift),
                                            endTime.subtract(shift));
        }
        return new MicroSecondTimeRange(beginTime.add(shift),
                                        endTime.add(shift));
    }

    /**
     * Returns the beginning time for this range
     */
    public MicroSecondDate getBeginTime() {
        return beginTime;
    }

    /**
     * Returns the ending time for this range
     */
    public MicroSecondDate getEndTime() {
        return endTime;
    }

    /**
     * Returns the interval that this range comprises
     */
    public TimeInterval getInterval() {
        return interval;
    }

    public TimeRange getFissuresTimeRange() {
        return new TimeRange(getBeginTime().getFissuresTime(),
                             getEndTime().getFissuresTime());
    }

    public UnitRangeImpl getMillis() {
        return new UnitRangeImpl(beginTime.getTime(),
                                 endTime.getTime(),
                                 UnitImpl.MILLISECOND);
    }

    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(getClass() != other.getClass())
            return false;
        MicroSecondTimeRange mstrTime = (MicroSecondTimeRange)other;
        if(beginTime.equals(mstrTime.getBeginTime())
                && endTime.equals(mstrTime.getEndTime())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + beginTime.hashCode();
        result = 37 * result + endTime.hashCode();
        return result;
    }

    public String toString() {
        return beginTime + " to " + endTime;
    }

    private final MicroSecondDate beginTime;

    private final MicroSecondDate endTime;

    private final TimeInterval interval;
}// MicroSecondTimeRange
