package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCSequence{
    
    public JDBCSequence(Connection conn, String name)throws SQLException{
        this(conn, name, ConnMgr.getSQL(name + ".create"), ConnMgr.getSQL(name + ".nextVal"));
    }

    public JDBCSequence(Connection conn, String name, String creationSQL, String nextValSQL)throws SQLException{
        try{
            if(!DBUtil.tableExists(name, conn)){
                conn.createStatement().executeUpdate(creationSQL);
            }
        }catch(SQLException e){
            logger.info("Database must already exist for "+name, e);
        }
        nextVal = conn.prepareStatement(nextValSQL);
    }

    public int next() throws SQLException {
        ResultSet rs = nextVal.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private PreparedStatement nextVal;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCSequence.class);
}
