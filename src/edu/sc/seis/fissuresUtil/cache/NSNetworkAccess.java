package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.TRANSIENT;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

/**
 * A NSNetworkAccess allows for the NetworkAccess reference inside of it to go
 * stale by resetting the NetworkAccess to a fresh value from the netDC passed
 * in in its constructor. A NSNetworkDC is probably a good choice for the type
 * of netDC to pass in since it will also allow the NetworkDC itself to go stale
 * and be refreshed from the naming service.
 */
public class NSNetworkAccess extends ProxyNetworkAccess {

    /**
     * A ProxyNetworkAccess is not allowed as the NetworkAccess for this network
     * access since calling reset on this will reset the network access from the
     * netDC and the behaviour will change back to whatever is provided by the
     * NetworkAccess returned by the netDC.
     */
    public NSNetworkAccess(NetworkId id, VestingNetworkFinder vnf)
            throws NetworkNotFound {
        this(getAccess(id, vnf), id, vnf);
    }

    public NSNetworkAccess(SynchronizedNetworkAccess na,
                           NetworkId id,
                           VestingNetworkFinder vnf) {
        super(na);
        this.id = id;
        this.vnf = vnf;
    }

    /**
     * Refreshes the network from the network dc
     */
    public void reset() {
        try {
            vnf.reset();
            net = getAccess(id, vnf);
        } catch(NetworkNotFound e) {
            TRANSIENT t = new TRANSIENT("Unable to find the network to reset it");
            t.initCause(e);
            throw t;
        }
    }

    private static SynchronizedNetworkAccess getAccess(NetworkId id,
                                                       VestingNetworkFinder vnf)
            throws NetworkNotFound {
        return new SynchronizedNetworkAccess(((ProxyNetworkAccess)vnf.retrieve_by_id(id)).getCorbaObject());
    }

    private VestingNetworkFinder vnf;

    private NetworkId id;

    public String getDNS() {
        return vnf.getDNS();
    }

    public String getName() {
        return vnf.getName();
    }
}