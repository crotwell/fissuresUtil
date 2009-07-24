package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;

public class MagnitudeExtractor {

    public Magnitude extract(Object o) {
        if(o instanceof Magnitude) {
            return (Magnitude)o;
        }
        Origin origin = originExtractor.extract(o);
        if(origin != null) {
            return origin.getMagnitudes()[0];
        }
        return null;
    }
    
    private OriginExtractor originExtractor = new OriginExtractor();
}
