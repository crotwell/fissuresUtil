package edu.sc.seis.fissuresUtil.time;

import java.util.Arrays;
import java.util.Comparator;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;

/**
 * @author groves Created on Oct 28, 2004
 */
public class SortTool {

    public static LocalSeismogramImpl[] byLengthAscending(LocalSeismogramImpl[] seis) {
        Arrays.sort(seis, st.new SeisSizeSorter());
        return seis;
    }

    /**
     * @returns the seismograms in order of begin time
     */
    public static LocalSeismogramImpl[] byBeginTimeAscending(LocalSeismogramImpl[] seis) {
        Arrays.sort(seis, st.new SeisBeginSorter());
        return seis;
    }
    public static PlottableChunk[] byBeginTimeAscending(PlottableChunk[] pc) {
        Arrays.sort(pc, st.new PCBeginSorter());
        return pc;
    }

    public static RequestFilter[] byBeginTimeAscending(RequestFilter[] rf) {
        Arrays.sort(rf, st.new RFBeginSorter());
        return rf;
    }

    private class AscendingSizeSorter implements Comparator {

        public int compare(Object o1, Object o2) {
            TimeInterval int1 = getInterval(o1);
            TimeInterval int2 = getInterval(o2);
            if(int1.lessThan(int2)) {
                return -1;
            } else if(int1.greaterThan(int2)) { return 1; }
            return 0;
        }

        public TimeInterval getInterval(Object o) {
            return (TimeInterval)o;
        }
    }

    public class SeisSizeSorter extends AscendingSizeSorter {

        public TimeInterval getInterval(Object o) {
            return ((LocalSeismogramImpl)o).getTimeInterval();
        }
    }

    private class AscendingTimeSorter implements Comparator {

        public int compare(Object o1, Object o2) {
            MicroSecondDate o1Begin = getTime(o1);
            MicroSecondDate o2Begin = getTime(o2);
            if(o1Begin.before(o2Begin)) {
                return -1;
            } else if(o1Begin.after(o2Begin)) { return 1; }
            return 0;
        }

        public MicroSecondDate getTime(Object o) {
            return (MicroSecondDate)o;
        }
    }

    private class SeisBeginSorter extends AscendingTimeSorter {

        public MicroSecondDate getTime(Object o) {
            return ((LocalSeismogramImpl)o).getBeginTime();
        }
    }

    private class PCBeginSorter extends AscendingTimeSorter {

        public MicroSecondDate getTime(Object o) {
            return ((PlottableChunk)o).getBeginTime();
        }
    }

    private class RFBeginSorter extends AscendingTimeSorter {

        public MicroSecondDate getTime(Object o) {
            return new MicroSecondDate(((RequestFilter)o).start_time);
        }
    }

    private static final SortTool st = new SortTool();
}