package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class EQDataEvent{
    private CacheEvent[] evs;

    public EQDataEvent(CacheEvent[] events){ evs = events; }

    public CacheEvent[] getEvents(){ return evs; }
}
