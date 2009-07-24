package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

public class OriginExtractor {

    public Origin extract(Object o) {
        if(o instanceof Origin) {
            return (Origin)o;
        }
        EventAccessOperations ev = eventExtractor.extract(o);
        if(ev != null) {
            return EventUtil.extractOrigin(ev);
        }
        return null;
    }

    private EventExtractor eventExtractor = new EventExtractor();
}
