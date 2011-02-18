
package edu.sc.seis.fissuresUtil.cache;

import java.util.LinkedList;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * SeismogramBackgroundLoaderPool.java
 *
 *
 * Created: Mon Mar  5 20:54:16 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class SeismogramBackgroundLoaderPool  {

    
    public SeismogramBackgroundLoaderPool(int numWorkers) {
	for (int i=0; i<numWorkers; i++) {
	    idleWorker(new SeismogramBackgroundLoader(this));
	}
    }
    
    public SeismogramBackgroundLoaderPool(int numWorkers, 
				     SeismogramLoadedListener listener) {
	this(numWorkers);
	addSeismogramLoadedListener(listener);
    }

    public void getSeismogram(DataCenterOperations seisDC,
                              RequestFilter request,
			 SeismogramLoadedListener listener) {
	addToQueue(seisDC, request, listener);
    }

    public void addSeismogramLoadedListener(SeismogramLoadedListener listener) {
	listenerList.add(SeismogramLoadedListener.class, listener);
    }

    public void removeFooListener(SeismogramLoadedListener listener) {
	listenerList.remove(SeismogramLoadedListener.class, listener);
    }

    protected void fireSeismogramLoaded(RequestFilter filter,
                                        LocalSeismogram[] seis) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this seismogram
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==SeismogramLoadedListener.class) {
		((SeismogramLoadedListener)listeners[i+1]).seismogramLoaded(filter, seis);
	    }
	}
    }

    protected void fireSeismogramError(RequestFilter filter,
                                       FissuresException e) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this seismogram
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==SeismogramLoadedListener.class) {
		((SeismogramLoadedListener)listeners[i+1]).seismogramError(filter, e);
	    }
	}
    }

    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    protected synchronized void idleWorker(SeismogramBackgroundLoader loader) {
	idleWorkers.add(loader);
	notifyAll();
    }

    protected synchronized boolean isEmpty() {
	return queue.isEmpty();
    }

    protected synchronized SeismogramQueueElement getFromQueue() 
	throws InterruptedException 
    {
	while (queue.isEmpty()) {
	    wait();
	}
	return (SeismogramQueueElement)queue.removeLast();
    }

    protected synchronized SeismogramBackgroundLoader getWorker() 
	throws InterruptedException 
    {
	while (idleWorkers.isEmpty()) {
	    wait();
	}
	return (SeismogramBackgroundLoader)idleWorkers.removeLast();
    }

    protected synchronized void addToQueue(DataCenterOperations seisDC,
                                           RequestFilter request,
					   SeismogramLoadedListener listener) {
	queue.addFirst(new SeismogramQueueElement(seisDC, request, listener));
	notifyAll();
    }

    private LinkedList queue = new LinkedList();

    private LinkedList idleWorkers = new LinkedList();

} // SeismogramBackgroundLoaderPool
