package edu.sc.seis.fissuresUtil.flow.extractor.model;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.QuantityImpl;


public class DepthExtractor implements QuantityExtractor {

    public QuantityImpl extract(Object o) {
        if(o instanceof QuantityImpl) {
            return (QuantityImpl)o;
        }
        Location loc = le.extract(o);
        if(loc == null){
            return null;
        }
        return (QuantityImpl)loc.depth;
    }
    private LocationExtractor le = new LocationExtractor();
}
