package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class BulletproofVestFactory {

    public static ProxyEventAccessOperations vestEventAccess(EventAccessOperations eventAccess) {
        // this does not use the default retry as eventAccess currently cannot
        // reset back to the
        // name service, so it makes more sense to fail after 3 tries than to
        // keep retrying over
        // and over again. Also because of the cache, as long as the cache is
        // populated right
        // after the eventAccess is retrieved, there is little chance of a
        // failure.
        return vestEventAccess(eventAccess, 3);
    }

    public static ProxyEventAccessOperations vestEventAccess(EventAccessOperations eventAccess,
                                                             int numRetry) {
        if (eventAccess == null) {
            throw new IllegalArgumentException("eventAccess cannot be null");
        }
        if(eventAccess instanceof CacheEvent) {
            return (ProxyEventAccessOperations)eventAccess;
        }
        RetryEventAccessOperations retry = new RetryEventAccessOperations(eventAccess,
                                                                          numRetry);
        CacheEvent cache = new CacheEvent(retry);
        return cache;
    }

    public static ProxyEventAccessOperations[] vestEventAccess(EventAccessOperations[] events) {
        return vestEventAccess(events, 3);
    }
    
    public static ProxyEventAccessOperations[] vestEventAccess(EventAccessOperations[] events, int numRetry) {
        ProxyEventAccessOperations[] out = new ProxyEventAccessOperations[events.length];
        for (int i = 0; i < events.length; i++) {
            out[i] = vestEventAccess(events[i], numRetry);
        }
        return out;
    }

    public static ProxyEventDC vestEventDC(String serverDNS,
                                           String serverName,
                                           FissuresNamingService fisName) {
        return vestEventDC(serverDNS,
                           serverName,
                           fisName,
                           new ClassicRetryStrategy(getDefaultNumRetry()));
    }

    public static ProxyEventDC vestEventDC(String serverDNS,
                                           String serverName,
                                           FissuresNamingService fisName,
                                           RetryStrategy strat) {
        NSEventDC ns = new NSEventDC(serverDNS, serverName, fisName);
        RetryEventDC retry = new RetryEventDC(ns, strat);
        CacheEventDC cache = new CacheEventDC(retry);
        return cache;
    }

    public static ProxySeismogramDC vestSeismogramDC(String serverDNS,
                                                     String serverName,
                                                     FissuresNamingService fisName) {
        return vestSeismogramDC(serverDNS,
                                serverName,
                                fisName,
                                getDefaultNumRetry());
    }

    public static ProxySeismogramDC vestSeismogramDC(String serverDNS,
                                                     String serverName,
                                                     FissuresNamingService fisName,
                                                     int numRetry) {
        return vestSeismogramDC(serverDNS,
                                serverName,
                                fisName,
                                new ClassicRetryStrategy(numRetry));
    }

    public static ProxySeismogramDC vestSeismogramDC(String serverDNS,
                                                     String serverName,
                                                     FissuresNamingService fisName,
                                                     RetryStrategy strat) {
        NSSeismogramDC ns = new NSSeismogramDC(serverDNS, serverName, fisName);
        RetrySeismogramDC retryDC = new RetrySeismogramDC(ns, strat);
return retryDC;
        // make sure DC doesn't send lots more data than we asked for
//        return new CoarseRequestCutSeismogramDC(retryDC); 
    }

    public static ProxyPlottableDC vestPlottableDC(String serverDNS,
                                                   String serverName,
                                                   FissuresNamingService fisName) {
        return vestPlottableDC(serverDNS,
                               serverName,
                               fisName,
                               getDefaultNumRetry());
    }

    public static ProxyPlottableDC vestPlottableDC(String serverDNS,
                                                   String serverName,
                                                   FissuresNamingService fisName,
                                                   int numRetry) {
        return vestPlottableDC(serverDNS,
                               serverName,
                               fisName,
                               new ClassicRetryStrategy(numRetry));
    }

    public static ProxyPlottableDC vestPlottableDC(String serverDNS,
                                                   String serverName,
                                                   FissuresNamingService fisName,
                                                   RetryStrategy strat) {
        NSPlottableDC ns = new NSPlottableDC(serverDNS, serverName, fisName);
        RetryPlottableDC retry = new RetryPlottableDC(ns, strat);
        CachePlottableDC cache = new CachePlottableDC(retry);
        return cache;
    }

    public static int getDefaultNumRetry() {
        return defaultNumRetry;
    }

    public static void setDefaultNumRetry(int defaultNum) {
        defaultNumRetry = defaultNum;
    }

    /**
     * Sleep for some time between retries. Each RetryXYZDC proxy uses this to
     * retry less frequently as the number of failures in a row increases.
     */
    public static void retrySleep(int count) {
        if(count > 1) {
            try {
                if(count > 30) {
                    Thread.sleep((defaultTimeoutSeconds+ 300)* sleepSeconds * 1000);
                } else if(count > 10) {
                        Thread.sleep((defaultTimeoutSeconds+ 10 * count) * sleepSeconds * 1000);
                } else {
                    Thread.sleep(defaultTimeoutSeconds * sleepSeconds * 1000);
                }
            } catch(InterruptedException e) {
                // oh well
            }
        }
    }

    protected static int defaultTimeoutSeconds = 20;
    
    protected static int sleepSeconds = 1;

    private static int defaultNumRetry = 3;
}
