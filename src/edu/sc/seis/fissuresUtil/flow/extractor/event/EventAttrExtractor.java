package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;

public class EventAttrExtractor {

    public EventAttr extract(Object o) {
        if(o instanceof EventAttr) {
            return (EventAttr)o;
        }
        EventAccessOperations ev = ee.extract(o);
        if(ev != null) {
            return ev.get_attributes();
        }
        return null;
    }

    private EventExtractor ee = new EventExtractor();
}
