package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

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

    /** Applys the overall sensitivity of the response to the seismogram. This
     * will promote short or int based seismograms to float to avoid rounding
     * and overflow problems. */
    public LocalSeismogramImpl apply(LocalSeismogramImpl seis)
        throws ChannelNotFound, NetworkNotFound,  FissuresException {

        Instrumentation inst = getInstrumentation(seis.channel_id, seis.begin_time);

        /* Sensitivity is COUNTs per Groung Motion, so should divide in order
         * to convert COUNT seismogram into Ground Motion. */
        Sensitivity sensitivity = inst.the_response.the_sensitivity;
        LocalSeismogramImpl outSeis;

        // don't use int or short, promote to float
        if (seis.can_convert_to_float()) {
            float[] fSeries = seis.get_as_floats();
            float[] out = new float[fSeries.length];
            for (int i=0; i<fSeries.length; i++) {
                out[i] = fSeries[i] / sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } else {
            double[] dSeries = seis.get_as_doubles();
            double[] out = new double[dSeries.length];
            for (int i=0; i<dSeries.length; i++) {
                out[i] = dSeries[i] / sensitivity.sensitivity_factor;
            }
            outSeis = new LocalSeismogramImpl(seis, out);
        } // end of else
        outSeis.y_unit = inst.the_response.stages[0].input_units;
        logger.debug("NOAMP seis units are "+outSeis.y_unit);
        return outSeis;
    }

    public Instrumentation getInstrumentation(ChannelId channel_id, Time begin_time) throws NetworkNotFound, ChannelNotFound {
        Instrumentation inst = getFromCache(channel_id, begin_time);
        if (inst == null) {
            NetworkAccess net = finder.retrieve_by_id(channel_id.network_id);
            inst = net.retrieve_instrumentation(channel_id, begin_time);
            addToCache(channel_id, inst);
        }
        return inst;
    }

    private NetworkFinder finder;

    public void addToCache(ChannelId chan, Instrumentation inst) {
        List instList = (List)instCache.get(ChannelIdUtil.toString(chan));
        if (instList == null) {
            instList = new LinkedList();
            instCache.put(ChannelIdUtil.toString(chan), instList);
        }
        instList.add(new InstrumentationDater(chan, inst));
    }

    public Instrumentation getFromCache(ChannelId chan, Time begin_time) {
        List instList = (List)instCache.get(ChannelIdUtil.toString(chan));
        if(instList == null) { return null; }
        Iterator it = instList.iterator();
        while (it.hasNext()) {
            InstrumentationDater instD = (InstrumentationDater)it.next();
            MicroSecondTimeRange timeRange = new MicroSecondTimeRange(instD.inst.effective_time);
            if (instD != null && timeRange.intersects(new MicroSecondDate(begin_time))) {
                return instD.inst;
            }
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


    private static final Logger logger = Logger.getLogger(ResponseGain.class);

}// ResponseGain
