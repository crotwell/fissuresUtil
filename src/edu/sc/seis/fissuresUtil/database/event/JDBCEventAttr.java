package edu.sc.seis.fissuresUtil.database.event;

import edu.sc.seis.fissuresUtil.*;
import edu.sc.seis.fissuresUtil.database.*;

import java.sql.*;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.EventAttrImpl;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class JDBCEventAttr extends JDBCTable {
    public JDBCEventAttr(Connection conn) throws SQLException{
        this(conn, new JDBCParameterRef(conn), new JDBCFlinnEngdahl(conn));
    }
    
    /**
     * Constructor for JDBCEventAttr . In this Constructor following Tables are
     * created if they don't already exist.
     * 1. EventAttr.
     * 2. EventParameterReference.
     */
    public JDBCEventAttr(Connection conn, JDBCParameterRef paramRef,
                         JDBCFlinnEngdahl flinnEngdahl) throws SQLException {
        super("eventattr",conn);
        this.paramRef = paramRef;
        this.flinnEngdahl = flinnEngdahl;
        seq = new JDBCSequence(conn, "EventAttrSeq");
        
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("eventattr", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("eventattr.create"));
        }
        if(!DBUtil.tableExists("eventparameterreference", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("eventparameterreference.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO eventattr"+
                                            " (eventid, eventname ,"+
                                            " eventflinnengdahlid)"+
                                            " VALUES( ?,?,?)");
        putEventParamRefStmt = conn.prepareStatement(" INSERT INTO eventparameterreference"+
                                                         " (eventparameterreferenceid, " +
                                                         " parameterid ) "+
                                                         " VALUES(?,?)");
        insertPreferredOrigin = conn.prepareStatement(" UPDATE eventattr"+
                                                          " SET preferredorigin = ?"+
                                                          " WHERE eventid = ?");
        getDBIdStmt = conn.prepareStatement(" SELECT eventid FROM eventattr"+
                                                " WHERE eventname = ? AND"+
                                                " eventflinnengdahlid = ? ");
        getEvent = conn.prepareStatement(" SELECT eventname, eventflinnengdahlid, preferredorigin FROM "+
                                             "eventattr WHERE eventid = ?");
        getParameterRefs = conn.prepareStatement(" SELECT parametera_id, parametercreator FROM "+
                                                     paramRef.getTableName()+", "+
                                                     "eventparameterreference, "+
                                                     "eventattr "+
                                                     " WHERE "+
                                                     " parameterref.parameterid = "+
                                                     " eventparameterreference.parameterid"+
                                                     " AND "+
                                                     "eventparameterreferenceid = "+
                                                     " eventid AND "+
                                                     " eventid = ?");
        getAllStmt = conn.prepareStatement(" SELECT eventid FROM eventattr");
        getPreferredOriginStmt = conn.prepareStatement(" select preferredorigin FROM eventattr"+
                                                           " WHERE eventid = ?");
        getByNameStmt = conn.prepareStatement(" select distinct eventid FROM eventattr"+
                                                  " WHERE eventname = ?");
        getByPreferredOriginIdStmt = conn.prepareStatement(" select distinct eventid FROM eventattr"+
                                                               " WHERE preferredorigin = ?");
        updateRegionStmt = conn.prepareStatement(" UPDATE eventattr"+
                                                     " SET eventflinnengdahlid = ? "+
                                                     " WHERE eventid = ?");
    }
    
    /**
     * This method sets the jdbcOrigin field of JDBCEventAttr
     */
    public void setJDBCOrigin(JDBCOrigin originTable) throws SQLException{
        this.originTable = originTable;
        getOnConstraintStmt = conn.prepareStatement(" SELECT  distinct eventid, "+
                                                        " origin_time, "+
                                                        " originnanoseconds, "+
                                                        " originleapseconds,  "+
                                                        " catalog, "+
                                                        " contributor, "+
                                                        " magnitudetype "+
                                                        " FROM " +tableName+
                                                        " , "+originTable.getTableName()+
                                                        " , "+originTable.magnitudeSubTableName+
                                                        " , "+originTable.jdbcMagnitude.getTableName()+
                                                        " , "+originTable.jdbcLocation.getTableName()+
                                                        " ,"+originTable.jdbcLocation.getUnitTableName()+
                                                        " , "+originTable.jdbcCatalog.getTableName()+
                                                        " , "+originTable.jdbcCatalog.jdbcContributor.getTableName()+
                                                        " WHERE originid = preferredorigin AND "+
                                                        " originmagnitudeid = originid AND "+
                                                        " magnitudeid = originrefmagnitudeid AND "+
                                                        " locationid = "+
                                                        " originlocationid AND "+
                                                        " locationdepthunitid = "+originTable.jdbcLocation.getUnitTableName()+".dbid AND "+
                                                        " locationdepthvalue >= ? AND locationdepthvalue <= ? AND "+
                                                        " locationlatitude  >= ? AND locationlatitude <= ? AND "+
                                                        " locationlongitude >= ? AND locationlongitude <= ? AND "+
                                                        " magnitudevalue >= ? AND magnitudevalue <= ? AND "+
                                                        " origin_time >=  ? AND origin_time <= ? AND "+
                                                        " origincatalogid = catalogid AND "+
                                                        " catalogcontributorid = contributorid"
                                                   );
    }
    
    /** puts the various fields of EventAttr into a preparedStatement
     * @param eventAttr - EventAttr
     * @param stmt - PreparedStatement
     * @index - the index
     * @return - the resulting index.
     */
    private int insert(EventAttr eventAttr, PreparedStatement stmt, int index)
        throws SQLException {
        stmt.setString(index++,eventAttr.name);
        stmt.setInt(index++, flinnEngdahl.put(eventAttr.region));
        //stmt.setNull(index++,java.sql.Types.INTEGER);
        //index = JDBCFlinnEngdahl.(eventAttr.region,stmt,index);
        return index;
    }
    
    
    /**
     * Returns the dbid given the object of eventAttr
     * @param eventAttr - eventAttr given as input
     * @return int - the dbid corresponding to eventAttr
     */
    public int getDBId(EventAttr eventAttr) throws SQLException,NotFound  {
        insert(eventAttr,getDBIdStmt,1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("eventid");
        }
        throw new NotFound("id for this eventAttr is not found");
    }
    
    /**
     * This function returns all the EventAttributes as an array
     * @return - an Array of EventAttr[]
     */
    public EventAttr[] getEventAttrs() throws SQLException, NotFound {
        ResultSet rs = getAllStmt.executeQuery();
        EventAttr[] eventAttrs ;
        ArrayList aList = new ArrayList();
        while (rs.next()) {
            aList.add(get(rs.getInt("eventid")));
        }
        eventAttrs = new EventAttr[aList.size()];
        /*for (int i = 0; i < aList.size(); i++) {
         eventAttrs[i] = (EventAttr) aList.get(i);
         }*/
        eventAttrs = (EventAttr[]) aList.toArray(eventAttrs);
        return eventAttrs;
    }
    
    /**
     * This function returns an array of EventAttrs given an preferred origin id
     * @param originid - the dbid of the preferred origin
     * @return - an array of EventAttr[]
     */
    public EventAttr[] getByPreferredOriginId(int originid) throws SQLException, NotFound {
        getByPreferredOriginIdStmt.setInt(1,originid);
        EventAttr[] eventAttrs;
        ArrayList aList = new ArrayList();
        ResultSet rs = getByPreferredOriginIdStmt.executeQuery();
        while(rs.next()) {
            aList.add( get(rs.getInt("eventid")));
        }
        eventAttrs = new EventAttr[aList.size()];
        eventAttrs = (EventAttr[]) aList.toArray(eventAttrs);
        return eventAttrs;
    }
    
    /**
     * This function returns all the EventAttributes as an array given the eventname
     * @param name - the eventname
     * @return - an Array of Integer
     */
    public java.lang.Integer[] getByName(String name) throws SQLException, NotFound {
        getByNameStmt.setString(1, name);
        ResultSet rs = getByNameStmt.executeQuery();
        //EventAttr[] eventAttrs;
        ArrayList aList = new ArrayList();
        while(rs.next()) {
            aList.add(new java.lang.Integer(rs.getInt("eventid")));
            //get(rs.getInt("eventid")));
            
        }
        java.lang.Integer[] eventIds  = new java.lang.Integer[aList.size()];
        eventIds = (java.lang.Integer[]) aList.toArray(eventIds);
        return eventIds;
    }
    
    
    /**
     * This function returns all the EventAttributes as an array which satisfy some constraints.
     * @param
     * @return - an Array of Integer
     */
    public Integer[] getOnConstraint(double min_depth,
                                     double max_depth,
                                     float minLat,
                                     float maxLat,
                                     float minLon,
                                     float maxLon,
                                     edu.iris.Fissures.Time stime,
                                     edu.iris.Fissures.Time etime,
                                     float min_magnitude,
                                     float max_magnitude,
                                     String[] search_types,
                                     String[] catalogs,
                                     String[] contributors) throws SQLException{
        Timestamp tse = JDBCTimeCheck.getTimeStamp(etime);
        Timestamp tss = JDBCTimeCheck.getTimeStamp(stime);
        tss.setNanos(0);
        if(tse.getNanos() != 0) {
            tse.setNanos(0);
            tse.setSeconds(tse.getSeconds() + 1);
        }
        
        //will set the ? values of the getOnConstraintStmt
        //and then executeQuery on that stmt and
        // while rs.next() will build an array of
        //EventAttr's and at the end return those event Attrs.
        logger.debug("The min depth is "+min_depth);
        logger.debug("The max depth is "+max_depth);
        logger.debug("The minLatitude is "+minLat);
        logger.debug("The maxLatitude is "+maxLat);
        logger.debug("The minLontitude is "+minLon);
        logger.debug("The maxLongitude is "+maxLon);
        logger.debug("The minMagnitude is "+min_magnitude);
        logger.debug("The maxMagnitude is "+max_magnitude);
        logger.debug("The startTime is "+stime.date_time);
        logger.debug("The endTime is "+etime.date_time);
        
        getOnConstraintStmt.setDouble(1,min_depth);
        getOnConstraintStmt.setDouble(2,max_depth);
        getOnConstraintStmt.setFloat(3,minLat);
        getOnConstraintStmt.setFloat(4,maxLat);
        getOnConstraintStmt.setFloat(5,minLon);
        getOnConstraintStmt.setFloat(6,maxLon);
        getOnConstraintStmt.setFloat(7,min_magnitude);
        getOnConstraintStmt.setFloat(8,max_magnitude);
        getOnConstraintStmt.setTimestamp(9, JDBCTimeCheck.getTimeStamp(stime));
        getOnConstraintStmt.setTimestamp(10, JDBCTimeCheck.getTimeStamp(etime));
        
        logger.debug("The resultant query is "+getOnConstraintStmt.toString());
        ResultSet rs = getOnConstraintStmt.executeQuery();
        ArrayList aList = new ArrayList();
        while (rs.next()) {
            if( checkIfValid(catalogs, rs.getString("catalog")) &&
               checkIfValid(contributors, rs.getString("contributor")) &&
               checkIfValid(search_types, rs.getString("magnitudetype"))) {
                aList.add(new Integer(rs.getInt("eventid")));
            }
        }
        Integer[] eventIds = new Integer[aList.size()];
        return (Integer[])aList.toArray(eventIds);
    }
    
    
    /**
     * Inserting the eventAttr into the database
     * @param eventAttr - eventAttr to be insert into database
     * @return int - the dbid of the eventAttr
     */
    public int put(EventAttr eventAttr) throws SQLException {
        try{
            return getDBId(eventAttr);
        } catch(NotFound notFound) {
            int id = seq.next();
            putStmt.setInt(1,id);
            insert(eventAttr,putStmt,2);
            putStmt.executeUpdate();
            insertParameters(eventAttr.parm_ids, id);
            return id;
        }
    }
    
    /**
     * Inserting the preferred Origin into EventAttr Table
     * @param originin - the Origin
     * @ id - the id
     */
    public void putPreferredOrigin(Origin origin, int id) throws SQLException,NotFound {
        insertPreferredOrigin.setInt(1,originTable.put(origin,id));
        insertPreferredOrigin.setInt(2,id);
        insertPreferredOrigin.executeUpdate();
    }
    
    
    /**
     * This function takes the dbid of the preferred origin as an input parameter and
     * sets the preferredorigindbid for the corresponding rows of the event to be null
     * @param originid - the dbid of the preferred origin
     */
    public void clearPreferredOrigin(int originid) throws NotFound, SQLException {
        EventAttr[] eventAttrs = getByPreferredOriginId(originid);
        for(int counter = 0; counter < eventAttrs.length; counter++) {
            int eventid = getDBId(eventAttrs[counter]);
            insertPreferredOrigin.setNull(1, Types.INTEGER);
            insertPreferredOrigin.setInt(2, eventid);
            insertPreferredOrigin.executeUpdate();
        }
    }
    
    /**
     * The value of preferred Origin is returned
     * @param eventAttr
     * @return the PreferredOrigin Corresponding to the eventAttr
     */
    public Origin getPreferredOrigin(EventAttr eventAttr) throws SQLException,NotFound {
        return getPreferredOriginOnEventId(getDBId(eventAttr));
    }
    
    
    /**
     * Returns the Preferred Origin given the event id
     * @param id - dbid in the eventAttr table
     * @return Origin - the preferredOrigin corresponding to this eventid
     */
    public Origin getPreferredOriginOnEventId(int id) throws SQLException,NotFound {
        getPreferredOriginStmt.setInt(1,id);
        ResultSet rs = getPreferredOriginStmt.executeQuery();
        if(rs.next()) {
            try {
                return originTable.get(rs.getInt("preferredorigin"));
            } catch(SQLException e) {
                throw new SQLException(" The information about this origin is not found in origin table");
            }
        }
        throw new SQLException(" The information about this origin is not found in origin table");
        
    }
    
    
    /**
     * Returns and EventAttr given eventid
     * @param eventid
     * @return EventAttr
     */
    public EventAttr get(int eventid) throws SQLException, NotFound {
        getEvent.setInt(1,eventid);
        ResultSet rs = getEvent.executeQuery();
        if ( rs.next())  return extract(rs,eventid);
        throw new NotFound("No EventAttr Found for eventid = "+ eventid);
    }
    
    
    
    /**
     * Inserts the ParameterRefs Array
     * @param params - the parameterreference array
     * @param eventid - the dbid of eventAttr table
     */
    
    
    public void insertParameters(ParameterRef[] params, int eventid)
        throws SQLException{
        for( int i=0; i < params.length ; i++) {
            int paramid = paramRef.put(params[i]);
            putEventParameterRef(eventid, paramid);
        }
    }
    
    /**
     * inserts a record in eventparameterreference table
     * @param eventid - the dbid of eventAttr table
     * @param parameterid - the dbid of the ParameterReference table
     */
    
    public void putEventParameterRef(int eventid, int parameterid)
        throws SQLException {
        putEventParamRefStmt.setInt(1, eventid);
        putEventParamRefStmt.setInt(2, parameterid);
        putEventParamRefStmt.executeUpdate();
    }
    
    /**
     * Gets the parameterRefs given the eventid
     * @param id - eventid
     * @return ParamterRef[] - the ParameterRef array
     */
    public ParameterRef[] getParameters(int id) throws SQLException{
        List ids = new ArrayList();
        getParameterRefs.setInt(1,id);
        ResultSet rs = getParameterRefs.executeQuery();
        while(rs.next()) {
            ids.add(new ParameterRef(rs.getString("parametera_id"),
                                     rs.getString("parametercreator")));
        }
        ParameterRef[] params = new ParameterRef[ids.size()];
        return (ParameterRef[]) ids.toArray(params);
    }
    
    /**
     * Returns a new EventAttr based on the resultset
     * @param rs - ResultSet
     * @id - the eventid
     * @return - the EventAttr
     */
    private EventAttr extract(ResultSet rs, int id) throws SQLException,NotFound {
        ParameterRef[] params = getParameters(id);
        FlinnEngdahlRegion region = flinnEngdahl.get(rs.getInt("eventflinnengdahlid"));
        return new EventAttrImpl(rs.getString("eventname"),region,params);
    }
    
    /**
     * This function updates The FlinnengdahlRegion associated with an event.
     * @param id - the eventid
     * @param region - the flinnengdahlregion
     */
    public void updateFlinnEngdahlRegion(int id, FlinnEngdahlRegion region)
        throws SQLException, NotFound {
        updateRegionStmt.setInt(1, flinnEngdahl.getDBId(region));
        updateRegionStmt.setInt(2, getDBId(get(id)));
        updateRegionStmt.executeUpdate();
    }
    
    private boolean checkIfValid(String[] list, String value) {
        if(list.length == 0 || list == null) return true;
        if(value == null) return false;
        for(int counter = 0; counter < list.length; counter++) {
            if(list[counter].equalsIgnoreCase(value)) return true;
        }
        return false;
    }
    
    private JDBCFlinnEngdahl flinnEngdahl;
    
    private JDBCParameterRef paramRef;
    
    private JDBCOrigin originTable;
    
    private PreparedStatement getDBIdStmt;
    
    private PreparedStatement getEvent;
    
    private PreparedStatement getParameterRefs;
    
    private PreparedStatement getByNameStmt;
    
    private PreparedStatement putStmt;
    
    private PreparedStatement putEventParamRefStmt;
    
    private PreparedStatement insertPreferredOrigin;
    
    private PreparedStatement getPreferredOriginStmt;
    
    private PreparedStatement getAllStmt;
    
    private PreparedStatement getOnConstraintStmt;
    
    private PreparedStatement getByPreferredOriginIdStmt;
    
    private PreparedStatement updateRegionStmt;
    
    private JDBCSequence seq;
    
    private static Logger logger = Logger.getLogger(JDBCEventAttr.class);
}
