package edu.sc.seis.fissuresUtil.time;

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
        TimeInterval sampleInterval = new TimeInterval(1d / one.getPixelsPerDay(),
                                                        UnitImpl.DAY);
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

    public static boolean areContiguous(MicroSecondTimeRange one,
                                        MicroSecondTimeRange two,
                                        TimeInterval interval) {
        if(!RangeTool.areOverlapping(one, two)) {
            TimeInterval doubleInterval = (TimeInterval)interval.multiplyBy(2.0);
            if(one.getEndTime().before(two.getBeginTime())) {
                return one.getEndTime()
                        .add(doubleInterval)
                        .after(two.getBeginTime());
            } else {
                return two.getEndTime()
                        .add(doubleInterval)
                        .after(one.getBeginTime());
            }
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
        if(one.getBeginTime().before(two.getEndTime()) && one.getEndTime()
                .after(two.getBeginTime())) { return true; }
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
        if(seis.length == 0) { return DisplayUtils.ZERO_TIME; }
        MicroSecondDate beginTime = SortTool.byBeginTimeAscending(seis)[0].getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < seis.length; i++) {
            if(seis[i].getEndTime().after(endTime)) {
                endTime = seis[i].getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }

    public static MicroSecondTimeRange getFullTime(PlottableChunk[] pc) {
        if(pc.length == 0) { return DisplayUtils.ZERO_TIME; }
        MicroSecondDate beginTime = SortTool.byBeginTimeAscending(pc)[0].getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < pc.length; i++) {
            if(pc[i].getEndTime().after(endTime)) {
                endTime = pc[i].getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }
}