/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * @author oliverpa
 */
public abstract class ProxyEventDC implements EventDCOperations {
	
	public void setEventDC(EventDC eventDC){
		this.eventDC = eventDC;
	}
	
	public void reset(){
		if (eventDC instanceof ProxyEventDC){
			((ProxyEventDC)eventDC).reset();
		}
	}
	
	public EventFinder a_finder() {
		if (eventDC != null){
			return eventDC.a_finder();
		}
		else {
			throw new org.omg.CORBA.NO_IMPLEMENT();
		}
	}

	public EventChannelFinder a_channel_finder() {
		if (eventDC != null){
			return eventDC.a_channel_finder();
		}
		else {
			throw new org.omg.CORBA.NO_IMPLEMENT();
		}
	}

	protected EventDC eventDC = null;
}
