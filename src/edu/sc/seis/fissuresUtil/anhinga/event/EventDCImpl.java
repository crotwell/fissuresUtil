

package edu.sc.seis.anhinga.event;



import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDCPOA;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * EventDCImpl.java
 *
 *
 * Created: Mon Jun 11 14:23:39 2001
 *
 * @author Srinivasa Telukutla
 * @version
 */

public class EventDCImpl extends EventDCPOA {

     //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //


    public EventFinder
    a_finder() {

    return eventFinder;
    //throw new org.omg.CORBA.NO_IMPLEMENT();

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //


    public EventChannelFinder
    a_channel_finder() {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


     public void setEventFinder(EventFinder eventFinder) {

    this.eventFinder = eventFinder;
    }

    private EventFinder eventFinder = null;



} // EventDCImpl
