package edu.sc.seis.fissuresUtil.database.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author groves Created on Nov 24, 2004
 */
public class TableSetup {

    public static void setup(String tableName,
                             Connection conn,
                             Object tableObj,
                             String propFile) throws Exception {
        setup(tableName, conn, tableObj, propFile, new VelocityContext());
    }

    public static void setup(String tableName,
                             Connection conn,
                             Object tableObj,
                             String propFile,
                             Context ctx) throws Exception {
        ctx.put("tablename", tableName);
        SQLLoader sql = new SQLLoader(propFile, ctx);
        TableSetup.customSetup(tableName, conn, tableObj, sql);
    }

    public static void customSetup(String tablename,
                                   Connection conn,
                                   Object tableObj,
                                   SQLLoader statements) throws SQLException {
        // create seq before table in case of table dependency
        createSequence(tablename, conn, tableObj, statements);
        createTable(tablename, conn, statements);
        prepareStatements(tablename, conn, tableObj, statements);
    }

    private static void createTable(String tablename,
                                    Connection conn,
                                    SQLLoader statements) throws SQLException {
        if(!DBUtil.tableExists(tablename, conn)) {
            String creationStmt = statements.get(tablename + ".create");
            if(creationStmt == null) { throw new IllegalArgumentException("creation Statement, cannot be null."); }
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
     * creates JDBCSequences for fields named tableName+"Seq" using the property
     * tableName+"Seq.create" and tableName+"Seq.nextVal"
     */
    public static void createSequence(String tableName,
                                      Connection conn,
                                      Object tableObj,
                                      SQLLoader statements) throws SQLException {
        try {
            Field field = tableObj.getClass().getDeclaredField(tableName
                    + "Seq");
            String key = tableName + "Seq.create";
            if(field != null) {
                if(statements.has(key)) {
                    String sql = statements.get(key);
                    field.setAccessible(true);
                    field.set(tableObj, new JDBCSequence(conn, tableName
                            + "Seq"));
                } else {
                    throw new IllegalArgumentException(tableName
                            + "Seq.create is not defined, unable to create sequence");
                }
            }
        } catch(NoSuchFieldException e) {
            logger.info("No Sequence field named: " + tableName + "Seq found.");
            Field[] fields = tableObj.getClass().getDeclaredFields();
            for(int i = 0; i < fields.length; i++) {
                logger.info("Field in " + tableName + " " + fields[i].getName());
            }
            // no field following the naming convention, so skip
        } catch(IllegalArgumentException e) {
            GlobalExceptionHandler.handle("Thought this couldn't happen since I checked the object type.",
                                          e);
        } catch(IllegalAccessException e) {
            GlobalExceptionHandler.handle("Thought this couldn't happen since I called setAccessible.  Looks like I was wrong",
                                          e);
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TableSetup.class);
}