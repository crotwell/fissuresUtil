package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public class EventExtractor {

    public EventAccessOperations extract(Object o) {
        if(o instanceof EventAccessOperations){
            return (EventAccessOperations)o;
        }
        return null;
    }
}
