package edu.sc.seis.fissuresUtil.time;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


/**
 * @author groves
 * Created on Oct 28, 2004
 */
public class CoverageTool {

    /**
     * @returns an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the given seismograms begin and end.
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
                                             LocalSeismogramImpl[] existingFilters) {
        if(existingFilters.length == 0) { return neededFilters; }
        LocalSeismogramImpl[] sorted = SortTool.sortByDate(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    public static RequestFilter[] notCovered(RequestFilter[] existingFilters,
                                             RequestFilter[] neededFilters) {
        if(existingFilters.length == 0) { return neededFilters; }
        RequestFilter[] sorted = SortTool.sortByDate(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for(int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i]);
        }
        return CoverageTool.notCovered(neededFilters, ranges);
    }

    public static RequestFilter[] notCovered(RequestFilter[] filters,
                                             MicroSecondTimeRange[] timeRanges) {
        List unsatisfied = new ArrayList();
        for(int i = 0; i < filters.length; i++) {
            boolean beginCovered = false;
            boolean endCovered = false;
            MicroSecondDate filterBegin = new MicroSecondDate(filters[i].start_time);
            MicroSecondDate filterEnd = new MicroSecondDate(filters[i].end_time);
            for(int j = 0; j < timeRanges.length
                    && !(beginCovered && endCovered); j++) {
                MicroSecondDate timeBegin = timeRanges[j].getBeginTime();
                MicroSecondDate timeEnd = timeRanges[j].getEndTime();
                if((filterBegin.after(timeBegin) && filterBegin.before(timeEnd))
                        || filterBegin.equals(timeBegin)) {
                    beginCovered = true;
                }
                if((filterEnd.before(timeEnd) && timeBegin.before(filterEnd))
                        || filterEnd.equals(timeEnd)) {
                    endCovered = true;
                }
            }
            if(!beginCovered && !endCovered) {
                unsatisfied.add(filters[i]);
            } else if(!beginCovered) {
                unsatisfied.add(new RequestFilter(filters[i].channel_id,
                                                  filters[i].start_time,
                                                  timeRanges[0].getBeginTime()
                                                          .getFissuresTime()));
            } else if(!endCovered) {
                unsatisfied.add(new RequestFilter(filters[i].channel_id,
                                                  timeRanges[timeRanges.length - 1].getEndTime()
                                                          .getFissuresTime(),
                                                  filters[i].end_time));
            }
        }
        return (RequestFilter[])unsatisfied.toArray(new RequestFilter[unsatisfied.size()]);
    }}
