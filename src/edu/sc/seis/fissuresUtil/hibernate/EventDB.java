package edu.sc.seis.fissuresUtil.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;

public class EventDB extends AbstractHibernateDB {

	public EventDB() {
		initQueryStrings();
	}

	protected void initQueryStrings() {
		getLastEventString = "From " + getEventClass().getName()
				+ " e ORDER BY e.id desc";
		finderQueryBase = "select e FROM "
				+ getEventClass().getName()
				+ " e join e.preferred.magnitudeList m "
				+ "WHERE e.preferred.location.latitude between :minLat AND :maxLat "
				+ "AND m.value between :minMag AND :maxMag  "
				+ "AND e.preferred.originTime.time between :minTime AND :maxTime  "
				+ "AND e.preferred.location.depth.value between :minDepth and :maxDepth  ";
		finderQueryAvoidDateline = finderQueryBase
				+ "AND e.preferred.location.longitude between :minLon and :maxLon ";
		finderQueryAroundDateline = finderQueryBase
				+ " AND ((? <= e.preferred.location.longitude) OR (e.preferred.location.longitude <= ?))";
		getIdenticalEventString = "From " + getEventClass().getName()
				+ " e WHERE " + "e.preferred.originTime.time = :originTime "
				+ "AND e.preferred.location.latitude = :lat "
				+ "AND e.preferred.location.latitude = :lon "
				+ "AND e.preferred.location.depth.value = :depth";
		eventByTimeAndDepth = "From " + getEventClass().getName()
        + " e WHERE " + "e.preferred.origin_time.time between :minTime and :maxTime"
        + "AND e.preferred.my_location.depth.value between :minDepth and :maxDepth";
	}
	
	public List getAll() {
	    return getSession().createQuery("from "+getEventClass().getName()).list();
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
		CacheEvent[] out = (CacheEvent[]) result.toArray(new CacheEvent[0]);
		return out;
	}

	public CacheEvent getEvent(int dbid) throws NotFound {
		Session session = getSession();
		CacheEvent out = (CacheEvent) session.get(getEventClass(), new Integer(
				dbid));
		if (out == null) {
			throw new NotFound();
		}
		return out;
	}

	public long put(CacheEvent event) {
		Session session = getSession();
		internUnit(event.getOrigin().my_location);
		Origin[] origins = event.get_origins();
		for(int i = 0; i < origins.length; i++) {
            internUnit(origins[i].my_location);
        }
		Integer dbid = (Integer) session.save(event);
		//event.setDbId(dbid.intValue());
		return dbid.longValue();
	}

	public CacheEvent getLastEvent() throws NotFound {
		Session session = getSession();
		Query query = session.createQuery(getLastEventString);
		query.setMaxResults(1);
		List result = query.list();
		if (result.size() > 0) {
			CacheEvent out = (CacheEvent) result.get(0);
			return out;
		}
		throw new NotFound();
	}

	public CacheEvent getIdenticalEvent(CacheEvent e) {
		Session session = getSession();
		Query query = session.createQuery(getIdenticalEventString);
		query.setMaxResults(1);
		try {
			query.setTimestamp("originTime", new MicroSecondDate(e
					.get_preferred_origin().origin_time).getTimestamp());
			query.setDouble("depth",
					e.get_preferred_origin().my_location.depth.value);
			query.setDouble("lat",
					e.get_preferred_origin().my_location.latitude);
			query.setDouble("lon",
					e.get_preferred_origin().my_location.longitude);
			List result = query.list();
			if (result.size() > 0) {
				CacheEvent out = (CacheEvent) result.get(0);
				return out;
			}
		} catch (NoPreferredOrigin npo) {

		}
		return null;
	}
	
	private static EventDB singleton;

    public CacheEvent[] getEventsByTimeAndDepthRanges(MicroSecondDate minTime,
                                                      MicroSecondDate maxTime,
                                                      double minDepth,
                                                      double maxDepth) {
        Session session = getSession();
        Query query = session.createQuery(eventByTimeAndDepth);
        query.setTimestamp("minTime", minTime.getTimestamp());
        query.setTimestamp("maxTime", maxTime.getTimestamp());
        query.setDouble("minDepth", minDepth);
        query.setDouble("maxDepth", maxDepth);
        List result = query.list();
        CacheEvent[] out = (CacheEvent[]) result.toArray(new CacheEvent[0]);
        return out;
    }
    
	public static EventDB getSingleton() {
	    if (singleton == null) {
	        singleton = new EventDB();
	    }
	    return singleton;
	}
	
	/**
	 * override to use queries on subclasses of CacheEvent. For example SOD uses
	 * StatefulEvent.
	 */
	protected Class getEventClass() {
		return CacheEvent.class;
	}

	protected String getLastEventString;

	protected String finderQueryBase;

	protected String finderQueryAvoidDateline;

	protected String finderQueryAroundDateline;

	protected String getIdenticalEventString;
	
	protected String eventByTimeAndDepth;
}
