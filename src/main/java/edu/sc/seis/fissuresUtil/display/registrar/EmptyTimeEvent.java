package edu.sc.seis.fissuresUtil.display.registrar;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

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
        super(null,null, time);
    }
}// EmptyTimeEvent
