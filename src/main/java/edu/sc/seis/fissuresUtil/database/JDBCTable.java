package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;

/**
 * JDBCTable.java All methods are unsyncronized, the calling application should
 * make sure that a single instance of this class is not accessed by more than
 * one thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE! Created: Fri Jan 26
 * 10:00:16 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class JDBCTable {

    public JDBCTable(String tableName, Connection conn) {
        this.tableName = tableName;
        this.conn = conn;
    }

    public String getTableName() {
        return tableName;
    }

    public Connection getConnection() {
        return conn;
    }

    protected String tableName;

    protected Connection conn;
} // JDBCTable
