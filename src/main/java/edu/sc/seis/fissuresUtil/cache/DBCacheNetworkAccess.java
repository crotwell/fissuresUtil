package edu.sc.seis.fissuresUtil.cache;

import java.util.HashMap;
import java.util.List;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;


/** A Cache NetworkAccess that also looks at the database for retrieval. */
public class DBCacheNetworkAccess extends CacheNetworkAccess {

    public DBCacheNetworkAccess(NetworkAttrImpl attr, FissuresNamingService fisName) {
        super(new LazyNetworkAccess(attr, getNetDC(attr, fisName)), attr);
    }
    public DBCacheNetworkAccess(NetworkAccess netAccess, NetworkAttrImpl attr) {
        super(netAccess, attr);
    }

    @Override
    public Channel[] retrieve_for_station(StationId id) {
        NetworkDB netdb = NetworkDB.getSingleton();
        try {
            List<ChannelImpl> chans = netdb.getChannelsForStation(netdb.getStationById(id));
            if (chans.size() != 0) {
                return chans.toArray(new ChannelImpl[0]);
            } else {
                return getNetworkAccess().retrieve_for_station(id);
            }
        } catch(NotFound e) {
            return new ChannelImpl[0];
        }
    }

    @Override
    public Station[] retrieve_stations() {
        NetworkDB netdb = NetworkDB.getSingleton();
        List<StationImpl> staList = netdb.getStationForNet((NetworkAttrImpl)get_attributes());
        if (staList.size() != 0) {
            return staList.toArray(new StationImpl[0]);
        } else {
            return getNetworkAccess().retrieve_stations();
        }
    }

    public static VestingNetworkDC getNetDC(NetworkAttrImpl attr,
                                            FissuresNamingService fisName) {
        String key = FissuresNamingService.piecesToNameString(attr.getSourceServerDNS(),
                                                FissuresNamingService.NETWORKDC,
                                                attr.getSourceServerName());
        if(!dbvnFinderCache.containsKey(key)) {
            VestingNetworkDC netdc = new VestingNetworkDC(attr.getSourceServerDNS(),
                                                          attr.getSourceServerName(),
                                                          fisName);
            dbvnFinderCache.put(key, netdc);
        }
        return dbvnFinderCache.get(key);
    }
    
    protected static HashMap<String, VestingNetworkDC> dbvnFinderCache = new HashMap<String, VestingNetworkDC>();

}