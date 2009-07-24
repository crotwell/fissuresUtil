package edu.sc.seis.fissuresUtil.bag;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;


/**
 * @author groves
 * Created on Oct 7, 2004
 */
public class SamplingUtil {


    public static List<ChannelImpl> inSampling(SamplingRange sampling, List<ChannelImpl> chans) {
        double minSPS = getSamplesPerSecond(sampling.min);
        double maxSPS = getSamplesPerSecond(sampling.max);
        List results = new ArrayList();
        for(ChannelImpl chan : chans) {
            double chanSPS = getSamplesPerSecond(chan.getSamplingInfo());
            if(minSPS <= chanSPS && chanSPS <= maxSPS) {
                results.add(chan);
            }
        }
        return results;
        
    }

    private static double getSamplesPerSecond(Sampling sampling) {
        double numSeconds = new TimeInterval(sampling.interval).convertTo(UnitImpl.SECOND).value;
        return sampling.numPoints / numSeconds;
    }
}
