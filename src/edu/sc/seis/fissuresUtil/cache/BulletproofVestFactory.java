package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class BulletproofVestFactory {

    /**
     * Bulletproofing the NetworkAccess combines our four delicious
     * ProxyNetworkAccess classes in a hard candy shell. The order is
     * SynchronizedDCNetworkAccess inside a NSNetworkAccess inside a
     * RetryNetworkAccess inside a CacheNetworkAccess. This means that all of
     * the results you get from this network access will be synched around the
     * DC, cached, if there are some transient network issues we'll retry 3
     * times, and if the network access reference goes stale we'll reget it from
     * the naming service. It'd is required to use a NSNetworkDC as the type of
     * NetworkDCOperations as it allows the NetworkDC to be refreshed from the
     * naming service if the reference goes stale
     * 
     * @deprecated - vestNetworkDC now vests everything that comes out of it, so
     *             if you use it you don't need to call this
     * 
     */
    public static ProxyNetworkAccess vestNetworkAccess(NetworkAccess na,
                                                       VestingNetworkFinder vnf) {
        // avoid vesting if it is already vested.
        if(na instanceof ProxyNetworkAccess) {
            return (ProxyNetworkAccess)na;
        }
        return VestingNetworkFinder.vest(na, vnf, getDefaultNumRetry());
    }

    public static ProxyEventAccessOperations vestEventAccess(EventAccessOperations eventAccess) {
        // this does not use the default retry as eventAccess currently cannot reset back to the
        // name service, so it makes more sense to fail after 3 tries than to keep retrying over
        // and over again. Also because of the cache, as long as the cache is populated right 
        // after the eventAccess is retrieved, there is little chance of a failure.
        return vestEventAccess(eventAccess, 3);
    }

    public static ProxyEventAccessOperations vestEventAccess(EventAccessOperations eventAccess,
                                                             int numRetry) {
        if(eventAccess instanceof CacheEvent) {
            return (ProxyEventAccessOperations)eventAccess;
        }
        RetryEventAccessOperations retry = new RetryEventAccessOperations(eventAccess,
                                                                          numRetry);
        CacheEvent cache = new CacheEvent(retry);
        return cache;
    }

    public static ProxyEventDC vestEventDC(String serverDNS,
                                           String serverName,
                                           FissuresNamingService fisName) {
        return vestEventDC(serverDNS, serverName, fisName, getDefaultNumRetry());
    }

    public static ProxyEventDC vestEventDC(String serverDNS,
                                           String serverName,
                                           FissuresNamingService fisName,
                                           int numRetry) {
        NSEventDC ns = new NSEventDC(serverDNS, serverName, fisName);
        RetryEventDC retry = new RetryEventDC(ns, numRetry);
        CacheEventDC cache = new CacheEventDC(retry);
        return cache;
    }

    /**
     * @deprecated - call new VestingNetworkDC directly
     */
    public static ProxyNetworkDC vestNetworkDC(String serverDNS,
                                               String serverName,
                                               FissuresNamingService fisName) {
        return new VestingNetworkDC(serverDNS, serverName, fisName);
    }

    /**
     * When reset is called on the NSNetworkFinder, the networkfinder gets
     * revested in a retry. Just be aware that any changes here need to be
     * checked there, as well.
     * 
     * @deprecated - the DCs returned by vestNetworkDC now vest their finders,
     *             so you should just vest there and forget about it
     */
    public static ProxyNetworkFinder vestNetworkFinder(ProxyNetworkDC netDC) {
        return new VestingNetworkFinder(netDC, getDefaultNumRetry());
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
        NSSeismogramDC ns = new NSSeismogramDC(serverDNS, serverName, fisName);
        RetrySeismogramDC retryDC = new RetrySeismogramDC(ns, numRetry);
        return retryDC;
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
        NSPlottableDC ns = new NSPlottableDC(serverDNS, serverName, fisName);
        RetryPlottableDC retry = new RetryPlottableDC(ns, numRetry);
        CachePlottableDC cache = new CachePlottableDC(retry);
        return cache;
    }

    public static int getDefaultNumRetry() {
        return defaultNumRetry;
    }

    public static void setDefaultNumRetry(int defaultNum) {
        defaultNumRetry = defaultNum;
    }
    
    /** Sleep for some time between retries. Each RetryXYZDC proxy uses this to retry less
     * frequently as the number of failures in a row increases.
     */
    public static void retrySleep(int count) {
        if (count>3) {
            try {
                if (count>10) {
                    Thread.sleep(10*sleepSeconds*1000);
                }else {
                    Thread.sleep(sleepSeconds*1000);
                }
            } catch(InterruptedException e) {
                //  oh well
            }
        }
    }

    protected static int sleepSeconds = 1;
    
    private static int defaultNumRetry = 3;
}
