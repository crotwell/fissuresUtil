package edu.sc.seis.fissuresUtil.database.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves Created on Nov 24, 2004
 */
public class TableSetup {

    public static void setup(String tablename,
                      Connection conn,
                      Object tableObj,
                      SQLLoader statements) throws SQLException {
        createTable(tablename, conn, statements.get(tablename + ".create"));
        prepareStatements(tablename, conn, tableObj, statements);
    }

    private static void createTable(String tablename,
                             Connection conn,
                             String creationStmt) throws SQLException {
        System.out.println("Running create statement " + creationStmt);
        if(!DBUtil.tableExists(tablename, conn)) {
            conn.createStatement().executeUpdate(creationStmt);
        }
    }

    /**
     * Finds all PreparedStatement fields on tableObj's class and if an sql
     * statement is available in statements of the form tablename.fieldname a
     * prepared statement is created for it and assigned to the field
     */
    public static void prepareStatements(String tableName,
                                         Connection conn,
                                         Object tableObj,
                                         SQLLoader statements)
            throws SQLException {
        Field[] fields = tableObj.getClass().getDeclaredFields();
        for(int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String key = tableName + "." + field.getName();
            if(statements.has(key)) {
                String sql = statements.get(key);
                field.setAccessible(true);
                try {
                    field.set(tableObj, conn.prepareStatement(sql));
                } catch(IllegalArgumentException e) {
                    GlobalExceptionHandler.handle("Thought this couldn't happen since I checked the object type.",
                                                  e);
                } catch(IllegalAccessException e) {
                    GlobalExceptionHandler.handle("Thought this couldn't happen since I called setAccessible.  Looks like I was wrong",
                                                  e);
                }
            }
        }
    }
}