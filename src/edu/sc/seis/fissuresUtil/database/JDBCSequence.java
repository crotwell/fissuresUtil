package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCSequence{
    public JDBCSequence(Connection conn, String name)throws SQLException{
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
}
