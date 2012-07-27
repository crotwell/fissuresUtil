package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DBUtil {

    public static String commaDelimit(int numQuestionMarks) {
        List qs = new ArrayList(numQuestionMarks);
        for(int i = 0; i < numQuestionMarks; i++) {
            qs.add("?");
        }
        return commaDelimit(qs);
    }

    public static String commaDelimit(Collection strings) {
        String commaDelimited = "";
        Iterator it = strings.iterator();
        boolean first = true;
        while(it.hasNext()) {
            if(first) {
                first = false;
            } else {
                commaDelimited += ", ";
            }
            commaDelimited += it.next();
        }
        return commaDelimited;
    }

    public static boolean tableExists(String tableName, Connection conn)
            throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet rs = dbmd.getTables(null, null, null, null);
        while(rs.next()) {
            if(rs.getString("TABLE_NAME").equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean sequenceExists(String seqName, Connection conn)
            throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        if(dbmd.getURL().startsWith("jdbc:postgresql:")) {
            return returnsAnyRows("SELECT * from pg_class where relname = '"
                    + seqName.toLowerCase() + "' AND relkind = 'S'", conn);
        } else if(dbmd.getURL().startsWith("jdbc:hsqldb:")) {
            // try select from system_sequences
            String whereClause = "WHERE sequence_name = '"
                    + seqName.toUpperCase() + "'";
            try {
                return returnsAnyRows("SELECT sequence_name FROM system_sequences "
                                              + whereClause,
                                      conn);
            } catch(SQLException e) {
                // Try again as this may be a table not found exception.
                // Under HSQLDB 1.8 and later the system_sequences table
                // belongs to a schema
                return returnsAnyRows("SELECT sequence_name FROM information_schema.system_sequences "
                                              + whereClause,
                                      conn);
            }
        }
        // not postgres, so try asking as a table, maybe this works for other
        // databases
        return tableExists(seqName, conn);
    }

    private static boolean returnsAnyRows(String stmt, Connection conn)
            throws SQLException {
        return conn.createStatement().executeQuery(stmt).next();
    }

    public static void printTableExistence(String name, Connection conn)
            throws SQLException {
        if(tableExists(name, conn))
            System.out.println(name + " exists");
        else
            System.out.println(name + " does not exist");
    }

    public static void printExistingTables(Connection conn) throws SQLException {
        DatabaseMetaData dbmd = conn.getMetaData();
        System.out.println("********************************************");
        ResultSet rs = dbmd.getTables(null, null, null, null);
        while(rs.next()) {
            System.out.println(rs.getString("TABLE_NAME"));
        }
        System.out.println("********************************************");
    }
}
