package edu.sc.seis.fissuresUtil.display;

/**
 * EmptyTimeEvent.java
 *
 *
 * Created: Tue Oct 22 12:50:47 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class EmptyTimeEvent extends TimeEvent {
    public EmptyTimeEvent (MicroSecondTimeRange time){
	super(null,null);
	this.genericTime = time;
    }
    
    public MicroSecondTimeRange getTime(){
	return genericTime;
    }
    
    private MicroSecondTimeRange genericTime;
}// EmptyTimeEvent
