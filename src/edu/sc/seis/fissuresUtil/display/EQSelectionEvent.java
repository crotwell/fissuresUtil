package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfEvent.EventAccessOperations;



public class EQSelectionEvent{

    private EventAccessOperations ev;
    private Object src;

    public EQSelectionEvent(Object source, EventAccessOperations event){
        src = source;
        ev = event;
    }

    public EventAccessOperations getEvent(){
        return ev;
    }

    public Object getSource(){
        return src;
    }
}
