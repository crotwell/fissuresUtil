package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class NetworkDB extends AbstractHibernateDB {

    public NetworkDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public NetworkDB(SessionFactory factory) {
        super(factory);
    }

    public long put(Station sta) {
        Session session = getSession();
        internUnit(sta.my_location);
        Integer dbid = (Integer)session.save(sta);
        return dbid.longValue();
    }

    public Station getStation(int dbid) throws NotFound {
        Session session = getSession();
        Query query = session.createQuery(getStationByDbIdString);
        query.setInteger("dbid", dbid);
        List result = query.list();
        if(result.size() > 0) {
            StationImpl out = (StationImpl)result.get(0);
            session.close();
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
}
