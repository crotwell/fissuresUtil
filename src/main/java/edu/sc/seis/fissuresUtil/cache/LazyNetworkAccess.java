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
        this(attr, (ProxyNetworkFinder)netDC.a_finder());
    }

    public LazyNetworkAccess(NetworkAttrImpl attr, ProxyNetworkFinder netFinder) {
        super(null);
        this.attr = attr;
        this.netFinder = netFinder;
    }

    public NetworkAccess getNetworkAccess() {
        if(super.getNetworkAccess() == null) {
            try {
                if (attr.get_code().charAt(0) == '_') {
                    //virtual network, must use retrieve_by_name
                    NetworkAccess[] nets = netFinder.retrieve_by_name(attr.get_code());
                    if (nets.length != 0) {
                        setNetworkAccess(nets[0]);
                    } else {
                        throw new NetworkNotFound();
                    }
                } else {
                    setNetworkAccess(netFinder.retrieve_by_id(attr.get_id()));
                }
            } catch(NetworkNotFound e) {
                throw new RuntimeException("unable to reconnect to networkaccess: "
                                                   + NetworkIdUtil.toString(attr),
                                           e);
            }
        }
        return super.getNetworkAccess();
    }

    ProxyNetworkFinder netFinder;
}
