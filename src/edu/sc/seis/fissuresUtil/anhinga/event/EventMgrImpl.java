package edu.sc.seis.fissuresUtil.anhinga.event;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventMgrPOA;

public class EventMgrImpl extends EventMgrPOA {

    public EventMgrImpl() {}

    public EventFactory a_factory() {
        return eventFactory;
    }

    public EventFinder a_finder() {
        return eventFinder;
    }

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
