package edu.sc.seis.fissuresUtil.time;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Oct 28, 2004
 */
public class RangeTool {

    public static boolean areContiguous(LocalSeismogramImpl one,
                                        LocalSeismogramImpl two) {
        if(!RangeTool.areOverlapping(one, two)) {
            if(one.getEndTime().before(two.getBeginTime())) {
                return RangeTool.areContiguous(one.getEndTime(),
                                                two.getBeginTime(),
                                                one.getSampling().getPeriod());
            } else {
                return RangeTool.areContiguous(two.getEndTime(),
                                                one.getBeginTime(),
                                                one.getSampling().getPeriod());
            }
        }
        return false;
    }

    public static boolean areOverlapping(MicroSecondTimeRange one,
                                         MicroSecondTimeRange two) {
        if((one.getBeginTime().before(two.getEndTime()) && one.getEndTime()
                .after(two.getBeginTime()))
                || (two.getBeginTime().before(one.getEndTime()) && two.getEndTime()
                        .after(one.getBeginTime()))) { return true; }
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

    public static boolean areContiguous(MicroSecondDate one,
                                        MicroSecondDate two,
                                        TimeInterval interval) {
        TimeInterval doubleInterval = (TimeInterval)interval.multiplyBy(2.0);
        if(one.add(doubleInterval).after(two)) { return true; }
        return false;
    }

    /**
     * @returns A time range encompassing the earliest begin time of the passed
     *          in seismograms to the latest end time
     */
    public static MicroSecondTimeRange getFullTime(LocalSeismogramImpl[] seis) {
        if(seis.length == 0) { return DisplayUtils.ZERO_TIME; }
        MicroSecondDate beginTime = SortTool.sortByDate(seis)[0].getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < seis.length; i++) {
            if(seis[i].getEndTime().after(endTime)) {
                endTime = seis[i].getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }
}