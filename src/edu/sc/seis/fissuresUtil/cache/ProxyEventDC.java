/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCOperations;

/**
 * @author oliverpa
 */
public abstract class ProxyEventDC implements EventDCOperations {
	
	public void setEventDC(EventDCOperations eventDC){
		this.eventDC = eventDC;
	}
	
	public EventDC getCorbaObject(){
		if (eventDC instanceof ProxyEventDC){
			return ((ProxyEventDC)eventDC).getCorbaObject();
		} else {
			return (EventDC)eventDC;
		}
	}
	
	public void reset(){
		if (eventDC instanceof ProxyEventDC){
			((ProxyEventDC)eventDC).reset();
		}
	}
	
    public EventDCOperations getWrappedDC() { return eventDC; }
	
    public EventDCOperations getWrappedDC(Class wrappedClass) {
        if(getWrappedDC().getClass().equals(wrappedClass)){
            return getWrappedDC();
        }else if(getWrappedDC().getClass().equals(ProxyEventDC.class)){
            ((ProxyEventDC)getWrappedDC()).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("This doesn't contain a DC of class " + wrappedClass);
    }
	
	protected EventDCOperations eventDC = null;
}
