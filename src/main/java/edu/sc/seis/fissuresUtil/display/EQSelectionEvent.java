package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfEvent.EventAccessOperations;



public class EQSelectionEvent{

    private EventAccessOperations[] evts;
    private Object src;

    public EQSelectionEvent(Object source, EventAccessOperations[] events){
        src = source;
        evts = events;
    }

    public EventAccessOperations[] getEvents(){
        return evts;
    }

    public Object getSource(){
        return src;
    }
}
