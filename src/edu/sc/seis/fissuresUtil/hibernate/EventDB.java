package edu.sc.seis.fissuresUtil.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;

public class EventDB extends AbstractHibernateDB {
    
    public EventDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public EventDB(SessionFactory factory) {
        super(factory);
    }

    public CacheEvent[] query(EventFinderQuery q) {
        BoxArea ba = AreaUtil.makeContainingBox(q.getArea());
        String queryString = (ba.min_longitude <= ba.max_longitude ? finderQueryAvoidDateline
                : finderQueryAroundDateline);
        Session session = getSession();
        Query query = session.createQuery(queryString);
        query.setFloat("minLat", ba.min_latitude);
        query.setFloat("maxLat", ba.max_latitude);
        query.setFloat("minMag", q.getMinMag());
        query.setFloat("maxMag", q.getMaxMag());
        query.setTimestamp("minTime", q.getTime().getBeginTime().getTimestamp());
        query.setTimestamp("maxTime", q.getTime().getEndTime().getTimestamp());
        query.setDouble("minDepth", q.getMinDepth());
        query.setDouble("maxDepth", q.getMaxDepth());
        query.setFloat("minLon", ba.min_longitude);
        query.setFloat("maxLon", ba.max_longitude);
        List result = query.list();
        CacheEvent[] out = (CacheEvent[])result.toArray(new CacheEvent[0]);
        return out;
    }

    public CacheEvent getEvent(int dbid) throws NotFound {
        Session session = getSession();
        Query query = session.createQuery(getByDbIdString);
        query.setInteger("id", dbid);
        List result = query.list();
        if(result.size() > 0) {
            CacheEvent out = (CacheEvent)result.get(0);
            return out;
        }
        throw new NotFound();
    }

    public long put(CacheEvent event) {
        Session session = getSession();
        internUnit(event.getOrigin().my_location);
        Integer dbid = (Integer)session.save(event);
        event.setDbId(dbid.intValue());
        return dbid.longValue();
    }

    public CacheEvent getLastEvent() throws NotFound {
        Session session = getSession();
        Query query = session.createQuery(getLastEventString);
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() > 0) {
            CacheEvent out = (CacheEvent)result.get(0);
            return out;
        }
        throw new NotFound();
    }
    
    static String getByDbIdString = "From edu.sc.seis.fissuresUtil.cache.CacheEvent e WHERE id = :id";
    
    static String getLastEventString = "From edu.sc.seis.fissuresUtil.cache.CacheEvent e ORDER BY e.id desc";

    static String finderQueryBase = "select e FROM edu.sc.seis.fissuresUtil.cache.CacheEvent e join e.preferred.magnitudes m "
            + "WHERE e.preferred.my_location.latitude between :minLat AND :maxLat "
            + "AND m member of e.preferred.magnitudes AND m.value between :minMag AND :maxMag  "
            + "AND e.preferred.origin_time.time between :minTime AND :maxTime  "
            + "AND e.preferred.my_location.depth.value between :minDepth and :maxDepth  ";

    static String finderQueryAvoidDateline = finderQueryBase
            + "AND e.preferred.my_location.longitude between :minLon and :maxLon ";

    static String finderQueryAroundDateline = finderQueryBase
            + " AND ((? <= e.preferred.my_location.longitude) OR (e.preferred.my_location.longitude <= ?))";
}
