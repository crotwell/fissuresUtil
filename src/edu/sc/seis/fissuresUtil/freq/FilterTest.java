
//package net.alomax.freq;
// change package
package edu.sc.seis.fissuresUtil.freq;

import edu.iris.Fissures.display.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;

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
	if (args.length != 1) {
	    System.out.println("Usage java edu.sc.seis.fissuresUtil.freq.FilterTest sacfile");
	    System.exit(0);
	} // end of if (args.length != 1)
	
	try {
	    SacTimeSeries sac = new SacTimeSeries();
	    sac.read(args[0]);
	    LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);

	    //	    LocalSeismogramImpl seis = 
	    //(LocalSeismogramImpl)SeisPlotUtil.createTestData();
	    float[] fdata;
	    if (seis.can_convert_to_long()) {
		int[] idata = seis.get_as_longs();
		fdata = new float[idata.length];
		for (int i=0; i<idata.length; i++) {
		    fdata[i] = idata[i];
		}
	    } else {
		fdata = seis.get_as_floats();
	    } // end of else
	    

	    Cmplx[] fftdata = Cmplx.fft(fdata);
	    SeisGramText localeText = new SeisGramText(null);
	    ButterworthFilter filter = new ButterworthFilter(localeText,
							     .1,
							     8.0,
							     2, ButterworthFilter.TWOPASS);
	    double dt = seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
	    Cmplx[] filtered = filter.apply(dt, fftdata);
	    float[] outdata = Cmplx.fftInverse(filtered, seis.getNumPoints());
	    TimeSeriesDataSel ts = new TimeSeriesDataSel();
	    ts.flt_values(outdata);
	    LocalSeismogramImpl outSeis = new LocalSeismogramImpl(seis, 
								  ts);
	    sac = FissuresToSac.getSAC(outSeis);
	    sac.write("filter.out");
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }


} // FilterTest
