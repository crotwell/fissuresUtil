package edu.sc.seis.fissuresUtil.cache;

import java.util.HashMap;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkIdUtil;

public class CacheByIdNetworkFinder extends ProxyNetworkFinder {

    public CacheByIdNetworkFinder(NetworkFinder nf) {
        super(nf);
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        String key = NetworkIdUtil.toString(id);
        if(!netIdToAccessMap.containsKey(key)) {
            logger.debug("Not in cache, go and get it over the net: "+key);
            netIdToAccessMap.put(key, super.retrieve_by_id(id));
        } else {
            logger.debug("Is in cache: "+key);
        }
        return netIdToAccessMap.get(key);
    }

    @Override
    public void reset() {
        super.reset();
        netIdToAccessMap.clear();
    }

    private HashMap<String, NetworkAccess> netIdToAccessMap = new HashMap<String, NetworkAccess>();
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CacheByIdNetworkFinder.class);
}
