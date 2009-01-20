package edu.sc.seis.fissuresUtil.time;

import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Oct 28, 2004
 */
public class RangeTool {

    public static boolean areContiguous(PlottableChunk one, PlottableChunk two) {
        TimeInterval sampleInterval = new TimeInterval(0, UnitImpl.DAY);
        return areContiguous(one.getTimeRange(),
                             two.getTimeRange(),
                             sampleInterval);
    }

    public static boolean areContiguous(LocalSeismogramImpl one,
                                        LocalSeismogramImpl two) {
        return areContiguous(new MicroSecondTimeRange(one),
                             new MicroSecondTimeRange(two),
                             one.getSampling().getPeriod());
    }

    public static boolean areContiguous(RequestFilter one, RequestFilter two) {
        return areContiguous(new MicroSecondTimeRange(one),
                             new MicroSecondTimeRange(two));
    }

    public static boolean areContiguous(MicroSecondTimeRange one,
                                        MicroSecondTimeRange two,
                                        TimeInterval interval) {
        if(!RangeTool.areOverlapping(one, two)) {
            TimeInterval littleMoreThanInterval = (TimeInterval)interval.add(new TimeInterval(1, UnitImpl.MICROSECOND));
            if(one.getEndTime().before(two.getBeginTime())) {
                return one.getEndTime()
                        .add(littleMoreThanInterval)
                        .after(two.getBeginTime());
            }
            return two.getEndTime()
                    .add(littleMoreThanInterval)
                    .after(one.getBeginTime());
        }
        return false;
    }

    public static boolean areContiguous(MicroSecondTimeRange one,
                                        MicroSecondTimeRange two) {
        return one.getEndTime().equals(two.getBeginTime())
                || one.getBeginTime().equals(two.getEndTime());
    }

    public static boolean areOverlapping(PlottableChunk one, PlottableChunk two) {
        return areOverlapping(one.getTimeRange(), two.getTimeRange());
    }

    public static boolean areOverlapping(MicroSecondTimeRange one,
                                         MicroSecondTimeRange two) {
        if(one.getBeginTime().before(two.getEndTime())
                && one.getEndTime().after(two.getBeginTime())) {
            return true;
        }
        return false;
    }

    public static boolean areOverlapping(LocalSeismogramImpl one,
                                         LocalSeismogramImpl two) {
        MicroSecondTimeRange oneTr = new MicroSecondTimeRange(one.getBeginTime(),
                                                              one.getEndTime());
        MicroSecondTimeRange twoTr = new MicroSecondTimeRange(two.getBeginTime(),
                                                              two.getEndTime());
        return areOverlapping(oneTr, twoTr);
    }

    /**
     * @returns A time range encompassing the earliest begin time of the passed
     *          in seismograms to the latest end time
     */
    public static MicroSecondTimeRange getFullTime(LocalSeismogramImpl[] seis) {
        if(seis.length == 0) {
            return DisplayUtils.ZERO_TIME;
        }
        MicroSecondDate beginTime = SortTool.byBeginTimeAscending(seis)[0].getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < seis.length; i++) {
            if(seis[i].getEndTime().after(endTime)) {
                endTime = seis[i].getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }


    /**
     * @returns A time range encompassing the earliest begin time of the passed
     *          in request filter to the latest end time
     */
    public static MicroSecondTimeRange getFullTime(RequestFilter[] seis) {
        if(seis.length == 0) {
            return DisplayUtils.ZERO_TIME;
        }
        MicroSecondDate beginTime = new MicroSecondDate(SortTool.byBeginTimeAscending(seis)[0].start_time);
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < seis.length; i++) {
            if(new MicroSecondDate(seis[i].end_time).after(endTime)) {
                endTime = new MicroSecondDate(seis[i].end_time);
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }
    
    
    public static MicroSecondTimeRange getFullTime(List<PlottableChunk> pc) {
        if(pc.size() == 0) {
            return DisplayUtils.ZERO_TIME;
        }
        MicroSecondDate beginTime = SortTool.byBeginTimeAscending(pc).get(0).getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for (PlottableChunk plottableChunk : pc) {
            if(plottableChunk.getEndTime().after(endTime)) {
                endTime = plottableChunk.getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }
}