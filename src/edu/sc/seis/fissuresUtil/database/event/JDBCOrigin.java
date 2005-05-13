package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.JDBCParameterRef;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCOrigin extends EventTable {

    public JDBCOrigin(Connection conn) throws SQLException {
        this(conn,
             new JDBCLocation(conn),
             new JDBCEventAttr(conn),
             new JDBCParameterRef(conn),
             new JDBCMagnitude(conn),
             new JDBCCatalog(conn),
             new JDBCTime(conn));
    }

    /**
     * Constructor for the JDBCOrigin class. In this Constructor the following
     * tables are created 1. Origin 2. OriginParameterReference
     */
    public JDBCOrigin(Connection conn, JDBCLocation jdbcLocation,
            JDBCEventAttr jdbcEventAttr, JDBCParameterRef jdbcParameterRef,
            JDBCMagnitude jdbcMagnitude, JDBCCatalog jdbcCatalog, JDBCTime time)
            throws SQLException {
        super("origin", conn);
        this.jdbcLocation = jdbcLocation;
        this.jdbcParamRef = jdbcParameterRef;
        this.jdbcMagnitude = jdbcMagnitude;
        this.jdbcCatalog = jdbcCatalog;
        this.timeTable = time;
        String parameterSubTableName = "originparamref";
        this.jdbcEventAttr = jdbcEventAttr;
        seq = new JDBCSequence(conn, "OriginSeq");
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("origin", conn)) {
            stmt.executeUpdate(ConnMgr.getSQL("origin.create"));
        }
        if(!DBUtil.tableExists("originparamref", conn)) {
            stmt.executeUpdate(ConnMgr.getSQL("originparamref.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO origin " + "(origin_id, "
                + "origin_catalog_id, " + "origin_time_id, "
                + "origin_location_id, " + "origin_text_id) "
                + "VALUES(?,?,?,?,?)");
        putOriginParamRefStmt = conn.prepareStatement(" INSERT INTO "
                + parameterSubTableName + "(originparamrefid,"
                + " originparameterid )" + " VALUES(?,?)");
        deleteOriginParamRefStmt = conn.prepareStatement(" DELETE FROM "
                + parameterSubTableName + " WHERE originparamrefid = ?");
        getDBIdStmt = conn.prepareStatement(" SELECT origin_id FROM origin "
                + " WHERE origin_catalog_id = ? AND"
                + " origin_time_id = ? AND" + " origin_location_id = ? AND"
                + " origin_text_id = ?");
        getStmt = conn.prepareStatement(" SELECT * FROM origin WHERE origin_id = ?");
        getParamsStmt = conn.prepareStatement(" SELECT parametera_id, parametercreator FROM "
                + jdbcParamRef.getTableName()
                + ","
                + parameterSubTableName
                + ","
                + tableName
                + " WHERE "
                + " parameterref.parameterid = "
                + " originparamref.originparameterid"
                + " AND "
                + " originparamrefid = " + " origin_id AND " + " origin_id = ?");
        updateEventIdStmt = conn.prepareStatement(" UPDATE " + tableName
                + " SET origin_event_id = ? " + " WHERE origin_id = ?");
        deleteOriginStmt = conn.prepareStatement(" DELETE FROM " + tableName
                + " WHERE origin_id = ?");
        getAllStmt = conn.prepareStatement(" SELECT origin_id FROM origin "
                + "  WHERE origin_event_id = ?");
    }

    /**
     * inserts an origin object into the database and returns the correspoding
     * dbid
     * 
     * @param origin -
     *            Origin
     * @return int - dbid
     */
    public int put(Origin origin) throws SQLException {
        try {
            return getDBId(origin);
        } catch(NotFound ex) {
            return insertOrigin(origin);
        }
    }

    private int insertOrigin(Origin o) throws SQLException {
        int id = seq.next();
        putStmt.setInt(1, id);
        insert(o, putStmt, 2);
        putStmt.executeUpdate();
        putParamRefs(o.parm_ids, id);
        putMagnitudes(o.magnitudes, id);
        return id;
    }

    /**
     * This method is mainly used in updating the origineventid of the origin
     * class given the Origin. If the Origin is not present already it is
     * inserted.
     */
    public int put(Origin origin, int eventId) throws SQLException {
        int id = put(origin);
        setEventId(id, eventId);
        return id;
    }

    private void setEventId(int originId, int eventId) throws SQLException {
        updateEventIdStmt.setInt(1, eventId);
        updateEventIdStmt.setInt(2, originId);
        updateEventIdStmt.executeUpdate();
    }

    /**
     * This method inserts the parameterRef into the parameterRef Table.
     * 
     * @param params -
     *            Array of ParameterRef
     * @id - origin dbid
     */
    public void putParamRefs(ParameterRef[] params, int id) throws SQLException {
        for(int i = 0; i < params.length; i++) {
            putParamRef(id, jdbcParamRef.put(params[i]));
        }
    }

    /**
     * This method inserts a row into originparameterreference table
     * 
     * @param id -
     *            origin dbid
     * @paramId - the parameterid obtained after inserting one of the params
     *          into the ParameterRefTable
     */
    public void putParamRef(int id, int paramId) throws SQLException {
        putOriginParamRefStmt.setInt(1, id);
        putOriginParamRefStmt.setInt(2, paramId);
        putOriginParamRefStmt.executeUpdate();
    }

    /**
     * This method deletes all the rows int originparameterreference for which
     * originparameterreferenceid = originid
     * 
     * @param originid -
     *            the dbid of the origin
     */
    public void deleteParamRef(int originid) throws SQLException {
        deleteOriginParamRefStmt.setInt(1, originid);
        deleteOriginParamRefStmt.executeUpdate();
    }

    public void putMagnitudes(Magnitude[] mags, int id) throws SQLException {
        jdbcMagnitude.put(mags, id);
    }

    /**
     * This method returns the dbid given the origin object
     * 
     * @param origin -
     *            Origin
     * @return int - dbid
     */
    public int getDBId(Origin origin) throws SQLException, NotFound {
        insert(origin, getDBIdStmt, 1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) return rs.getInt("origin_id");
        throw new NotFound('\n' + getDBIdStmt.toString());
    }

    public Origin[] getOrigins(int eventid) throws SQLException, NotFound {
        getAllStmt.setInt(1, eventid);
        ResultSet rs = getAllStmt.executeQuery();
        List origins = new ArrayList();
        while(rs.next())
            origins.add(get(rs.getInt("origin_id")));
        return (Origin[])origins.toArray(new Origin[origins.size()]);
    }

    public Origin get(int originId) throws SQLException, NotFound {
        getStmt.setInt(1, originId);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) return extract(rs);
        throw new NotFound(" there is no Origin object corresponding to the id "
                + originId);
    }

    public int insert(Origin origin, PreparedStatement stmt, int index)
            throws SQLException {
        stmt.setInt(index++,
                    jdbcCatalog.put(origin.catalog, origin.contributor));
        stmt.setInt(index++, timeTable.put(origin.origin_time));
        stmt.setInt(index++, jdbcLocation.put(origin.my_location));
        stmt.setString(index++, origin.get_id());
        return index;
    }

    public ParameterRef[] getParams(int dbId) throws SQLException {
        List ids = new ArrayList();
        getParamsStmt.setInt(1, dbId);
        ResultSet rs = getParamsStmt.executeQuery();
        while(rs.next()) {
            ids.add(new ParameterRef(rs.getString("parametera_id"),
                                     rs.getString("parametercreator")));
        }
        return (ParameterRef[])ids.toArray(new ParameterRef[ids.size()]);
    }

    /**
     * this method returns the Magnitude array given the dbid
     * 
     * @param id -
     *            dbid
     * @return - array of Magnitude
     */
    public Magnitude[] getMags(int originId) throws SQLException, NotFound {
        return jdbcMagnitude.get(originId);
    }

    /**
     * returns the Origin object given the result set and dbid
     * @param rs -
     *            ResultSet
     * @param id -
     *            dbid
     * 
     * @return - Origin
     */
    public Origin extract(ResultSet rs) throws SQLException,
            NotFound {
        int originId = rs.getInt("origin_id");
        ParameterRef[] params = getParams(originId);
        Magnitude[] magnitudes = getMags(originId);
        Location location = jdbcLocation.get(rs.getInt("origin_location_id"));
        if(location == null) { throw new NullPointerException("Location from database is NULL, originId="
                + originId); }
        return new OriginImpl(rs.getString("origin_text_id"),
                              jdbcCatalog.get(rs.getInt("origin_catalog_id")),
                              jdbcCatalog.getContributor(rs.getInt("origin_catalog_id")),
                              timeTable.get(rs.getInt("origin_time_id")),
                              location,
                              magnitudes,
                              params);
    }

    protected JDBCLocation jdbcLocation;

    protected JDBCMagnitude jdbcMagnitude;

    protected JDBCParameterRef jdbcParamRef;

    protected JDBCEventAttr jdbcEventAttr;

    protected JDBCCatalog jdbcCatalog;

    private JDBCTime timeTable;

    protected String magnitudeSubTableName = "originmagnitude";

    private PreparedStatement putStmt, getStmt, getDBIdStmt, getParamsStmt,
            putOriginParamRefStmt, deleteOriginParamRefStmt, updateEventIdStmt,
            getAllStmt, deleteOriginStmt;

    private JDBCSequence seq;
} // JDBCOrigin
