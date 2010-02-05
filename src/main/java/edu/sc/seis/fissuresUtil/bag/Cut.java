package edu.sc.seis.fissuresUtil.bag;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Cuts seismograms based on a begin and end time.
 * 
 * 
 * Created: Tue Oct 1 21:23:44 2002
 * 
 * @author Philip Crotwell
 * @version $Id: Cut.java 21021 2010-02-05 19:45:30Z crotwell $
 */
public class Cut implements LocalSeismogramFunction {

    public Cut(MicroSecondDate begin, MicroSecondDate end) {
        this.begin = begin;
        this.end = end;
    }

    public Cut(RequestFilter request) {
        this(new MicroSecondDate(request.start_time),
             new MicroSecondDate(request.end_time));
    }

    /**
     * @return - a seismogram cut to the configured time window. The original
     *         seismogram is not modified. Returns null if no data is within the
     *         cut window.
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
            throws FissuresException {
        if(!overlaps(seis)) {
            return null;
        } else if(seis.getBeginTime().equals(getBegin())
                && seis.getEndTime().equals(getEnd())) {
            return seis;
        }
        // first trim unneeded encoded data. This helps a lot for large seismograms as no decompression needed
        LocalSeismogramImpl tmpSeis = applyEncoded(seis);
        int beginIndex = getBeginIndex(tmpSeis);
        int endIndex = getEndIndex(tmpSeis);
        return cut(tmpSeis, beginIndex, endIndex);
    }

    public static LocalSeismogramImpl cut(LocalSeismogramImpl seis, int beginIndex, int endIndex) throws FissuresException {
        LocalSeismogramImpl outSeis;
        if(seis.can_convert_to_short()) {
            short[] outS = new short[endIndex - beginIndex+1];
            short[] inS = seis.get_as_shorts();
            System.arraycopy(inS, beginIndex, outS, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outS);
        } else if(seis.can_convert_to_long()) {
            int[] outI = new int[endIndex - beginIndex+1];
            int[] inI = seis.get_as_longs();
            System.arraycopy(inI, beginIndex, outI, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outI);
        } else if(seis.can_convert_to_float()) {
            float[] outF = new float[endIndex - beginIndex+1];
            float[] inF = seis.get_as_floats();
            System.arraycopy(inF, beginIndex, outF, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outF);
        } else {
            double[] outD = new double[endIndex - beginIndex+1];
            double[] inD = seis.get_as_doubles();
            System.arraycopy(inD, beginIndex, outD, 0, endIndex - beginIndex+1);
            outSeis = new LocalSeismogramImpl(seis, outD);
        } // end of else
        outSeis.begin_time = seis.getBeginTime()
                .add((TimeInterval)seis.getSampling()
                        .getPeriod()
                        .multiplyBy(beginIndex))
                .getFissuresTime();
        return outSeis;
    }

    public boolean overlaps(LocalSeismogramImpl seis) {
        return begin.before(seis.getEndTime())
                && end.after(seis.getBeginTime());
    }

    protected int getEndIndex(LocalSeismogramImpl seis) {
        TimeInterval sampPeriod = seis.getSampling().getPeriod();
        QuantityImpl endShift = end.subtract(seis.getBeginTime());;
        endShift = endShift.divideBy(sampPeriod);
        endShift = endShift.convertTo(SEC_PER_SEC); // should be dimensonless
        int endIndex = (int)Math.floor(endShift.value);
        if(endIndex < 0) {
            endIndex = 0;
        }
        if(endIndex >= seis.getNumPoints()) {
            endIndex = seis.getNumPoints()-1;
        }
        return endIndex;
    }

    protected int getBeginIndex(LocalSeismogramImpl seis) {
        TimeInterval sampPeriod = seis.getSampling().getPeriod();
        QuantityImpl beginShift = begin.subtract(seis.getBeginTime());
        beginShift = beginShift.divideBy(sampPeriod);
        beginShift = beginShift.convertTo(SEC_PER_SEC); // should be
        // dimensonless
        int beginIndex = (int)Math.ceil(beginShift.value);
        if(beginIndex < 0) {
            beginIndex = 0;
        } // end of if (beginIndex < 0)
        if(beginIndex >= seis.getNumPoints()) {
            beginIndex = seis.getNumPoints() - 1;
        }
        return beginIndex;
    }

    public String toString() {
        return "Cut from " + begin + " to " + end;
    }

    public MicroSecondDate getBegin() {
        return begin;
    }

    public MicroSecondDate getEnd() {
        return end;
    }

    private MicroSecondDate begin, end;

    public static final UnitImpl SEC_PER_SEC = UnitImpl.divide(UnitImpl.SECOND,
                                                               UnitImpl.SECOND);

    public RequestFilter apply(RequestFilter original) {
        RequestFilter result = new RequestFilter();
        result.channel_id = original.channel_id;
        MicroSecondDate filterBegin = new MicroSecondDate(original.start_time);
        MicroSecondDate filterEnd = new MicroSecondDate(original.end_time);
        if(begin.after(filterEnd) || end.before(filterBegin)) {
            return null;
        } // end of if ()
        if(begin.after(filterBegin)) {
            result.start_time = begin.getFissuresTime();
        } else {
            result.start_time = original.start_time;
        }
        if(end.before(filterEnd)) {
            result.end_time = end.getFissuresTime();
        } else {
            result.end_time = original.end_time;
        }
        return result;
    }
    


    /**
     * Makes a seismogram covering as little extra beyond begin and end times of
     * this cut without extracting the data from the encoded data array. This
     * means there may be a few extra points around the begin and end time as
     * the encoded data segments probably won't line up with the cut times. If
     * the cut and the seismogram have no time in common, null is returned. If
     * the data isn't encoded, a regular cut is performed on it
     * 
     * @return an encoded seismogram covering as little of cut time as possible
     *         or null if there's no overlap
     * @throws FissuresException
     * 
     */
    public LocalSeismogramImpl applyEncoded(LocalSeismogramImpl seis)
            throws FissuresException {
        if(!seis.is_encoded()) {
            return seis;
        }
        if(!overlaps(seis)) {
            return null;
        }
        int beginIndex = getBeginIndex(seis);
        int endIndex = getEndIndex(seis);
        List outData = new ArrayList();
        EncodedData[] ed = seis.get_as_encoded();
        int currentPoint = 0;
        int firstUsedPoint = -1;
        int pointsInNewSeis = 0;
        for(int i = 0; i < ed.length && currentPoint < endIndex; i++) {
            if(currentPoint + ed[i].num_points > beginIndex) {
                outData.add(ed[i]);
                pointsInNewSeis += ed[i].num_points;
                if(firstUsedPoint == -1) {
                    firstUsedPoint = currentPoint;
                }
            }
            currentPoint += ed[i].num_points;
        }
        TimeSeriesDataSel ds = new TimeSeriesDataSel();
        ds.encoded_values((EncodedData[])outData.toArray(new EncodedData[outData.size()]));
        LocalSeismogramImpl outSeis = new LocalSeismogramImpl(seis, ds);
        outSeis.begin_time = seis.getBeginTime()
                .add((TimeInterval)seis.getSampling()
                        .getPeriod()
                        .multiplyBy(firstUsedPoint))
                .getFissuresTime();
        outSeis.num_points = pointsInNewSeis;
        return outSeis;
    }
}// Cut
