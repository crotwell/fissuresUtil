package edu.sc.seis.fissuresUtil.cache;


/**
 * EventLoadedListener.java
 * Allows a backgroung event loader to notify interested parties that the
 * attributes and preferred origin have been added to the cache for the event.
 *
 * Created: Fri Jan 25 12:22:38 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface EventLoadedListener extends java.util.EventListener {

    public void eventLoaded(ProxyEventAccessOperations event);
	    
}// EventLoadedListener
