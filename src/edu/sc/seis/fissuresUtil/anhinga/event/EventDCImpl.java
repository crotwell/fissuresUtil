package edu.sc.seis.fissuresUtil.anhinga.event;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDCPOA;
import edu.iris.Fissures.IfEvent.EventFinder;

public class EventDCImpl extends EventDCPOA {

    public EventFinder a_finder() {
        return eventFinder;
    }

    public EventChannelFinder a_channel_finder() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void setEventFinder(EventFinder eventFinder) {
        this.eventFinder = eventFinder;
    }

    private EventFinder eventFinder = null;
} // EventDCImpl
