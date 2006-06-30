package edu.sc.seis.fissuresUtil.time;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.fissuresUtil.database.plottable.JDBCPlottable;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.SortTool.SeisSizeSorter;

/**
 * @author groves Created on Oct 29, 2004
 */
public class ReduceTool {
    
    public static LocalSeismogramImpl[] cutOverlap(LocalSeismogramImpl[] seis)
            throws FissuresException {
        // first get rid of totally contained overlaps
        seis = removeContained(seis);
        SortTool.byLengthAscending(seis);
        SeisSizeSorter sorter = new SeisSizeSorter();
        List results = new ArrayList();
        for(int i = seis.length-1; i >= 0; i--) {
            if (seis[i] == null) {continue;}
            results.add(seis[i]);
            for(int j = i - 1; j >= 0; j--) {
                if (seis[j] == null) {continue;}
                if(RangeTool.areOverlapping(seis[i], seis[j])) {
                    MicroSecondDate iEnd = seis[i].getEndTime();
                    MicroSecondDate iBegin = seis[i].getBeginTime();
                    TimeInterval halfSample = (TimeInterval)seis[i].getSampling().getPeriod().divideBy(2);
                    if (iEnd.before(seis[j].getEndTime())) {
                        // overlap on i's end
                        Cut cut = new Cut(iEnd.add(halfSample), seis[j].getEndTime().add(halfSample));
                        seis[j] = cut.apply(seis[j]);   
                    } else {
                        Cut cut = new Cut(seis[j].getBeginTime().subtract(halfSample), iBegin.subtract(halfSample));
                        seis[j] = cut.apply(seis[j]);
                    }
                    for(int k=j-1; k>=0; k++) {
                        if (seis[k] != null && sorter.compare(seis[k], seis[k+1]) == 1) {
                            // cut has made order no longer ascending
                            LocalSeismogramImpl tmp = seis[k];
                            seis[k] = seis[k+1];
                            seis[k+1] = tmp;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        return (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
    }

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
     * Unites contiguous and equal seismograms into a single
     * LocalSeismogramImpl. Partially overlapping seismograms are left separate.
     */
    public static LocalSeismogramImpl[] merge(LocalSeismogramImpl[] seis) {
        return new LSMerger().merge(seis);
    }

    /**
     * Unites all RequestFilters for the same channel in the given array into a
     * single requestfilter if they're contiguous or overlapping in time.
     */
    public static RequestFilter[] merge(RequestFilter[] ranges) {
        return new RFMerger().merge(ranges);
    }

    /**
     * Unites all ranges in the given array into a single range if they're
     * contiguous or overlapping
     */
    public static MicroSecondTimeRange[] merge(MicroSecondTimeRange[] ranges) {
        return new MSTRMerger().merge(ranges);
    }

    /**
     * Unites all chunks in the given array into a single chunk if they're
     * contiguous or overlapping in time. Ignores the channels and samples per
     * second inside of the chunks, so they must be grouped according to that
     * before being merged
     */
    public static PlottableChunk[] merge(PlottableChunk[] chunks) {
        return new PlottableChunkMerger().merge(chunks);
    }

    private static abstract class Merger {

        public abstract Object merge(Object one, Object two);

        public abstract boolean shouldMerge(Object one, Object two);

        public Object[] internalMerge(Object[] chunks,
                                      Object[] resultantTypeArray) {
            chunks = (Object[])chunks.clone();
            for(int i = 0; i < chunks.length; i++) {
                Object chunk = chunks[i];
                for(int j = i + 1; j < chunks.length; j++) {
                    Object chunk2 = chunks[j];
                    if(shouldMerge(chunk, chunk2)) {
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

    private static class MSTRMerger extends Merger {

        public Object merge(Object one, Object two) {
            return new MicroSecondTimeRange(cast(one), cast(two));
        }

        public boolean shouldMerge(Object one, Object two) {
            MicroSecondTimeRange o = (MicroSecondTimeRange)one;
            MicroSecondTimeRange t = (MicroSecondTimeRange)two;
            if(o.getBeginTime().before(t.getBeginTime())){
                return !o.getEndTime().before(t.getBeginTime());
            }
            return !t.getEndTime().before(o.getBeginTime());
        }

        public MicroSecondTimeRange cast(Object o) {
            return (MicroSecondTimeRange)o;
        }

        public MicroSecondTimeRange[] merge(MicroSecondTimeRange[] ranges) {
            return (MicroSecondTimeRange[])internalMerge(ranges,
                                                         new MicroSecondTimeRange[0]);
        }
    }

    private static class RFMerger extends Merger {

        public Object merge(Object one, Object two) {
            RequestFilter orig = (RequestFilter)one;
            MicroSecondTimeRange tr = new MicroSecondTimeRange(toMSTR(one),
                                                               toMSTR(two));
            return new RequestFilter(orig.channel_id, tr.getBeginTime()
                    .getFissuresTime(), tr.getEndTime().getFissuresTime());
        }

        protected String getChannelString(Object rf) {
            return ChannelIdUtil.toStringNoDates(((RequestFilter)rf).channel_id);
        }

        public boolean shouldMerge(Object one, Object two) {
            return getChannelString(one).equals(getChannelString(two))
                    && (RangeTool.areOverlapping(toMSTR(one), toMSTR(two)) || RangeTool.areContiguous(toMSTR(one),
                                                                                                      toMSTR(two)));
        }

        protected MicroSecondTimeRange toMSTR(Object o) {
            return new MicroSecondTimeRange((RequestFilter)o);
        }

        public RequestFilter[] merge(RequestFilter[] ranges) {
            return (RequestFilter[])internalMerge(ranges, new RequestFilter[0]);
        }
    }

    private static class LSMerger extends Merger {

        public Object merge(Object one, Object two) {
            LocalSeismogramImpl seis = (LocalSeismogramImpl)one;
            LocalSeismogramImpl seis2 = (LocalSeismogramImpl)two;
            MicroSecondTimeRange fullRange = new MicroSecondTimeRange(toMSTR(seis),
                                                                      toMSTR(seis2));
            if(fullRange.equals(toMSTR(seis))) {
                return seis;
            }
            LocalSeismogramImpl earlier = seis;
            LocalSeismogramImpl later = seis2;
            if(seis2.getBeginTime().before(seis.getBeginTime())) {
                earlier = seis2;
                later = seis;
            }
            try {
                if(seis.is_encoded() && seis2.is_encoded()) {
                    EncodedData[] earlierED = earlier.get_as_encoded();
                    EncodedData[] laterED = later.get_as_encoded();
                    EncodedData[] outED = new EncodedData[earlierED.length
                            + laterED.length];
                    System.arraycopy(earlierED, 0, outED, 0, earlierED.length);
                    System.arraycopy(laterED,
                                     0,
                                     outED,
                                     earlierED.length,
                                     laterED.length);
                    TimeSeriesDataSel td = new TimeSeriesDataSel();
                    td.encoded_values(outED);
                    LocalSeismogramImpl newSeis =  new LocalSeismogramImpl(earlier, td);
                    newSeis.num_points = seis.num_points + seis2.num_points;
                    return newSeis;
                }
                int numPoints = seis.getNumPoints() + seis2.getNumPoints();
                if(seis.can_convert_to_short() && seis2.can_convert_to_short()) {
                    short[] outS = new short[numPoints];
                    System.arraycopy(earlier.get_as_shorts(),
                                     0,
                                     outS,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_shorts(),
                                     0,
                                     outS,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outS);
                } else if(seis.can_convert_to_long() && seis2.can_convert_to_long()) {
                    int[] outI = new int[numPoints];
                    System.arraycopy(earlier.get_as_longs(),
                                     0,
                                     outI,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_longs(),
                                     0,
                                     outI,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outI);
                } else if(seis.can_convert_to_float() && seis2.can_convert_to_float()) {
                    float[] outF = new float[numPoints];
                    System.arraycopy(earlier.get_as_floats(),
                                     0,
                                     outF,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_floats(),
                                     0,
                                     outF,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outF);
                } else {
                    double[] outD = new double[numPoints];
                    System.arraycopy(earlier.get_as_doubles(),
                                     0,
                                     outD,
                                     0,
                                     earlier.getNumPoints());
                    System.arraycopy(later.get_as_doubles(),
                                     0,
                                     outD,
                                     earlier.getNumPoints(),
                                     later.getNumPoints());
                    return new LocalSeismogramImpl(earlier, outD);
                }
            } catch(FissuresException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean shouldMerge(Object one, Object two) {
            return getChannelString(one).equals(getChannelString(two))
                    && (RangeTool.areContiguous((LocalSeismogramImpl)one,
                                                (LocalSeismogramImpl)two) || toMSTR(one).equals(toMSTR(two)));
        }

        protected String getChannelString(Object rf) {
            return ChannelIdUtil.toStringNoDates(((LocalSeismogramImpl)rf).channel_id);
        }

        protected MicroSecondTimeRange toMSTR(Object o) {
            return new MicroSecondTimeRange((LocalSeismogramImpl)o);
        }

        public LocalSeismogramImpl[] merge(LocalSeismogramImpl[] ranges) {
            return (LocalSeismogramImpl[])internalMerge(ranges,
                                                        new LocalSeismogramImpl[0]);
        }
    }

    private static class PlottableChunkMerger extends Merger {

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

        public boolean shouldMerge(Object one, Object two) {
            return RangeTool.areContiguous(cast(one), cast(two))
                    || RangeTool.areOverlapping(cast(one), cast(two));
        }

        private PlottableChunk cast(Object o) {
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ReduceTool.class);
}