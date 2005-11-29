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
        return nf.retrieve_by_id(id);
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return nf.retrieve_by_code(code);
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return nf.retrieve_by_name(name);
    }

    public NetworkAccess[] retrieve_all() {
        return nf.retrieve_all();
    }
}