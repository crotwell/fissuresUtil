package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
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
 * @version $Id: Cut.java 16417 2006-03-13 20:15:20Z groves $
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
        if(begin.after(seis.getEndTime()) || end.before(seis.getBeginTime())) {
            return null;
        } // end of if ()
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
        QuantityImpl endShift = seis.getEndTime().subtract(end);
        endShift = endShift.divideBy(sampPeriod);
        endShift = endShift.convertTo(SEC_PER_SEC); // should be dimensonless
        int endIndex = seis.getNumPoints() - (int)Math.floor(endShift.value);
        if(endIndex < 0) {
            endIndex = 0;
        }
        if(endIndex > seis.getNumPoints()) {
            endIndex = seis.getNumPoints();
        }
        LocalSeismogramImpl outSeis;
        if(seis.can_convert_to_short()) {
            short[] outS = new short[endIndex - beginIndex];
            short[] inS = seis.get_as_shorts();
            System.arraycopy(inS, beginIndex, outS, 0, endIndex - beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outS);
        } else if(seis.can_convert_to_long()) {
            int[] outI = new int[endIndex - beginIndex];
            int[] inI = seis.get_as_longs();
            System.arraycopy(inI, beginIndex, outI, 0, endIndex - beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outI);
        } else if(seis.can_convert_to_float()) {
            float[] outF = new float[endIndex - beginIndex];
            float[] inF = seis.get_as_floats();
            System.arraycopy(inF, beginIndex, outF, 0, endIndex - beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outF);
        } else {
            double[] outD = new double[endIndex - beginIndex];
            double[] inD = seis.get_as_doubles();
            System.arraycopy(inD, beginIndex, outD, 0, endIndex - beginIndex);
            outSeis = new LocalSeismogramImpl(seis, outD);
        } // end of else
        outSeis.begin_time = seis.getBeginTime()
                .add((TimeInterval)sampPeriod.multiplyBy(beginIndex))
                .getFissuresTime();
        return outSeis;
    }

    public String toString() {
        return "Cut from " + begin + " to " + end;
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
}// Cut
