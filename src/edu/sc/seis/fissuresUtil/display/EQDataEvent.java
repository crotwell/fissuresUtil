package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public class EQDataEvent{
    private EventAccessOperations[] evs;

    public EQDataEvent(EventAccessOperations[] events){ evs = events; }

    public EventAccessOperations[] getEvents(){ return evs; }
}
