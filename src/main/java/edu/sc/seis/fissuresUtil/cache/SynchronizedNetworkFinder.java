package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class SynchronizedNetworkFinder extends ProxyNetworkFinder {

    public SynchronizedNetworkFinder(NetworkFinder nf) {
        super(nf);
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        synchronized(SynchronizedNetworkAccess.class) {
            return super.retrieve_by_id(id);
        }
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        synchronized(SynchronizedNetworkAccess.class) {
            return super.retrieve_by_code(code);
        }
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        synchronized(SynchronizedNetworkAccess.class) {
            return super.retrieve_by_name(name);
        }
    }

    public NetworkAccess[] retrieve_all() {
        synchronized(SynchronizedNetworkAccess.class) {
            return super.retrieve_all();
        }
    }
}
