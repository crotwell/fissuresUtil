package edu.sc.seis.fissuresUtil.database.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves Created on Nov 24, 2004
 */
public class TableSetup {

    public static void setup(JDBCTable table, String propFile)
            throws SQLException {
        setup(table.getTableName(), table.getConnection(), table, propFile);
    }

    public static void setup(String tableName,
                             Connection conn,
                             Object tableObj,
                             String propFile) throws SQLException {
        setup(tableName, conn, tableObj, propFile, new VelocityContext());
    }

    public static void setup(String tableName,
                             Connection conn,
                             Object tableObj,
                             String propFile,
                             Context ctx) throws SQLException {
        ctx.put(TABLE_NAME, tableName);
        SQLLoader sql = new SQLLoader(propFile, ctx);
        TableSetup.customSetup(tableName, conn, tableObj, sql);
    }

    public static void customSetup(String tablename,
                                   Connection conn,
                                   Object tableObj,
                                   SQLLoader statements) throws SQLException {
        if(!statements.getContext().containsKey(TABLE_NAME)) {
            statements.getContext().put(TABLE_NAME, tablename);
        }
        createTable(tablename, conn, statements);
        createViews(tablename, conn, statements);
        prepareStatements(tablename, conn, tableObj, statements);
    }

    private static void createViews(String tablename,
                                    Connection conn,
                                    SQLLoader statements) throws SQLException {
        String views = statements.get(tablename + ".views");
        if(views != null) {
            StringTokenizer tok = new StringTokenizer(views, ",");
            while(tok.hasMoreElements()) {
                String viewName = tok.nextToken();
                createTableOrView(viewName, conn, statements);
            }
        }
    }

    private static void createTable(String tablename,
                                    Connection conn,
                                    SQLLoader statements) throws SQLException {
        createTableOrView(tablename, conn, statements);
    }

    private static void createTableOrView(String tablename,
                                          Connection conn,
                                          SQLLoader statements)
            throws SQLException {
        if(!DBUtil.tableExists(tablename, conn)) {
            String creationStmt = statements.get(tablename + ".create");
            if(creationStmt == null) { throw new IllegalArgumentException("creation Statement, cannot be null: "
                    + tablename + ".create"); }
            try {
                conn.createStatement().executeUpdate(creationStmt);
            } catch(SQLException e) {
                logger.error("problem statement: " + creationStmt);
                SQLException sqle = new SQLException(e.getMessage() + " "
                        + creationStmt);
                sqle.setStackTrace(e.getStackTrace());
                throw sqle;
            }
            createIndices(tablename, conn, statements);
        }
    }

    private static void createIndices(String tablename,
                                      Connection conn,
                                      SQLLoader statements) throws SQLException {
        String stmt = "";
        try {
            String[] propNames = statements.getNamesForPrefix(tablename
                    + ".index");
            for(int i = 0; i < propNames.length; i++) {
                stmt = statements.get(propNames[i]);
                conn.createStatement().executeUpdate(stmt);
            }
        } catch(SQLException e) {
            logger.error("problem statement: " + stmt);
            SQLException sqle = new SQLException(e.getMessage() + " " + stmt);
            sqle.setStackTrace(e.getStackTrace());
            throw sqle;
        }
    }

    /**
     * Finds all PreparedStatement fields on tableObj's class and if an sql
     * statement is available in statements of the form tablename.fieldname a
     * prepared statement is created for it and assigned to the field
     */
    private static void prepareStatements(String tableName,
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

    public static final String TABLE_NAME = "tablename";

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TableSetup.class);
}