package edu.sc.seis.anhinga.event;


import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventMgrPOA;

/**
 * EventMgrImpl.java
 *
 *
 * Created: Mon Oct  1 12:58:10 2001
 *
 * @author <a href="mailto: "Srinivasa Telukutla</a>
 * @version
 */

public class EventMgrImpl extends EventMgrPOA{
    public EventMgrImpl (){

    }
    public EventFactory a_factory() {
    return eventFactory;
    }
  //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //
    /***/

    public EventFinder a_finder() {
    return eventFinder;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //
    /***/

    public EventChannelFinder a_channel_finder() {
    return null;
    }


    public void setEventFinder(EventFinder eventFinder) {

    this.eventFinder = eventFinder;
    }

    public void setEventFactory(EventFactory eventFactory) {

    this.eventFactory = eventFactory;
    }

    private EventFinder eventFinder = null;

    private EventFactory eventFactory = null;


}// EventMgrImpl
