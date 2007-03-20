package edu.sc.seis.fissuresUtil.time;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Oct 28, 2004
 */
public class CoverageTool {

    /**
     * @returns an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the given seismograms begin and end.
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
                                             LocalSeismogramImpl[] existingFilters) {
        if(existingFilters.length == 0) {
            return neededFilters;
        }
        LocalSeismogramImpl[] sorted = SortTool.byBeginTimeAscending(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    /**
     * @returns an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the existing filters begin and end.
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
                                             RequestFilter[] existingFilters) {
        if(existingFilters.length == 0) {
            return neededFilters;
        }
        RequestFilter[] sorted = SortTool.byBeginTimeAscending(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    public static RequestFilter[] notCovered(RequestFilter[] filters,
                                             MicroSecondTimeRange[] timeRanges) {
        List unsatisfied = new ArrayList();
        timeRanges = ReduceTool.merge(timeRanges);
        timeRanges = SortTool.byBeginTimeAscending(timeRanges);
        for(int i = 0; i < filters.length; i++) {
            MicroSecondDate rfStart = new MicroSecondDate(filters[i].start_time);
            MicroSecondDate rfEnd = new MicroSecondDate(filters[i].end_time);
            for(int j = 0; j < timeRanges.length; j++) {
                MicroSecondDate trStart = timeRanges[j].getBeginTime();
                MicroSecondDate trEnd = timeRanges[j].getEndTime();
                if(trStart.before(rfEnd)) {
                    if(trEnd.after(rfStart)) {
                        if(ReduceTool.equalsOrBefore(trStart, rfStart)) {
                            rfStart = trEnd;
                        } else {
                            unsatisfied.add(new RequestFilter(filters[i].channel_id,
                                                              rfStart.getFissuresTime(),
                                                              trStart.getFissuresTime()));
                            rfStart = trEnd;
                        }
                        if(ReduceTool.equalsOrAfter(trEnd, rfEnd)) {
                            break;
                        }
                    }
                }
            }
            if(rfEnd.after(rfStart)) {
                unsatisfied.add(new RequestFilter(filters[i].channel_id,
                                                  rfStart.getFissuresTime(),
                                                  rfEnd.getFissuresTime()));
            }
        }
        return (RequestFilter[])unsatisfied.toArray(new RequestFilter[unsatisfied.size()]);
    }

    public static RequestFilter[] notCoveredIgnoreGaps(RequestFilter[] filters,
                                                       MicroSecondTimeRange[] timeRanges) {
        timeRanges = SortTool.byBeginTimeAscending(timeRanges);
        MicroSecondTimeRange allCoverage = new MicroSecondTimeRange(timeRanges[0],
                                                                    timeRanges[timeRanges.length - 1]);
        return notCovered(filters, new MicroSecondTimeRange[] {allCoverage});
    }
}