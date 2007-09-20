package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
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
        Session session = getSession();
        internUnit(sta.my_location);
        try {
            int netDbid = put(sta.my_network);
            System.out.println("Put net: "+netDbid+"  "+((NetworkAttrImpl)sta.my_network).getDbid());
        } catch(ConstraintViolationException e) {
            // assume network is already put, attach net
            try {
            sta.my_network = getNetworkByCode(sta.my_network.get_id());
            } catch (NotFound ee) {
                // something bad happening to the database...
                throw new RuntimeException(e);
            }
        }
        Integer dbid = (Integer)session.save(sta);
        return dbid.intValue();
    }

    private NetworkAttr getNetworkByCode(NetworkId netId) throws NotFound {
        Session session = getSession();
        Query query = session.createQuery(getNetworkByCodeString);
        query.setString("netCode", netId.network_code);
        List result = query.list();
        if(NetworkIdUtil.isTemporary(netId)) {
            Iterator it = result.iterator();
            while(it.hasNext()) {
                NetworkAttr n = (NetworkAttr)it.next();
                if (NetworkIdUtil.areEqual(netId, n.get_id())) {
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
        Session session = getSession();
        Query query = session.createQuery(getStationByDbIdString);
        query.setInteger("dbid", dbid);
        List result = query.list();
        if(result.size() > 0) {
            StationImpl out = (StationImpl)result.get(0);
            return out;
        }
        throw new NotFound();
    }

    public Station[] getAllStations() {
        Session session = getSession();
        Query query = session.createQuery(getAllStationsString);
        List result = query.list();
        return (Station[])result.toArray(new Station[0]);
    }

    static String getStationByDbIdString = "From edu.iris.Fissures.network.StationImpl s WHERE dbid = :dbid";

    static String getAllStationsString = "From edu.iris.Fissures.network.StationImpl s";

    static String getNetworkByCodeString = "From edu.iris.Fissures.network.NetworkAttrImpl n WHERE network_code = :netCode";
}
