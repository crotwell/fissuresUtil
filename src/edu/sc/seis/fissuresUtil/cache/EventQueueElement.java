
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * EventQueueElement.java
 *
 *
 * Created: Mon Mar  5 21:02:21 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class EventQueueElement  {
    
    public EventQueueElement(EventAccessOperations event,
			     CacheEvent cache,
			     EventLoadedListener listener) {
	this.event = event;
	this.cache = cache;
	this.listener = listener;
    }

    CacheEvent getCache() {
	return cache;
    }

    EventAccessOperations event;
    CacheEvent cache;
    EventLoadedListener listener;

} // EventQueueElement
