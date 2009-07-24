package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public class EQDataEvent {

    // list of EventAccessOperations
    private List evs;

    public EQDataEvent(EventAccessOperations[] events) {
        evs = new ArrayList();
        for(int i = 0; i < events.length; i++) {
            evs.add(events[i]);
        }
    }

    public EQDataEvent(List events) {
        evs = events;
    }

    public List getEvents() {
        return evs;
    }
}
