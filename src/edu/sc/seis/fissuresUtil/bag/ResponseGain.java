package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;

/**
 * ResponseGain.java
 *
 *
 * Created: Wed Nov  6 18:37:15 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class ResponseGain implements LocalSeismogramFunction {
    public ResponseGain (NetworkAccess net){
	this.net = net;
    }

    public ResponseGain (NetworkDC netdc){
	this.netdc = netdc;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) 
	throws ChannelNotFound, NetworkNotFound
    {
	if (netdc != null) {
	    net = netdc.a_finder().retrieve_by_id(seis.channel_id.network_id);
	} // end of if (netdc != null)
	
	Instrumentation inst = 
	    net.retrieve_instrumentation(seis.channel_id, 
					 seis.begin_time);
	Sensitivity sensitivity = inst.the_response.the_sensitivity;
	LocalSeismogramImpl outSeis;

	if (seis.can_convert_to_short()) {
	    short[] sSeries = seis.get_as_shorts();
	    short[] out = new short[sSeries.length];
	    for (int i=0; i<sSeries.length; i++) {
		out[i] = (short)(sSeries[i] * sensitivity.sensitivity_factor);
	    }
	    outSeis = new LocalSeismogramImpl(seis, out);
	} else if (seis.can_convert_to_long()) {
	    int[] iSeries = seis.get_as_longs();
	    int[] out = new int[iSeries.length];
	    for (int i=0; i<iSeries.length; i++) {
		out[i] = (short)(iSeries[i] * sensitivity.sensitivity_factor);
	    }
	    outSeis = new LocalSeismogramImpl(seis, out);
	} else if (seis.can_convert_to_float()) {
	    float[] fSeries = seis.get_as_floats();
	    float[] out = new float[fSeries.length];
	    for (int i=0; i<fSeries.length; i++) {
		out[i] = (float)(fSeries[i] * sensitivity.sensitivity_factor);
	    }
	    outSeis = new LocalSeismogramImpl(seis, out);
	} else {
	    double[] dSeries = seis.get_as_doubles();
	    double[] out = new double[dSeries.length];
	    for (int i=0; i<dSeries.length; i++) {
		out[i] = (dSeries[i] * sensitivity.sensitivity_factor);
	    }
	    outSeis = new LocalSeismogramImpl(seis, out);	 
	} // end of else
	outSeis.y_unit = inst.the_response.stages[0].input_units;
	return outSeis;
    }

    NetworkAccess net = null;

    NetworkDC netdc = null;

}// ResponseGain
