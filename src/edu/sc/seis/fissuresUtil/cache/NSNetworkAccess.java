package edu.sc.seis.fissuresUtil.cache;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import org.omg.CORBA.TRANSIENT;

/**
 * A NSNetworkAccess allows for the NetworkAccess reference inside of it to go
 * stale by resetting the NetworkAccess to a fresh value from the netDC passed
 * in in its constructor.  A NSNetworkDC is probably a good choice for the type
 * of netDC to pass in since it will also allow the NetworkDC itself to go stale
 * and be refreshed from the naming service.
 */
public class NSNetworkAccess extends ProxyNetworkAccess{
    /**
     * A ProxyNetworkAccess is not allowed as the NetworkAccess for this network
     * access since calling reset on this will reset the network access from the
     * netDC and the behaviour will change back to whatever is provided by the
     * NetworkAccess returned by the netDC.
     */
    public NSNetworkAccess(NetworkId id, NetworkDCOperations netDC) throws NetworkNotFound{
        this(netDC.a_finder().retrieve_by_id(id), id, netDC);
    }

    public NSNetworkAccess(NetworkAccess na, NetworkId id, NetworkDCOperations netDC){
        super(na);
        this.id = id;
        this.netDC = netDC;
    }

    /**
     * Refreshes the network from the network dc
     */
    public void reset(){
        try {
            net = netDC.a_finder().retrieve_by_id(id);
        } catch (NetworkNotFound e) {
            TRANSIENT t = new TRANSIENT("Unable to find the network to reset it");
            t.initCause(e);
            throw t;
        }
    }

    private NetworkDCOperations netDC;
    private NetworkId id;
}
