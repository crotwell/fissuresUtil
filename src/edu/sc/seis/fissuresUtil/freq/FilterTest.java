
//package net.alomax.freq;
// change package
package edu.sc.seis.fissuresUtil.freq;

import edu.iris.Fissures.display.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;

/**
 * FilterTest.java
 *
 *
 * Created: Tue Jun  6 15:06:02 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class FilterTest  {
    
    public FilterTest() {
        
    }
    
    public static void main(String[] args) {
	try {
	    LocalSeismogramImpl seis = 
		(LocalSeismogramImpl)SeisPlotUtil.createTestData();
	    int[] idata = seis.get_as_longs();
	    float[] fdata = new float[idata.length];
	    for (int i=0; i<idata.length; i++) {
		fdata[i] = idata[i];
	    }
	    Cmplx[] fftdata = Cmplx.fft(fdata);
	    SeisGramText localeText = new SeisGramText(null);
	    ButterworthFilter filter = new ButterworthFilter(localeText,
							     .01,
							     1.0,
							     4, ButterworthFilter.NONCAUSAL);
	    double dt = seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
	    Cmplx[] filtered = filter.apply(dt, fftdata);
	    float[] outdata = Cmplx.fftInverse(filtered, seis.getNumPoints());
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }


} // FilterTest
