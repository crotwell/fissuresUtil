package edu.sc.seis.fissuresUtil.database.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCEventAccess extends EventTable{
    public JDBCEventAccess(Connection conn) throws SQLException{
        this(conn, new JDBCOrigin(conn), new JDBCEventAttr(conn));
    }

    public JDBCEventAccess(Connection conn, JDBCOrigin origins,
                           JDBCEventAttr attrs) throws SQLException{
        super("eventaccess", conn);
        this.origins = origins;
        this.attrs = attrs;
        seq = new JDBCSequence(conn, "EventAccessSeq");
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("eventaccess", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("EventAccess.create"));
        }
        put = conn.prepareStatement(" INSERT INTO eventaccess "+
                                        "(event_id, IOR, origin_id, " +
                                        "eventattr_id, server, dns)"+
                                        "VALUES(?,?,?,?,?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT event_id FROM eventaccess "+
                                                " WHERE eventattr_id = ? AND"+
                                                " origin_id = ?");
        getCorbaStrings = conn.prepareStatement(" SELECT IOR, server, dns FROM eventaccess " +
                                                    " WHERE event_id = ?");
        getAttrAndOrigin = conn.prepareStatement(" SELECT eventattr_id, origin_id FROM eventaccess " +
                                                     " WHERE event_id = ?");
        getEventIds = conn.prepareStatement(" SELECT eventattr_id, event_id, origin_id FROM eventaccess");
    }

    public CacheEvent getEvent(int dbid) throws SQLException, NotFound{
        getAttrAndOrigin.setInt(1, dbid);
        ResultSet rs = getAttrAndOrigin.executeQuery();
        rs.next();
        return getEvent(rs, dbid);
    }

    private CacheEvent getEvent(ResultSet rs, int dbid) throws NotFound, SQLException{
        Origin preferredOrigin = origins.get(rs.getInt("origin_id"));
        Origin[] allOrigins = origins.getOrigins(dbid);
        EventAttr attr = attrs.get(rs.getInt("eventattr_id"));
        return new CacheEvent(attr, allOrigins, preferredOrigin);
    }

    public CacheEvent[] getAllEvents() throws SQLException,SQLException{
        List events = new ArrayList();
        ResultSet rs = getEventIds.executeQuery();
        while(rs.next()){
            try {
                events.add(getEvent(rs, rs.getInt("event_id")));
            } catch (NotFound e) {
                throw new RuntimeException("This shouldn't happen.  I just got that id", e);
            }
        }
        return (CacheEvent[])events.toArray(new CacheEvent[events.size()]);
    }

    public String getServer(int dbid) throws SQLException{
        return getCorbaStrings(dbid)[0];
    }

    public String getDNS(int dbid) throws SQLException{
        return getCorbaStrings(dbid)[1];
    }

    public String getIOR(int dbid) throws SQLException{
        return getCorbaStrings(dbid)[2];
    }

    /**
     * Method getCorbaStrings returns all the server information about this
     * event db id
     * @return   a String[] of length three with the Server in position 0, DNS
     * in position 1 and the IOR in position 2
     */
    public String[] getCorbaStrings(int dbid) throws SQLException{
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
     * Method put adds this event to the database.  If it's already in there,
     * it merely returns the dbid of the previously inserted events.
     * @param    eao - an EventAccessOperations that must have a preferred origin
     * @return   the dbid
     */
    public int put(EventAccessOperations eao, String IOR,
                   String server, String dns) throws SQLException{
        try {
            return getDBId(eao);
        } catch (NotFound e) {
            int id = seq.next();
            int attrId = attrs.put(eao.get_attributes());
            int originId;
            for (int i = 0; i < eao.get_origins().length; i++) {
                origins.put(eao.get_origins()[i], id);
            }
            try {
                originId = origins.getDBId(eao.get_preferred_origin());
            } catch (NotFound ex) {
                throw new RuntimeException("The preferred origin wasn't found right after all origins were inserted.  This shouldn't ever happen.  If you're seeing this, I imagine very bad things are happening to the database right now");
            }catch (NoPreferredOrigin ee) {
                throw new IllegalArgumentException("Events passed in must have preferred origins");
            }
            put.setInt(1, id);
            put.setString(2, IOR);
            put.setInt(3, originId);
            put.setInt(4, attrId);
            put.setString(5, server);
            put.setString(6, dns);
            put.executeUpdate();
            return id;
        }
    }

    public int getDBId(EventAccessOperations eao) throws SQLException, NotFound{
        getDBIdStmt.setInt(1, attrs.getDBId(eao.get_attributes()));
        try {
            getDBIdStmt.setInt(2, origins.getDBId(eao.get_preferred_origin()));
        } catch (NoPreferredOrigin e) {
            throw new IllegalArgumentException("The event access passed into getDBId must have a preferred origin");
        }
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next())return rs.getInt("event_id");
        throw new NotFound("The event wasn't found in the db!");
    }

    private JDBCOrigin origins;

    private JDBCEventAttr attrs;

    private JDBCSequence seq;

    private PreparedStatement put, getDBIdStmt, getCorbaStrings, getAttrAndOrigin,
        getEventIds;
}
