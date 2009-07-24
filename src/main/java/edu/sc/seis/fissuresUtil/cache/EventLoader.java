
package edu.sc.seis.fissuresUtil.cache;


/**
 * EventBackgroundLoader.java
 *
 *
 * Created: Fri Feb 23 22:25:35 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class EventLoader implements Runnable {
    public EventLoader(CacheEvent cache){
        // no impl the listener
        this(cache, new EventLoadedListener() {
            public void eventLoaded(ProxyEventAccessOperations event) {}
        });
    }
    public EventLoader(CacheEvent cache,EventLoadedListener listener){
        this.cache = cache;
        this.listener = listener;
    }

    public void run() {
        cache.get_attributes();
        cache.getOrigin();
        listener.eventLoaded(cache);
    }

    private CacheEvent cache;
    private EventLoadedListener listener;
} // EventBackgroundLoader
