package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCEventAccess extends EventTable {

    public JDBCEventAccess() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCEventAccess(Connection conn) throws SQLException {
        this(conn, new JDBCOrigin(conn), new JDBCEventAttr(conn));
    }

    public JDBCEventAccess(Connection conn, JDBCOrigin origins,
            JDBCEventAttr attrs) throws SQLException {
        super("eventaccess", conn);
        this.origins = origins;
        this.attrs = attrs;
        seq = new JDBCSequence(conn, "EventAccessSeq");
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("eventaccess", conn)) {
            stmt.executeUpdate(ConnMgr.getSQL("EventAccess.create"));
        }
        put = conn.prepareStatement(" INSERT INTO eventaccess "
                + "(event_id, IOR, origin_id, " + "eventattr_id, server, dns)"
                + "VALUES(?,?,?,?,?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT event_id FROM eventaccess "
                + " WHERE eventattr_id = ? AND" + " origin_id = ?");
        getCorbaStrings = conn.prepareStatement(" SELECT IOR, server, dns FROM eventaccess "
                + " WHERE event_id = ?");
        getAttrAndOrigin = conn.prepareStatement(" SELECT eventattr_id, origin_id FROM eventaccess "
                + " WHERE event_id = ?");
        getEventIds = conn.prepareStatement(" SELECT eventattr_id, event_id, origin_id FROM eventaccess");
        //        queryStmt = conn.prepareStatement("SELECT DISTINCT event_id FROM
        // eventaccess,
        // origin, location WHERE "+
        //                                          "eventaccess.event_id = origin.origin_event_id AND "+
        //                                          "origin_location_id = location.loc_id AND "+
        //                                          "origin.origin_id = magnitude.originid AND "+
        //                                          "origin.origin_time_id = time.time_id AND "+
        //                                          "location.loc_lat >= ? AND location.loc_lat <= ? AND "+
        //                                          "location.loc_lon >= ? AND location.loc_lon <= ? AND "+
        //                                          "magnitude.magnitudevalue >= ? AND magnitude.magnitudevalue <= ? AND
        // "+
        //                                          "time.time_stamp >= ? AND time.time_stamp <= ? AND "+
        //                                          "");
        //        getByNameStmt = conn.prepareStatement("SELECT DISTINCT event_id FROM
        // eventaccess, eventattr WHERE "+
        //                                              "eventattr.name = ? AND "+
        //                                              "eventaccess.eventattr_id = eventattr.eventattr_id");
    }

    public CacheEvent getEvent(int dbid) throws SQLException, NotFound {
        CacheEvent ev = (CacheEvent)idsToEvents.get(new Integer(dbid));
        if(ev != null) { return ev; }
        getAttrAndOrigin.setInt(1, dbid);
        ResultSet rs = getAttrAndOrigin.executeQuery();
        rs.next();
        return getEvent(rs, dbid);
    }

    private CacheEvent getEvent(ResultSet rs, int dbid) throws NotFound,
            SQLException {
        Origin preferredOrigin = origins.get(rs.getInt("origin_id"));
        Origin[] allOrigins = origins.getOrigins(dbid);
        EventAttr attr = attrs.get(rs.getInt("eventattr_id"));
        CacheEvent ev = new CacheEvent(attr, allOrigins, preferredOrigin);
        idsToEvents.put(new Integer(dbid), ev);
        return ev;
    }

    public CacheEvent[] getAllEvents() throws SQLException, SQLException {
        List events = new ArrayList();
        ResultSet rs = getEventIds.executeQuery();
        while(rs.next()) {
            try {
                int id = rs.getInt("event_id");
                CacheEvent ev = (CacheEvent)idsToEvents.get(new Integer(id));
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
        return (CacheEvent[])events.toArray(new CacheEvent[events.size()]);
    }

    public String getServer(int dbid) throws SQLException {
        return getCorbaStrings(dbid)[0];
    }

    public String getDNS(int dbid) throws SQLException {
        return getCorbaStrings(dbid)[1];
    }

    public String getIOR(int dbid) throws SQLException {
        return getCorbaStrings(dbid)[2];
    }

    /**
     * Method getCorbaStrings returns all the server information about this
     * event db id
     * 
     * @return a String[] of length three with the Server in position 0, DNS in
     *         position 1 and the IOR in position 2
     */
    public String[] getCorbaStrings(int dbid) throws SQLException {
        String[] serverInfo = new String[3];
        getCorbaStrings.setInt(1, dbid);
        ResultSet rs = getCorbaStrings.executeQuery();
        rs.next();
        serverInfo[0] = rs.getString("server");
        serverInfo[1] = rs.getString("dns");
        serverInfo[2] = rs.getString("IOR");
        return serverInfo;
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
            int id = seq.next();
            int attrId = attrs.put(eao.get_attributes());
            int originId;
            for(int i = 0; i < eao.get_origins().length; i++) {
                origins.put(eao.get_origins()[i], id);
            }
            try {
                originId = origins.getDBId(eao.get_preferred_origin());
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
            return id;
        }
    }

    public int getDBId(EventAccessOperations eao) throws SQLException, NotFound {
        if(!(eao instanceof CacheEvent)) {
            eao = new CacheEvent(eao);
        }
        Integer id = (Integer)eventsToIds.get(eao);
        if(id != null) { return id.intValue(); }
        getDBIdStmt.setInt(1, attrs.getDBId(eao.get_attributes()));
        try {
            getDBIdStmt.setInt(2, origins.getDBId(eao.get_preferred_origin()));
        } catch(NoPreferredOrigin e) {
            throw new IllegalArgumentException("The event access passed into getDBId must have a preferred origin");
        }
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            int dbid = rs.getInt("event_id");
            eventsToIds.put(eao, new Integer(dbid));
            return dbid;
        }
        throw new NotFound("The event wasn't found in the db!");
    }

    public int[] query(Quantity min_depth,
                       Quantity max_depth,
                       float minLat,
                       float maxLat,
                       float minLon,
                       float maxLon,
                       Time start_time,
                       Time end_time,
                       float min_magnitude,
                       float max_magnitude,
                       String[] search_types,
                       String[] catalogs,
                       String[] contributors) throws SQLException {
        int index = 1;
        queryStmt.setFloat(index++, minLat);
        queryStmt.setFloat(index++, maxLat);
        queryStmt.setFloat(index++, minLon);
        queryStmt.setFloat(index++, maxLon);
        queryStmt.setFloat(index++, min_magnitude);
        queryStmt.setFloat(index++, max_magnitude);
        queryStmt.setTimestamp(index++,
                               new MicroSecondDate(start_time).getTimestamp());
        queryStmt.setTimestamp(index++,
                               new MicroSecondDate(end_time).getTimestamp());
        ResultSet rs = queryStmt.executeQuery();
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

    public int[] getByName(String name) throws SQLException, NotFound {
        getByNameStmt.setString(1, name);
        ResultSet rs = getByNameStmt.executeQuery();
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

    public JDBCEventAttr getAttributeTable() {
        return attrs;
    }

    private JDBCOrigin origins;

    private JDBCEventAttr attrs;

    private JDBCSequence seq;

    public static void emptyCache() {
        idsToEvents.clear();
        eventsToIds.clear();
    }

    private static Map idsToEvents = Collections.synchronizedMap(new HashMap());

    private static Map eventsToIds = Collections.synchronizedMap(new HashMap());

    private PreparedStatement put, getDBIdStmt, getCorbaStrings,
            getAttrAndOrigin, getEventIds, queryStmt, getByNameStmt;

    public void updateFlinnEngdahlRegion(int eventid, FlinnEngdahlRegion region)
            throws NotFound, SQLException {
    // TODO Auto-generated method stub
    }
}