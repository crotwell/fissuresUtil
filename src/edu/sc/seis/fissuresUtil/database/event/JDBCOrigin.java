package edu.sc.seis.fissuresUtil.database.event;

import edu.sc.seis.fissuresUtil.*;
import edu.sc.seis.fissuresUtil.database.*;

import java.sql.*;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.event.OriginImpl;
import java.util.ArrayList;
import java.util.List;

public class JDBCOrigin extends JDBCTable {
    public JDBCOrigin(Connection conn) throws SQLException{
        this(conn, new JDBCLocation(conn), new JDBCEventAttr(conn),
             new JDBCParameterRef(conn), new JDBCMagnitude(conn),
             new JDBCCatalog(conn));
    }
    
    /**
     * Constructor for the JDBCOrigin class. In this Constructor the following tables are created
     * 1. Origin
     * 2. OriginParameterReference
     * 3. OriginMagnitude
     */
    public JDBCOrigin(Connection conn, JDBCLocation jdbcLocation,
                      JDBCEventAttr jdbcEventAttr,
                      JDBCParameterRef jdbcParameterRef,
                      JDBCMagnitude jdbcMagnitude,
                      JDBCCatalog jdbcCatalog)  throws SQLException {
        super("origin",conn);
        this.jdbcLocation = jdbcLocation;
        this.jdbcParamRef = jdbcParameterRef;
        this.jdbcMagnitude = jdbcMagnitude;
        this.jdbcCatalog = jdbcCatalog;
        String parameterSubTableName = "originparamref";
        this.jdbcEventAttr = jdbcEventAttr;
        seq = new JDBCSequence(conn, "OriginSeq");
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("origin", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("origin.create"));
        }
        if(!DBUtil.tableExists("originparamref", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("originparamref.create"));
        }
        if(!DBUtil.tableExists("originmagnitude", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("originmagnitude.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO origin "+
                                            "(originid, "+
                                            "origincatalogid, " +
                                            "origin_time, " +
                                            "originnanoseconds, "+
                                            "originleapseconds, "+
                                            "originlocationid, "+
                                            "origintextid) "+
                                            "VALUES(?,?,?,?,?,?,?)");
        putOriginParamRefStmt = conn.prepareStatement(" INSERT INTO "+
                                                          parameterSubTableName+
                                                          "(originparamrefid,"+
                                                          " originparameterid )"+
                                                          " VALUES(?,?)");
        deleteOriginParamRefStmt = conn.prepareStatement(" DELETE FROM "+
                                                             parameterSubTableName+
                                                             " WHERE originparamrefid = ?");
        putMagStmt = conn.prepareStatement(" INSERT INTO originmagnitude "+
                                               "(originmagnitudeid,"+
                                               " originrefmagnitudeid )"+
                                               " VALUES(?,?)");
        deleteOriginMagnitudeStmt = conn.prepareStatement(" DELETE FROM originmagnitude"+
                                                              " WHERE originmagnitudeid = ?");
        getDBIdStmt = conn.prepareStatement(" SELECT originid FROM origin "+
                                                " WHERE "+
                                                " origincatalogid = ? AND"+
                                                " origin_time = ? AND" +
                                                " originnanoseconds = ? AND"+
                                                " originleapseconds = ? AND"+
                                                " originlocationid = ? AND"+
                                                " origintextid = ?");
        getStmt = conn.prepareStatement(" SELECT *"+
                                            " FROM origin"+
                                            " WHERE originid = ?");
        getParamsStmt = conn.prepareStatement(" SELECT parametera_id, parametercreator FROM "+
                                                  jdbcParamRef.getTableName()+","+
                                                  parameterSubTableName+","+
                                                  tableName+
                                                  " WHERE "+
                                                  " parameterref.parameterid = "+
                                                  " originparamref.originparameterid"+
                                                  " AND "+
                                                  " originparamrefid = "+
                                                  " originid AND "+
                                                  " originid = ?");
        getMagStmt = conn.prepareStatement(" SELECT magnitudetype,magnitudevalue,"+
                                               jdbcMagnitude.getTableName()+
                                               ".magnitudecontributorid FROM "+
                                               jdbcMagnitude.getTableName()+","+
                                               magnitudeSubTableName+","+
                                               tableName+
                                               " WHERE "+
                                               " magnitude.magnitudeid = "+
                                               " originmagnitude.originrefmagnitudeid"+
                                               " AND "+
                                               " originmagnitudeid = "+
                                               " originid AND "+
                                               " originid = ?");
        updateEventIdStmt = conn.prepareStatement(" UPDATE "+tableName+
                                                      " SET origineventid = ? "+
                                                      " WHERE originid = ?");
        deleteOriginStmt = conn.prepareStatement(" DELETE FROM "+tableName+
                                                     " WHERE originid = ?");
        getAllStmt = conn.prepareStatement(" SELECT originid FROM origin "+
                                               "  WHERE origineventid = ?");
    }
    
    
    /**
     * inserts an origin object into the database and returns the correspoding dbid
     * @param origin - Origin
     * @return int - dbid
     */
    public int put(Origin origin) throws SQLException{
        try{
            return getDBId(origin);
        } catch( NotFound ex) { return insertOrigin(origin); }
    }
    
    private int insertOrigin(Origin o) throws SQLException{
        int id = seq.next();
        putStmt.setInt(1,id);
        insert(o,putStmt,2);
        putStmt.executeUpdate();
        putParamRefs(o.parm_ids,id);
        putMagnitudes(o.magnitudes,id);
        return id;
    }
    
    /**
     * This method is mainly used in updating the origineventid of the origin class given the Origin.
     * If the Origin is not present already it is inserted.
     * @param origin - Origin
     * @param eventid - Eventid
     * @return - the dbid
     */
    public int put(Origin origin, int eventId) throws SQLException {
        int id = put(origin);
        setEventId(id, eventId);
        return id;
    }
    
    private void setEventId(int originId, int eventId) throws SQLException{
        updateEventIdStmt.setInt(1,eventId);
        updateEventIdStmt.setInt(2,originId);
        updateEventIdStmt.executeUpdate();
    }
    
    /**
     * This method inserts the parameterRef into the parameterRef Table.
     * @param params - Array of ParameterRef
     * @id - origin dbid
     */
    public void putParamRefs(ParameterRef[] params, int id)throws SQLException {
        for( int i=0; i < params.length ; i++) {
            putParamRef(id, jdbcParamRef.put(params[i]));
        }
    }
    
    
    /**
     * This method inserts a row into originparameterreference table
     * @param id - origin dbid
     * @paramId - the parameterid obtained after inserting one of the params into the ParameterRefTable
     */
    public void putParamRef(int id, int paramId) throws SQLException {
        putOriginParamRefStmt.setInt(1, id);
        putOriginParamRefStmt.setInt(2, paramId);
        putOriginParamRefStmt.executeUpdate();
    }
    
    /**
     * This method deletes all the rows int originparameterreference for which
     * originparameterreferenceid = originid
     * @param originid - the dbid of the origin
     */
    public void deleteParamRef(int originid) throws SQLException {
        deleteOriginParamRefStmt.setInt(1, originid);
        deleteOriginParamRefStmt.executeUpdate();
    }
    
    
    /**
     * This method inserts the Magnitude into the Magnitude Table.
     * @param magnitudes - Array of Magnitude
     * @originid - dbid
     */
    public void putMagnitudes(Magnitude[] mags, int id) throws SQLException {
        for( int i=0; i < mags.length ; i++) {
            putMag(id, jdbcMagnitude.put(mags[i]));
        }
    }
    
    /**
     * This method inserts a row into originmagnitude table
     * @param id - origin dbid
     * @mag id - the int obtained after inserting one of the magnitudes into the MagnitudeTable
     */
    public void putMag(int id, int magId) throws SQLException {
        putMagStmt.setInt(1, id);
        putMagStmt.setInt(2, magId);
        putMagStmt.executeUpdate();
    }
    
    
    /**
     * This method deletes all the rows int originMagnitude for which originmagnitudeid = originid
     * @param originid - the dbid of the origin
     */
    public void deleteMag(int originid ) throws SQLException {
        deleteOriginMagnitudeStmt.setInt(1, originid);
        deleteOriginMagnitudeStmt.executeUpdate();
    }
    
    /**
     * This method returns the dbid given the origin object
     * @param origin - Origin
     * @return int - dbid
     */
    public int getDBId(Origin origin) throws SQLException, NotFound {
        insert(origin,getDBIdStmt,1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next())return rs.getInt("originid");
        throw new NotFound('\n'+getDBIdStmt.toString());
    }
    
    /**
     * This function returns all the origins as an array of Origins.
     * @param eventid - the the dbid of the event
     * @return - an Array of Origin[]
     */
    public Origin[] getOrigins(int eventid) throws SQLException, NotFound {
        getAllStmt.setInt(1, eventid);
        ResultSet rs = getAllStmt.executeQuery();
        List origins = new ArrayList();
        while (rs.next())  origins.add(get(rs.getInt("originid")));
        return (Origin[])origins.toArray(new Origin[origins.size()]);
    }
    
    
    
    /**
     * This method returns the origin object given the dbid
     * @param id - dbid
     */
    public Origin get(int id) throws SQLException, NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next())  return extract(rs,id);
        throw new NotFound(" there is no Origin object corresponding to the id "+id);
    }
    
    public int insert(Origin origin,PreparedStatement stmt, int index)
        throws SQLException{
        stmt.setInt(index++, jdbcCatalog.put(origin.catalog, origin.contributor));
        index = JDBCTime.insert(origin.origin_time,stmt,index);
        stmt.setInt(index++,jdbcLocation.put(origin.my_location));
        stmt.setString(index++, origin.get_id());
        return index;
    }
    
    
    /**
     * this method returns the ParameterRef array given the dbid
     * @param id - dbid
     * @return - array of ParameterRef
     */
    
    public ParameterRef[] getParams(int id) throws SQLException{
        List ids = new ArrayList();
        getParamsStmt.setInt(1,id);
        ResultSet rs = getParamsStmt.executeQuery();
        while(rs.next()) {
            ids.add(new ParameterRef(rs.getString("parametera_id"),
                                     rs.getString("parametercreator")));
        }
        return (ParameterRef[]) ids.toArray(new ParameterRef[ids.size()]);
    }
    
    /**
     * this method returns the Magnitude array given the dbid
     * @param id - dbid
     * @return - array of Magnitude
     */
    public Magnitude[] getMags(int id) throws SQLException, NotFound{
        List ids = new ArrayList();
        getMagStmt.setInt(1,id);
        ResultSet rs = getMagStmt.executeQuery();
        while(rs.next()) {
            ids.add(new Magnitude(rs.getString("magnitudetype"),
                                  rs.getFloat("magnitudevalue"),
                                  jdbcCatalog.getContributorOnContributorId(rs.getInt("magnitudecontributorid"))));
        }
        return (Magnitude[]) ids.toArray(new Magnitude[ids.size()]);
    }
    
    /**
     * returns the Origin object given the result set and dbid
     * @param rs - ResultSet
     * @param id - dbid
     * @return - Origin
     */
    public Origin extract(ResultSet rs,int id) throws SQLException,NotFound {
        ParameterRef[] params = getParams(id);
        Magnitude[] magnitudes = getMags(id);
        Timestamp ts = rs.getTimestamp("origin_time");
        int nanoseconds = rs.getInt("originnanoseconds");
        int leapseconds = rs.getInt("originleapseconds");
        return new OriginImpl(rs.getString("origintextid"),
                              jdbcCatalog.get(rs.getInt("origincatalogid")),
                              jdbcCatalog.getContributor(rs.getInt("origincatalogid")),
                              JDBCTime.makeTime(ts,nanoseconds,leapseconds),
                              jdbcLocation.get(rs.getInt("originlocationid")),
                              magnitudes,
                              params);
    }
    
    /**
     * This function deletes the row corresponding to the give originid
     * @param originid - the dbid of the origin
     */
    public void deleteOrigin(int originid) throws SQLException, NotFound {
        getStmt.setInt(1,originid);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) {
            deleteOriginStmt.setInt(1, originid);
            deleteParamRef(originid);
            deleteMag(originid);
            jdbcEventAttr.clearPreferredOrigin(originid);
            deleteOriginStmt.executeUpdate();
        }
    }
    
    protected JDBCLocation jdbcLocation;
    
    protected JDBCMagnitude jdbcMagnitude;
    
    protected JDBCParameterRef jdbcParamRef;
    
    protected JDBCEventAttr jdbcEventAttr;
    
    protected JDBCCatalog jdbcCatalog;
    
    protected String magnitudeSubTableName = "originmagnitude";
    
    private PreparedStatement putStmt, getStmt, getDBIdStmt, getMagStmt,
        getParamsStmt, putMagStmt, deleteOriginMagnitudeStmt,
        putOriginParamRefStmt, deleteOriginParamRefStmt, updateEventIdStmt,
        getAllStmt, deleteOriginStmt;
    
    private JDBCSequence seq;
    
} // JDBCOrigin
