/**
 * ButterworthFilter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;

public class ButterworthFilter
    extends edu.sc.seis.fissuresUtil.freq.ButterworthFilter
    implements LocalSeismogramFunction {

    public ButterworthFilter(double lowFreqCorner,
                             double highFreqCorner,
                             int numPoles){
        super(new SeisGramText(), lowFreqCorner, highFreqCorner, numPoles);
    }

    public ButterworthFilter(double lowFreqCorner,
                             double highFreqCorner,
                             int numPoles,
                             int filterType){
        super(new SeisGramText(), lowFreqCorner, highFreqCorner, numPoles, filterType);
    }

    /** Applys the cut to the seismogram. Returns null if no data is within
     *  the cut window.
     */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws FissuresException {
        float[] fdata;
        fdata = seis.get_as_floats(); // throws FissuresException if double data
        Cmplx[] fftdata = Cmplx.fft(fdata);
        //save memory
        fdata = null;
        double dt = seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
        Cmplx[] filtered = apply(dt, fftdata);
        // save memory
        fftdata = null;
        float[] outData = Cmplx.fftInverse(filtered, seis.getNumPoints());

        TimeSeriesDataSel sel = new TimeSeriesDataSel();
        sel.flt_values(outData);
        return new LocalSeismogramImpl(seis, sel);
    }


}

