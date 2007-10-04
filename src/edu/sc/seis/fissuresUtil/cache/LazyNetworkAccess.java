package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkIdUtil;

/**
 * Holds a NetworkAttr and a NetworkDC and lazily reconnects to the
 * NetworkAccess if needed using retrieve_by_id.
 */
public class LazyNetworkAccess extends CacheNetworkAccess {

    public LazyNetworkAccess(NetworkAttr attr, ProxyNetworkDC netDC) {
        super(null);
        this.attr = attr;
        this.netDC = netDC;
    }

    public NetworkAccess getNet() {
        if(super.getNet() == null) {
            try {
                net = netDC.a_finder().retrieve_by_id(attr.get_id());
            } catch(NetworkNotFound e) {
                throw new RuntimeException("unable to reconnect to networkaccess: "
                                                   + NetworkIdUtil.toString(attr),
                                           e);
            }
        }
        return super.getNet();
    }

    ProxyNetworkDC netDC;
}
