package edu.sc.seis.fissuresUtil.database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCParameterRef extends JDBCTable {
    public JDBCParameterRef(Connection conn) throws SQLException {
        super("parameterref",conn);
        Statement stmt = conn.createStatement();
        //Created the sequence.
        seq = new JDBCSequence(conn, "ParameterRefSeq");
        //Created the table
        TableSetup.setup(getTableName(), conn, this, "edu/sc/seis/fissuresUtil/database/props/default.props");
        
        insert = conn.prepareStatement("INSERT INTO parameterref"+
                                           " ( parameterid,"+
                                           " parametera_id,"+
                                           " parametercreator )"+
                                           "VALUES (?,?,?)");
        
        get = conn.prepareStatement("SELECT parametera_id, "+
                                        " parametercreator"+
                                        " FROM parameterref"+
                                        " WHERE parameterid = ?");
        
        getDBId = conn.prepareStatement("SELECT parameterid FROM parameterref"+
                                            " WHERE parametera_id = ? AND "+
                                            " parametercreator = ?");
    }
    
    
    
    /**
     * Inserts the detaisl of ParameterRef into the database
     * @param parameterref - ParameterRef
     * @ return int - dbid
     */
    public int put(ParameterRef parameterRef) throws SQLException {
        try{
            return getDBId(parameterRef);
        }catch(NotFound notFound) {
            int id = seq.next();
            insert.setInt(1,id);
            insert.setString(2, parameterRef.a_id);
            insert.setString(3, parameterRef.creator);
            insert.executeUpdate();
            return id;
        }
    }
    
    /**
     * This method returns the dbid given teh ParameterRef Object.
     * @param parameterref - ParameterRef
     * @return int - the dbid
     */
    public int getDBId(ParameterRef parameterRef)throws SQLException, NotFound {
        getDBId.setString(1, parameterRef.a_id);
        getDBId.setString(2, parameterRef.creator);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next())  return rs.getInt("parameterid");
        throw new NotFound("parameterref is not found");
    }
    
    /**
     * This method returns the ParameterRef given the dbid
     * @param id - the dbid
     * @return - ParameterRef
     */
    public  ParameterRef get(int id) throws SQLException,NotFound {
        get.setInt(1,id);
        ResultSet rs = get.executeQuery();
        if(rs.next()) {
            return new ParameterRef(rs.getString("parametera_id"),
                                    rs.getString("parametercreator"));
        }
        throw new NotFound("No ParameterRef is found for paameterid " +id);
    }
    
    private PreparedStatement get;
    
    private PreparedStatement getDBId;
    
    private PreparedStatement insert;
    
    private JDBCSequence seq;
} // JDBCParameterRef
