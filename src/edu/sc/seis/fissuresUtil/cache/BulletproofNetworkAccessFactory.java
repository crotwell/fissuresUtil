package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

/**
 * BulletproofNetworkAccess combines our four delicious ProxyNetworkAccess
 * classes in a hard candy shell.  The order is NSNetworkAccess inside a
 * RetryNetworkAccess inside a CacheNetworkAccess inside a
 * SynchronizedDCNetworkAccess.  This means that all of the results you get from
 * this network access will be synched around the DC, cached, if there are some
 * transient network issues we'll retry 3 times, and if the network access
 * reference goes stale we'll reget it from the naming service.  It'd be wise
 * to use a NSNetworkDC as the type of NetworkDCOperations as it allows the
 * NetworkDC to be refreshed from the naming service if the reference goes stale
 *
 */
public class BulletproofNetworkAccessFactory{
    public static NetworkAccess vest(NetworkAccess na, NetworkDCOperations DC){
        SynchronizedNetworkAccess synch = new SynchronizedNetworkAccess(na);
        RetryNetworkAccess retry = new RetryNetworkAccess(synch, 3);
        CacheNetworkAccess cache = new CacheNetworkAccess(retry);
        NetworkId id = cache.get_attributes().get_id();
        synch.setNetworkAccess(new NSNetworkAccess(na, id, DC));
        return cache;
    }
}
