package edu.sc.seis.fissuresUtil.time;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.plottable.JDBCPlottable;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Oct 29, 2004
 */
public class ReduceTool {

    public static LocalSeismogramImpl[] removeContained(LocalSeismogramImpl[] seis) {
        SortTool.byLengthAscending(seis);
        List results = new ArrayList();
        for(int i = 0; i < seis.length; i++) {
            MicroSecondDate iEnd = seis[i].getEndTime();
            MicroSecondDate iBegin = seis[i].getBeginTime();
            boolean contained = false;
            for(int j = i + 1; j < seis.length && !contained; j++) {
                if(equalsOrAfter(iBegin, seis[j].getBeginTime())
                        && equalsOrBefore(iEnd, seis[j].getEndTime())) {
                    contained = true;
                }
            }
            if(!contained) {
                results.add(seis[i]);
            }
        }
        return (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
    }

    /**
     * Unites all ranges in the given array into a single range if they're
     * contiguous or overlapping
     */
    public static MicroSecondTimeRange[] merge(MicroSecondTimeRange[] ranges) {
        return rt.new MSTRMerger().merge(ranges);
    }

    /**
     * Unites all chunks in the given array into a single chunk if they're
     * contiguous or overlapping in time. Ignores the channels and samples per
     * second inside of the chunks, so they must be grouped according to that
     * before being merged
     */
    public static PlottableChunk[] merge(PlottableChunk[] chunks) {
        return rt.new PlottableChunkMerger().merge(chunks);
    }

    private abstract class Merger {

        public abstract Object merge(Object one, Object two);

        public abstract boolean areContiguous(Object one, Object two);

        public abstract boolean areOverlapping(Object one, Object two);

        public Object[] internalMerge(Object[] chunks,
                                      Object[] resultantTypeArray) {
            chunks = (Object[])chunks.clone();
            for(int i = 0; i < chunks.length; i++) {
                Object chunk = chunks[i];
                for(int j = i + 1; j < chunks.length; j++) {
                    Object chunk2 = chunks[j];
                    if(areContiguous(chunk, chunk2)
                            || areOverlapping(chunk, chunk2)) {
                        chunks[j] = merge(chunk, chunk2);
                        chunks[i] = null;
                        break;
                    }
                }
            }
            List results = new ArrayList();
            for(int i = 0; i < chunks.length; i++) {
                if(chunks[i] != null) {
                    results.add(chunks[i]);
                }
            }
            return results.toArray(resultantTypeArray);
        }
    }

    private class MSTRMerger extends Merger {

        public Object merge(Object one, Object two) {
            return new MicroSecondTimeRange(cast(one), cast(two));
        }

        public boolean areContiguous(Object one, Object two) {
            return RangeTool.areContiguous(cast(one), cast(two));
        }

        public boolean areOverlapping(Object one, Object two) {
            return RangeTool.areOverlapping(cast(one), cast(two));
        }

        public MicroSecondTimeRange cast(Object o) {
            return (MicroSecondTimeRange)o;
        }

        public MicroSecondTimeRange[] merge(MicroSecondTimeRange[] ranges) {
            return (MicroSecondTimeRange[])internalMerge(ranges,
                                                         new MicroSecondTimeRange[0]);
        }
    }

    private class PlottableChunkMerger extends Merger {

        public Object merge(Object one, Object two) {
            PlottableChunk chunk = cast(one);
            PlottableChunk chunk2 = cast(two);
            MicroSecondTimeRange fullRange = new MicroSecondTimeRange(chunk.getTimeRange(),
                                                                      chunk2.getTimeRange());
            logger.debug("Merging " + chunk + " and " + chunk2 + " into "
                    + fullRange);
            int samples = (int)Math.floor(chunk.getPixelsPerDay() * 2
                    * fullRange.getInterval().convertTo(UnitImpl.DAY).value);
            int[] y = new int[samples];
            JDBCPlottable.fill(fullRange, y, chunk);
            JDBCPlottable.fill(fullRange, y, chunk2);
            Plottable mergedData = new Plottable(null, y);
            PlottableChunk earlier = chunk;
            if(chunk2.getBeginTime().before(chunk.getBeginTime())) {
                earlier = chunk2;
            }
            return new PlottableChunk(mergedData,
                                      earlier.getBeginPixel(),
                                      earlier.getJDay(),
                                      earlier.getYear(),
                                      chunk.getPixelsPerDay(),
                                      chunk.getChannel());
        }

        public boolean areContiguous(Object one, Object two) {
            return RangeTool.areContiguous(cast(one), cast(two));
        }

        public boolean areOverlapping(Object one, Object two) {
            return RangeTool.areOverlapping(cast(one), cast(two));
        }

        public PlottableChunk cast(Object o) {
            return (PlottableChunk)o;
        }

        public PlottableChunk[] merge(PlottableChunk[] chunks) {
            return (PlottableChunk[])internalMerge(chunks,
                                                   new PlottableChunk[0]);
        }
    }

    public static boolean equalsOrAfter(MicroSecondDate first,
                                        MicroSecondDate second) {
        return first.equals(second) || first.after(second);
    }

    public static boolean equalsOrBefore(MicroSecondDate first,
                                         MicroSecondDate second) {
        return first.equals(second) || first.before(second);
    }

    private static final ReduceTool rt = new ReduceTool();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ReduceTool.class);
}