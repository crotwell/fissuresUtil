package edu.sc.seis.fissuresUtil.bag;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.SphericalCoords;


/**
 * @author groves
 * Created on Oct 7, 2004
 */
public class SamplingUtil {


    public static Channel[] inSampling(SamplingRange sampling, Channel[] chans) {
        double minSPS = getSamplesPerSecond(sampling.min);
        double maxSPS = getSamplesPerSecond(sampling.max);
        List results = new ArrayList();
        for(int i = 0; i < chans.length; i++) {
            Channel chan = chans[i];
            double chanSPS = getSamplesPerSecond(chan.sampling_info);
            if(minSPS <= chanSPS && chanSPS <= maxSPS) {
                results.add(chan);
            }
        }
        return (Channel[])results.toArray(new Channel[results.size()]);
        
    }

    private static double getSamplesPerSecond(Sampling sampling) {
        double numSeconds = new TimeInterval(sampling.interval).convertTo(UnitImpl.SECOND).value;
        return sampling.numPoints / numSeconds;
    }
}
