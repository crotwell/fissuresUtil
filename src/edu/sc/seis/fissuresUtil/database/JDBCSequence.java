package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class must be subclassed by the specific JDBC driver that wants to use
 * it.  The subclass should create a sequence called name in its constructor
 * and assign a PreparedStatement to nextVal that returns a result set when
 * executeQuery is called on it with one row in it with an integer in that row
 * that contains the next value
 */
public class JDBCSequence{
    public JDBCSequence(Connection conn, String name)throws SQLException{
        this.conn = conn;
        this.name = name;
        try{
            if(!DBUtil.tableExists(name, conn)){
                conn.createStatement().executeUpdate(ConnMgr.getSQL(name + ".create"));
            }
        }catch(SQLException e){}//Database must already exist
        nextVal = conn.prepareStatement(ConnMgr.getSQL(name + ".nextVal"));
    }
    
    public int next() throws SQLException {
        ResultSet rs = nextVal.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
    
    private PreparedStatement nextVal;
    
    protected String name;
    
    protected Connection conn;
}
