package edu.sc.seis.fissuresUtil.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

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

    /**
     * Finds all PreparedStatement fields on this class and if an sql statement
     * is available in ConnMgr of the form tablename.fieldname a prepared
     * statement is created for it and assigned to the field
     */
    protected void prepareStatements() throws SQLException {
        Field[] fields = getClass().getDeclaredFields();
        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String key = tableName + "." + field.getName();
            if(ConnMgr.hasSQL(key)) {
                String sql = ConnMgr.getSQL(key);
                boolean setAccessible = false;
                if(!field.isAccessible()) {
                    field.setAccessible(true);
                    setAccessible = true;
                }
                try {
                    field.set(this, conn.prepareStatement(sql));
                } catch(IllegalArgumentException e) {
                    GlobalExceptionHandler.handle("Thought this couldn't happen since I checked the object type.",
                                                  e);
                } catch(IllegalAccessException e) {
                    GlobalExceptionHandler.handle("Thought this couldn't happen since I called setAccessible.  Looks like I was wrong",
                                                  e);
                }
                if(setAccessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    protected String tableName;

    protected Connection conn;
} // JDBCTable
