package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil{
    public static boolean tableExists(String tableName, Connection conn)
        throws SQLException{
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet rs = dbmd.getTables(null, null, null, null);
        while(rs.next()){
            if(rs.getString("TABLE_NAME").equalsIgnoreCase(tableName)) return true;
        }
        return false;
    }
    
    public static void printTableExistence(String name, Connection conn)throws SQLException{
        if(tableExists(name, conn)) System.out.println(name + " exists");
        else System.out.println(name + " does not exist");
    }
    
    
    public static void printExistingTables(Connection conn)throws SQLException{
        DatabaseMetaData dbmd = conn.getMetaData();
        System.out.println("********************************************");
        ResultSet rs = dbmd.getTables(null, null, null, null);
        while(rs.next()){
            System.out.println(rs.getString("TABLE_NAME"));
        }
        System.out.println("********************************************");
    }
}
