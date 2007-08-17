package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCParameterRef;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCEventAttr extends EventTable {
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
        String props = "edu/sc/seis/fissuresUtil/database/props/event/default.props";
        TableSetup.setup(getTableName(), conn, this, props, new String[] { "eventparameterreference" });
        putStmt = conn.prepareStatement(" INSERT INTO eventattr"+
                                            " (eventattr_id, eventattr_name ,"+
                                            " flinnengdahlid)"+
                                            " VALUES( ?,?,?)");
        putEventParamRefStmt = conn.prepareStatement(" INSERT INTO eventparameterreference"+
                                                         " (eventparameterreferenceid, " +
                                                         " parameterid ) "+
                                                         " VALUES(?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT eventattr_id FROM eventattr"+
                                                " WHERE eventattr_name = ? AND"+
                                                " flinnengdahlid = ? ");
        getEvent = conn.prepareStatement(" SELECT * FROM "+
                                             "eventattr WHERE eventattr_id = ?");
        getParameterRefs = conn.prepareStatement(" SELECT parametera_id, parametercreator FROM "+
                                                     paramRef.getTableName()+", "+
                                                     "eventparameterreference, "+
                                                     "eventattr "+
                                                     " WHERE "+
                                                     " parameterref.parameterid = "+
                                                     " eventparameterreference.parameterid"+
                                                     " AND "+
                                                     "eventparameterreferenceid = "+
                                                     " eventattr_id AND "+
                                                     " eventattr_id = ?");
        getAllStmt = conn.prepareStatement(" SELECT eventattr_id FROM eventattr");
        getByNameStmt = conn.prepareStatement(" select distinct eventattr_id FROM eventattr"+
                                                  " WHERE eventattr_name = ?");
        updateRegionStmt = conn.prepareStatement(" UPDATE eventattr"+
                                                     " SET flinnengdahlid = ? "+
                                                     " WHERE eventattr_id = ?");
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
            return rs.getInt("eventattr_id");
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
            aList.add(get(rs.getInt("eventattr_id")));
        }
        eventAttrs = new EventAttr[aList.size()];
        /*for (int i = 0; i < aList.size(); i++) {
         eventAttrs[i] = (EventAttr) aList.get(i);
         }*/
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
        ArrayList aList = new ArrayList();
        while(rs.next()) {
            aList.add(new java.lang.Integer(rs.getInt("eventattr_id")));
        }
        java.lang.Integer[] eventIds  = new java.lang.Integer[aList.size()];
        eventIds = (java.lang.Integer[]) aList.toArray(eventIds);
        return eventIds;
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
     * Returns and EventAttr given eventid
     * @param eventid
     * @return EventAttr
     */
    public EventAttr get(int eventid) throws SQLException, NotFound {
        getEvent.setInt(1,eventid);
        ResultSet rs = getEvent.executeQuery();
        if ( rs.next())  return extract(rs);
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
    public EventAttr extract(ResultSet rs) throws SQLException,NotFound {
        int id = rs.getInt("eventattr_id");
        ParameterRef[] params = getParameters(id);
        FlinnEngdahlRegion region = flinnEngdahl.get(rs.getInt("flinnengdahlid"));
        return new EventAttrImpl(rs.getString("eventattr_name"),region,params);
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

    private JDBCFlinnEngdahl flinnEngdahl;

    private JDBCParameterRef paramRef;

    private PreparedStatement getDBIdStmt;

    private PreparedStatement getEvent;

    private PreparedStatement getParameterRefs;

    private PreparedStatement getByNameStmt;

    private PreparedStatement putStmt;

    private PreparedStatement putEventParamRefStmt;

    private PreparedStatement getAllStmt;

    private PreparedStatement updateRegionStmt;

    private JDBCSequence seq;

    private static Logger logger = Logger.getLogger(JDBCEventAttr.class);
}
