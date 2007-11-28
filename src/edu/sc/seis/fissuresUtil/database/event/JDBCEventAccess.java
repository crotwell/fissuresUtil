package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.PointDistanceAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;

public class JDBCEventAccess extends EventTable {

    public JDBCEventAccess() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCEventAccess(Connection conn) throws SQLException {
        this(conn, new JDBCOrigin(conn), new JDBCEventAttr(conn));
    }

    public JDBCEventAccess(Connection conn,
                           JDBCOrigin origins,
                           JDBCEventAttr attrs) throws SQLException {
        super("eventaccess", conn);
        this.jdbcOrigin = origins;
        this.jdbcAttr = attrs;
        seq = new JDBCSequence(conn, "EventAccessSeq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/props/event/eventaccess.props");
    }

    public CacheEvent getEvent(int dbid) throws SQLException, NotFound {
        CacheEvent ev = (CacheEvent)idsToEvents.get(new Integer(dbid));
        if(ev != null) {
            touchEventId(dbid);
            ev.setDbid(dbid);
            return ev;
        }
        getAttrAndOrigin.setInt(1, dbid);
        ResultSet rs = getAttrAndOrigin.executeQuery();
        if(rs.next()) {
            return getEvent(rs, dbid);
        }
        throw new NotFound();
    }

    public CacheEvent[] getEvents(int[] dbIds) throws SQLException, NotFound {
        CacheEvent[] events = new CacheEvent[dbIds.length];
        for(int i = 0; i < dbIds.length; i++) {
            events[i] = getEvent(dbIds[i]);
        }
        return events;
    }

    private CacheEvent getEvent(ResultSet rs, int dbid) throws NotFound,
            SQLException {
        OriginImpl preferredOrigin = jdbcOrigin.get(rs.getInt("origin_id"));
        OriginImpl[] allOrigins = jdbcOrigin.getOrigins(dbid);
        EventAttr attr = jdbcAttr.get(rs.getInt("eventattr_id"));
        CacheEvent ev = new CacheEvent(attr, allOrigins, preferredOrigin);
        ev.setDbid(dbid);
        idsToEvents.put(new Integer(dbid), ev);
        touchEventId(dbid);
        ev.setDbid(dbid);
        return ev;
    }

    public CacheEvent[] getAllEvents() throws SQLException, SQLException {
        List events = getAllEventsList();
        return (CacheEvent[])events.toArray(new CacheEvent[events.size()]);
    }

    public List getAllEventsList() throws SQLException, SQLException {
        return extractEvents(getEventIds.executeQuery());
    }

    public List extractEvents(ResultSet rs) throws SQLException {
        List events = new ArrayList();
        while(rs.next()) {
            try {
                int id = rs.getInt("event_id");
                CacheEvent ev = (CacheEvent)idsToEvents.get(new Integer(id));
                touchEventId(id);
                if(ev != null) {
                    events.add(ev);
                } else {
                    events.add(getEvent(rs, id));
                }
            } catch(NotFound e) {
                throw new RuntimeException("This shouldn't happen.  I just got that id",
                                           e);
            }
        }
        return events;
    }

    /**
     * Method put adds this event to the database. If it's already in there, it
     * merely returns the dbid of the previously inserted events.
     * 
     * @param eao -
     *            an EventAccessOperations that must have a preferred origin
     * @return the dbid
     */
    public int put(EventAccessOperations eao,
                   String IOR,
                   String server,
                   String dns) throws SQLException {
        if(!(eao instanceof CacheEvent)) {
            eao = new CacheEvent(eao);
        }
        try {
            return getDBId(eao);
        } catch(NotFound e) {
            return insert(eao, IOR, server, dns);
        }
    }

    public int insert(EventAccessOperations eao,
                      String IOR,
                      String server,
                      String dns) throws SQLException {
        int id = seq.next();
        int attrId = jdbcAttr.put(eao.get_attributes());
        int originId;
        for(int i = 0; i < eao.get_origins().length; i++) {
            jdbcOrigin.put(eao.get_origins()[i], id);
        }
        try {
            originId = jdbcOrigin.getDBId(eao.get_preferred_origin());
        } catch(NotFound ex) {
            throw new RuntimeException("The preferred origin wasn't found right after all origins were inserted.  This shouldn't ever happen.  If you're seeing this, I imagine very bad things are happening to the database right now");
        } catch(NoPreferredOrigin ee) {
            throw new IllegalArgumentException("Events passed in must have preferred origins");
        }
        put.setInt(1, id);
        put.setString(2, IOR);
        put.setInt(3, originId);
        put.setInt(4, attrId);
        put.setString(5, server);
        put.setString(6, dns);
        put.executeUpdate();
        eventsToIds.put(eao, new Integer(id));
        idsToEvents.put(new Integer(id), eao);
        touchEventId(id);
        return id;
    }

    public boolean contains(EventAccessOperations eao) throws SQLException {
        try {
            getDBId(eao);
        } catch(NotFound e) {
            return false;
        }
        return true;
    }

    public int getDBId(EventAccessOperations eao) throws SQLException, NotFound {
        if(!(eao instanceof CacheEvent)) {
            eao = new CacheEvent(eao);
        }
        Integer id = (Integer)eventsToIds.get(eao);
        if(id != null) {
            touchEventId(id.intValue());
            return id.intValue();
        }
        getDBIdStmt.setInt(1, jdbcAttr.getDBId(eao.get_attributes()));
        try {
            getDBIdStmt.setInt(2,
                               jdbcOrigin.getDBId(eao.get_preferred_origin()));
        } catch(NoPreferredOrigin e) {
            throw new IllegalArgumentException("The event access passed into getDBId must have a preferred origin");
        }
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            int dbid = rs.getInt("event_id");
            eventsToIds.put(eao, new Integer(dbid));
            touchEventId(dbid);
            return dbid;
        }
        throw new NotFound("The event wasn't found in the db!");
    }

    /**
     * @returns the indicies of events matched by the query ignoring the
     *          catalog, contributor and type sections of the query
     */
    public int[] query(EventFinderQuery q) throws SQLException {
        return query(q, finderQueryAvoidDateline, finderQueryAroundDateline);
    }

    public static int[] query(EventFinderQuery q,
                              PreparedStatement avoidDatelineStatement,
                              PreparedStatement aroundDatelineStatement)
            throws SQLException {
        int index = 1;
        // TODO - if q.getArea is PointDistanceArea, run results through
        // AreaUtil.inDonut before returning
        BoxArea ba = AreaUtil.makeContainingBox(q.getArea());
        PreparedStatement finderQuery = (ba.min_longitude <= ba.max_longitude ? avoidDatelineStatement
                : aroundDatelineStatement);
        finderQuery.setFloat(index++, ba.min_latitude);
        finderQuery.setFloat(index++, ba.max_latitude);
        finderQuery.setFloat(index++, q.getMinMag());
        finderQuery.setFloat(index++, q.getMaxMag());
        MicroSecondTimeRange range = q.getTime();
        finderQuery.setTimestamp(index++, range.getBeginTime().getTimestamp());
        finderQuery.setTimestamp(index++, range.getEndTime().getTimestamp());
        finderQuery.setDouble(index++, q.getMinDepth());
        finderQuery.setDouble(index++, q.getMaxDepth());
        finderQuery.setFloat(index++, ba.min_longitude);
        finderQuery.setFloat(index++, ba.max_longitude);
        ResultSet rs = finderQuery.executeQuery();
        ArrayList out = new ArrayList();
        while(rs.next()) {
            out.add(new Integer(rs.getInt(1)));
        }
        Integer[] bigInt = (Integer[])out.toArray(new Integer[out.size()]);
        int[] littleInt = new int[bigInt.length];
        for(int i = 0; i < bigInt.length; i++) {
            littleInt[i] = bigInt[i].intValue();
        }
        return littleInt;
    }


    /*
     * gets events from the database that vary in position or time by small
     * amounts. results will include the original event, as well (or at least
     * one would hope).
     */
    public CacheEvent[] getSimilarEvents(CacheEvent event, TimeInterval timeTolerance, QuantityImpl positionTolerance)
            throws SQLException, NotFound {
        EventFinderQuery query = new EventFinderQuery();
        Origin origin = EventUtil.extractOrigin(event);
        // get query time range
        MicroSecondDate evTime = new MicroSecondDate(origin.origin_time);
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(evTime.subtract(timeTolerance),
                                                                  evTime.add(timeTolerance));
        // get query area
        Area area = new PointDistanceAreaImpl(origin.my_location.latitude,
                                              origin.my_location.longitude,
                                              new QuantityImpl(0.0,
                                                               UnitImpl.DEGREE),
                                              positionTolerance);
        // set query vars
        query.setTime(timeRange);
        query.setArea(area);
        query.setMinMag(JDBCEventAccess.INCONCEIVABLY_SMALL_MAGNITUDE);
        query.setMaxMag(JDBCEventAccess.INCONCEIVABLY_LARGE_MAGNITUDE);
        query.setMinDepth(JDBCEventAccess.INCONCEIVABLY_SMALL_DEPTH);
        query.setMaxDepth(JDBCEventAccess.INCONCEIVABLY_LARGE_DEPTH);
        // get event ids and turn them into actual events
        int[] eventIds = query(query);
        CacheEvent[] events = getEvents(eventIds);
        return events;
    }
    
    public int[] getByName(String name) throws SQLException, NotFound {
        getByNameStmt.setString(1, name);
        ResultSet rs = getByNameStmt.executeQuery();
        ArrayList out = new ArrayList();
        while(rs.next()) {
            out.add(new Integer(rs.getInt(1)));
        }
        if(out.size() == 0) {
            throw new NotFound("No events by name " + name);
        }
        Integer[] bigInt = (Integer[])out.toArray(new Integer[out.size()]);
        int[] littleInt = new int[bigInt.length];
        for(int i = 0; i < bigInt.length; i++) {
            littleInt[i] = bigInt[i].intValue();
        }
        return littleInt;
    }

    public JDBCEventAttr getAttributeTable() {
        return jdbcAttr;
    }

    private JDBCOrigin jdbcOrigin;

    private JDBCEventAttr jdbcAttr;

    private JDBCSequence seq;

    public static void touchEventId(int dbid) {
        synchronized(eventAccessList) {
            Integer id = new Integer(dbid);
            if(eventAccessList.contains(id)) {
                eventAccessList.remove(id);
            }
            eventAccessList.add(0, id);
            if(eventAccessList.size() > MAX_EVENTS) {
                Integer dbIdToRemove = (Integer)eventAccessList.remove(eventAccessList.size() - 1);
                EventAccessOperations eao = (EventAccessOperations)idsToEvents.remove(dbIdToRemove);
                eventsToIds.remove(eao);
            }
        }
    }

    public static void emptyCache() {
        idsToEvents.clear();
        eventsToIds.clear();
        eventAccessList.clear();
    }

    private static Map idsToEvents = Collections.synchronizedMap(new HashMap());

    private static Map eventsToIds = Collections.synchronizedMap(new HashMap());

    private static List eventAccessList = Collections.synchronizedList(new ArrayList());

    private static int MAX_EVENTS = 5000;

    private static final Logger logger = Logger.getLogger(JDBCEventAccess.class);

    public static final float INCONCEIVABLY_SMALL_MAGNITUDE = -99.0f;
    public static final float INCONCEIVABLY_LARGE_MAGNITUDE = 12.0f;

    public static final float INCONCEIVABLY_SMALL_DEPTH = -99.0f;
    public static final float INCONCEIVABLY_LARGE_DEPTH = 7000.0f;
    
    private PreparedStatement put, getDBIdStmt, getAttrAndOrigin, getEventIds,
            finderQueryAvoidDateline, finderQueryAroundDateline, getByNameStmt,
            getLast;

    public JDBCEventAttr getJDBCAttr() {
        return jdbcAttr;
    }

    public JDBCOrigin getJDBCOrigin() {
        return jdbcOrigin;
    }

    public CacheEvent getLastEvent() throws SQLException, NotFound {
        ResultSet rs = getLast.executeQuery();
        if(rs.next()) {
            return getEvent(rs.getInt("event_id"));
        }
        throw new NotFound("No events!");
    }
}