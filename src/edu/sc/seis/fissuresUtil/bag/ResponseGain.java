package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import java.util.HashMap;

/**
 * Applys the overall sensitivity to a seismogram. This is purely a scale
 * factor, no frequency change is done.
 *
 *
 * Created: Wed Nov  6 18:37:15 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class ResponseGain implements LocalSeismogramFunction {

    public ResponseGain (NetworkDC netdc){ this(netdc.a_finder()); }

    public ResponseGain(NetworkFinder netFinder){
        finder = netFinder;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
        throws ChannelNotFound, NetworkNotFound,  FissuresException {

        Instrumentation inst = getFromCache(seis.channel_id);
        if (inst == null) {
            NetworkAccess net = finder.retrieve_by_id(seis.channel_id.network_id);
            inst = net.retrieve_instrumentation(seis.channel_id, seis.begin_time);
            addToCache(seis.channel_id, inst);
        }
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
                out[i] = fSeries[i] * sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } else {
            double[] dSeries = seis.get_as_doubles();
            double[] out = new double[dSeries.length];
            for (int i=0; i<dSeries.length; i++) {
                out[i] = dSeries[i] * sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } // end of else
        outSeis.y_unit = inst.the_response.stages[0].input_units;
        return outSeis;
    }

    private NetworkFinder finder;

    protected void addToCache(ChannelId chan, Instrumentation inst) {
        instCache.put(ChannelIdUtil.toString(chan),
                      new InstrumentationDater(chan, inst));
    }

    protected Instrumentation getFromCache(ChannelId chan) {
        InstrumentationDater instD = (InstrumentationDater)instCache.get(ChannelIdUtil.toString(chan));
        if (instD != null) {
            return instD.inst;
        }
        return null;
    }

    private HashMap instCache = new HashMap();

    class InstrumentationDater {
        InstrumentationDater(ChannelId chan, Instrumentation inst) {
            this.chan = chan;
            this.inst = inst;
            this.date = ClockUtil.now();
        }
        ChannelId chan;
        Instrumentation inst;
        MicroSecondDate date;
    }
}// ResponseGain
