
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import java.util.*;

/**
 * EventBackgroundLoaderPool.java
 *
 *
 * Created: Mon Mar  5 20:54:16 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class EventBackgroundLoaderPool  {
    
    public EventBackgroundLoaderPool(int numWorkers, 
				     EventLoadedListener listener) {
	addEventLoadedListener(listener);
	for (int i=0; i<numWorkers; i++) {
	    idleWorker(new EventBackgroundLoader(this));
	}
    }

    public void getEvent(EventAccessOperations event,
			 CacheEvent cache,
			 EventLoadedListener  listener) {
	addToQueue(event, cache, listener);
    }

    public void addEventLoadedListener(EventLoadedListener listener) {
	listenerList.add(EventLoadedListener.class, listener);
    }

    public void removeFooListener(EventLoadedListener listener) {
	listenerList.remove(EventLoadedListener.class, listener);
    }

    protected void fireEventLoaded(CacheEvent cache) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==EventLoadedListener.class) {
		((EventLoadedListener)listeners[i+1]).eventLoaded(cache);
	    }
	}
    }

    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    protected synchronized void idleWorker(EventBackgroundLoader loader) {
	idleWorkers.add(loader);
	notifyAll();
    }

    protected synchronized boolean isEmpty() {
	return queue.isEmpty();
    }

    protected synchronized EventQueueElement getFromQueue() 
	throws InterruptedException 
    {
	while (queue.isEmpty()) {
	    wait();
	}
	return (EventQueueElement)queue.removeLast();
    }

    protected synchronized EventBackgroundLoader getWorker() 
	throws InterruptedException 
    {
	while (idleWorkers.isEmpty()) {
	    wait();
	}
	return (EventBackgroundLoader)idleWorkers.removeLast();
    }

    protected synchronized void addToQueue(EventAccessOperations event,
					   CacheEvent cache,
					   EventLoadedListener listener) {
	queue.addFirst(new EventQueueElement(event, cache, listener));
	notifyAll();
    }

    private LinkedList queue = new LinkedList();

    private LinkedList idleWorkers = new LinkedList();

} // EventBackgroundLoaderPool
