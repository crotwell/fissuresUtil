
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import java.util.*;
import javax.swing.table.*;

/**
 * EventBackgroundLoader.java
 *
 *
 * Created: Fri Feb 23 22:25:35 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class EventBackgroundLoader {
    
    public EventBackgroundLoader(EventBackgroundLoaderPool pool) {
	this.pool = pool;

	Runnable r = new Runnable() {
		public void run() {
		    try {
			runWork();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    };
	privateThread = new Thread(eventLoaderThreadGroup,
                               r,
                               "EventLoader"+getThreadNum());
	privateThread.start();
    }

    public void runWork() {
	
	EventQueueElement q;
	EventAttr attr;
	Origin origin;
	while (noStopThread) {
	    try {
		q = pool.getFromQueue();
		attr = q.getCache().get_attributes();
		try {
		    origin = q.getCache().get_preferred_origin();
		} catch (NoPreferredOrigin ee) {
		}
		pool.fireEventLoaded(q.getCache());
	    } catch (InterruptedException e) {

	    }
	}
    }

    public void stopThread() {
	noStopThread = false;
    }

    private Thread privateThread;

    private volatile boolean noStopThread = true;

    private EventBackgroundLoaderPool pool;

    private static int threadNum = 0;

    private synchronized static int getThreadNum() {
        return threadNum++;
    }

    private ThreadGroup eventLoaderThreadGroup = new ThreadGroup("Event Loader");

} // EventBackgroundLoader
