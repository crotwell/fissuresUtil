package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

/**
 * BulletproofNetworkAccess combines our three delicious ProxyNetworkAccess
 * classes in a hard candy shell.  The order is NSNetworkAccess inside a
 * RetryNetworkAccess inside a CacheNetworkAccess.  This means that all of the
 * results you get from the network access will be cached, if there are some
 * transient network issues we'll retry 3 times, and if the network access
 * reference goes stale we'll reget it from the naming service.  It'd be wise
 * to use a NSNetworkDC as the type of NetworkDCOperations as it allows the
 * NetworkDC to be refreshed from the naming service if the reference goes stale
 *
 */
public class BulletproofNetworkAccess extends ProxyNetworkAccess{
    public BulletproofNetworkAccess(NetworkAccess na, NetworkDCOperations netDC, NetworkId id){
        super(new CacheNetworkAccess(new RetryNetworkAccess(new NSNetworkAccess(na, id, netDC), 3)));
    }
}

