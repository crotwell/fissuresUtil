package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfTimeSeries.*;

/**
 * Calculus.java
 *
 *
 * Created: Thu Aug 15 14:47:23 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class Calculus {
    public Calculus (){
	
    }
    
    public static int[] diff(int[] data) {
	int[] out = new int[data.length-1];
	for (int i=0; i<out.length; i++) {
	    out[i] = data[i+1] - data[i];
	} // end of for (int i=0; i<out.length; i++)
	return out;
    }

    public static LocalSeismogramImpl diff(LocalSeismogramImpl seis) {
	int[] seisData = seis.get_as_longs();
	int[] out = diff(seisData);
	TimeSeriesDataSel outData = new TimeSeriesDataSel();
	outData.int_values(out);
	return new LocalSeismogramImpl(seis, outData);
    }

    public static LocalSeismogramImpl integrate(LocalSeismogramImpl seis) {
	int[] seisData = seis.get_as_longs();
	TimeInterval sampPeriod = seis.getSampling().getPeriod();
	UnitImpl outUnit = UnitImpl.multiply(seis.getUnit(), 
					     sampPeriod.getUnit());
	MicroSecondDate outBeginTime = 
	    new MicroSecondDate(seis.getBeginTime());
	outBeginTime = outBeginTime.add((TimeInterval)sampPeriod.divideBy(2));
	float[] out = new float[seisData.length-1];
	for (int i=0; i<out.length; i++) {
	    out[i] = 
		(float)((seisData[i]+seisData[i+1])/2.0 * sampPeriod.value);
	} // end of for (int i=0; i<out.length; i++)
	
	TimeSeriesDataSel outData = new TimeSeriesDataSel();
	outData.flt_values(out);
	return new LocalSeismogramImpl(seis.get_id(),
				       seis.properties,
				       outBeginTime.getFissuresTime(),
				       out.length,
				       seis.getSampling(),
				       outUnit,
				       seis.channel_id,
				       seis.parm_ids,
				       seis.time_corrections,
				       seis.sample_rate_history, 
				       outData);
    }

}// Calculus
