/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCOperations;

/**
 * @author oliverpa
 */
public abstract class ProxyEventDC implements EventDCOperations,
        CorbaServerWrapper {

    public void setEventDC(EventDCOperations eventDC) {
        this.eventDC = eventDC;
    }

    public org.omg.CORBA.Object getCorbaObject() {
        if(eventDC instanceof ProxyEventDC) {
            return ((ProxyEventDC)eventDC).getCorbaObject();
        } else {
            return (EventDC)eventDC;
        }
    }

    public String getServerDNS() {
        if(eventDC instanceof ProxyEventDC) {
            return ((ProxyEventDC)eventDC).getServerDNS();
        }
        return null;
    }

    public String getServerName() {
        if(eventDC instanceof ProxyEventDC) {
            return ((ProxyEventDC)eventDC).getServerName();
        }
        return null;
    }

    public String getServerType() {
        return EVENTDC_TYPE;
    }

    public void reset() {
        if(eventDC instanceof ProxyEventDC) {
            ((ProxyEventDC)eventDC).reset();
        }
    }

    public EventDCOperations getEventDC() {
        return eventDC;
    }

    public EventDCOperations getWrappedDC(Class wrappedClass) {
        if(getClass().equals(wrappedClass)) {
            return this;
        }
        if(getEventDC().getClass().equals(wrappedClass)) {
            return getEventDC();
        } else if(getEventDC().getClass().equals(ProxyEventDC.class)) {
            return ((ProxyEventDC)getEventDC()).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("This doesn't contain a DC of class "
                + wrappedClass);
    }

    protected EventDCOperations eventDC = null;
}
