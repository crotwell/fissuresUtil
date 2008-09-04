package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;

/**
 * Holds a NetworkAttr and a NetworkDC and lazily reconnects to the
 * NetworkAccess if needed using retrieve_by_id.
 */
public class LazyNetworkAccess extends CacheNetworkAccess {

    public LazyNetworkAccess(NetworkAttrImpl attr, ProxyNetworkDC netDC) {
        super(null);
        this.attr = attr;
        this.netDC = netDC;
    }

    public NetworkAccess getNetworkAccess() {
        if(super.getNetworkAccess() == null) {
            try {
                setNetworkAccess(netDC.a_finder().retrieve_by_id(attr.get_id()));
            } catch(NetworkNotFound e) {
                throw new RuntimeException("unable to reconnect to networkaccess: "
                                                   + NetworkIdUtil.toString(attr),
                                           e);
            }
        }
        return super.getNetworkAccess();
    }

    ProxyNetworkDC netDC;
}
