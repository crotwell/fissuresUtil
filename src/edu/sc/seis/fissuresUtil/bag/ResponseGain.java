package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Applys the overall sensitivity to a seismogram. This is purely a scale
 * factor, no frequency change is done.
 * 
 * 
 * Created: Wed Nov 6 18:37:15 2002
 * 
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */
public class ResponseGain {

    /**
     * Applies the overall sensitivity of the response to the seismogram. This
     * will promote short or int based seismograms to float to avoid rounding
     * and overflow problems.
     */
    public static LocalSeismogramImpl apply(LocalSeismogramImpl seis,
                                            Instrumentation inst)
            throws FissuresException {
        if(!isValid(inst)) {
            throw new IllegalArgumentException("Invalid instrumentation for "
                    + ChannelIdUtil.toString(seis.channel_id));
        }
        return apply(seis,
                     inst.the_response.the_sensitivity,
                     inst.the_response.stages[0].input_units);
    }

    public static LocalSeismogramImpl apply(LocalSeismogramImpl seis,
                                            Sensitivity sensitivity,
                                            Unit initialUnits)
            throws FissuresException {
        // Sensitivity is COUNTs per Ground Motion, so should divide in order to
        // convert COUNT seismogram into Ground Motion.
        LocalSeismogramImpl outSeis;
        // don't use int or short, promote to float
        if(seis.can_convert_to_float()) {
            float[] fSeries = seis.get_as_floats();
            float[] out = new float[fSeries.length];
            for(int i = 0; i < fSeries.length; i++) {
                out[i] = fSeries[i] / sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } else {
            double[] dSeries = seis.get_as_doubles();
            double[] out = new double[dSeries.length];
            for(int i = 0; i < dSeries.length; i++) {
                out[i] = dSeries[i] / sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } // end of else
        outSeis.y_unit = initialUnits;
        return outSeis;
    }

    public static boolean isValid(Instrumentation inst) {
        Response resp = inst.the_response;
        return resp.stages.length != 0 && isValid(resp.the_sensitivity);
    }

    public static boolean isValid(Sensitivity sens) {
        return sens.frequency != -1 && sens.sensitivity_factor != -1;
    }
}// ResponseGain
