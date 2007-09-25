package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class NetworkDB extends AbstractHibernateDB {

    public NetworkDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public NetworkDB(SessionFactory factory) {
        super(factory);
    }

    public int put(NetworkAttr net) {
        Session session = getSession();
        Integer dbid = (Integer)session.save(net);
        return dbid.intValue();
    }

    public int put(Station sta) {
        Integer dbid;
        // assume network is already put, attach net
        try {
            sta.my_network = getNetworkById(sta.my_network.get_id());
        } catch(NotFound ee) {
            // must not have been added yet
            put(sta.my_network);
        }
        internUnit(sta.my_location);
        dbid = (Integer)getSession().save(sta);
        return dbid.intValue();
    }

    public int put(ChannelImpl chan) {
        Integer dbid;
        internUnit(chan.my_site.my_location);
        try {
            chan.my_site.my_station = getStationById(chan.my_site.my_station.get_id());
        } catch(NotFound e) {
            int staDbid = put(chan.my_site.my_station);
        }
        dbid = (Integer)getSession().save(chan);
        return dbid.intValue();
    }

    private StationImpl getStationById(StationId staId) throws NotFound {
        Query query = getSession().createQuery(getStationByIdString);
        query.setString("netCode", staId.network_id.network_code);
        query.setString("staCode", staId.station_code);
        query.setTimestamp("staBegin", new MicroSecondDate(staId.begin_time).getTimestamp());
        List result = query.list();
        if(result.size() > 0) {
            StationImpl out = (StationImpl)result.get(0);
            return out;
        }
        throw new NotFound();
    }

    private NetworkAttr getNetworkById(NetworkId netId) throws NotFound {
        Query query = getSession().createQuery(getNetworkByCodeString);
        query.setString("netCode", netId.network_code);
        List result = query.list();
        if(NetworkIdUtil.isTemporary(netId)) {
            Iterator it = result.iterator();
            while(it.hasNext()) {
                NetworkAttr n = (NetworkAttr)it.next();
                if(NetworkIdUtil.areEqual(netId, n.get_id())) {
                    return n;
                }
            }
            throw new NotFound();
        } else {
            if(result.size() > 0) {
                NetworkAttr out = (NetworkAttr)result.get(0);
                return out;
            }
            throw new NotFound();
        }
    }

    public Station getStation(int dbid) throws NotFound {
        Query query = getSession().createQuery(getStationByDbIdString);
        query.setInteger("dbid", dbid);
        List result = query.list();
        if(result.size() > 0) {
            StationImpl out = (StationImpl)result.get(0);
            return out;
        }
        throw new NotFound();
    }

    public Station[] getAllStations() {
        Query query = getSession().createQuery(getAllStationsString);
        List result = query.list();
        return (Station[])result.toArray(new Station[0]);
    }

    static String getStationByIdString = "SELECT s From edu.iris.Fissures.network.StationImpl s WHERE s.networkAttr.id.network_code = :netCode AND s.id.station_code = :staCode AND sta_begin_time = :staBegin";

    static String getStationByDbIdString = "From edu.iris.Fissures.network.StationImpl s WHERE dbid = :dbid";

    static String getAllStationsString = "From edu.iris.Fissures.network.StationImpl s";

    static String getNetworkByCodeString = "From edu.iris.Fissures.network.NetworkAttrImpl n WHERE network_code = :netCode";
}
