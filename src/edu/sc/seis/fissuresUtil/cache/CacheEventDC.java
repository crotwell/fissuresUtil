/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * @author oliverpa
 */
public class CacheEventDC extends ProxyEventDC {

	public CacheEventDC(ProxyEventDC eventDC) {
		setEventDC(eventDC);
	}

	public void reset() {
		evFinder = null;
		evChanFinder = null;
		super.reset();
	}

	public EventFinder a_finder() {
		if (evFinder == null){
			evFinder = eventDC.a_finder();
		}
		return evFinder;
	}

	public EventChannelFinder a_channel_finder() {
		if (evChanFinder == null){
			evChanFinder = eventDC.a_channel_finder();
		}
		return evChanFinder;
	}

	protected EventFinder evFinder;

	protected EventChannelFinder evChanFinder;
}