
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import java.util.LinkedList;

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

    private static EventBackgroundLoaderPool singleton = null;

    public synchronized static EventBackgroundLoaderPool getLoaderPool() {
        return getLoaderPool(5);
    }

    public synchronized static EventBackgroundLoaderPool getLoaderPool(int numWorkers) {
        if (singleton == null) {
            singleton = new EventBackgroundLoaderPool(numWorkers);
        }
        return singleton;
    }

    protected EventBackgroundLoaderPool(int numWorkers) {
        this.numWorkers = numWorkers;
        for (int i=0; i<numWorkers; i++) {
            idleWorker(new EventBackgroundLoader(this));
        }
    }

    public void getEvent(EventAccessOperations event,
                         CacheEvent cache,
                         EventLoadedListener  listener) {
        addToQueue(event, cache, listener);
    }

    /** Adds the listener to the list to be notified of ALL events as they are
     *  loaded. Most applications will not want to use this, but it may be
     *  useful for caching mechanisms. */
    public void addEventLoadedListener(EventLoadedListener listener) {
        listenerList.add(EventLoadedListener.class, listener);
    }

    public void removeEventLoadedListener(EventLoadedListener listener) {
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
        throws InterruptedException  {
        while (queue.isEmpty()) {
            wait();
        }
        return (EventQueueElement)queue.removeLast();
    }

    protected synchronized EventBackgroundLoader getWorker()
        throws InterruptedException  {
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

    private int numWorkers;

} // EventBackgroundLoaderPool
