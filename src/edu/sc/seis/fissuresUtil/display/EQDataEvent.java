package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class EQDataEvent{
    private EventAccessOperations[] evs;

    public EQDataEvent(EventAccessOperations[] events){ evs = events; }

    public EventAccessOperations[] getEvents(){ return evs; }
}
