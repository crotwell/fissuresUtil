package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public class EQDataEvent{
    private Object src;
    private EventAccessOperations[] evs;

    public EQDataEvent(Object source, EventAccessOperations[] events){
        src = source;
        evs = events;
    }

    public Object getSource(){
        return src;
    }

    public EventAccessOperations[] getEvents(){
        return evs;
    }
}
