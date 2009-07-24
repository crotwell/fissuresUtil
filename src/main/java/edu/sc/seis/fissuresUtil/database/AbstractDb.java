package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class acts an abstract class for database Operations. AbstractDb.java
 * Created: Fri Feb 7 10:45:30 2003
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public abstract class AbstractDb {

    public AbstractDb(String directoryName, String databaseName) {
        this.directoryName = directoryName;
        this.databaseName = databaseName;
    }

    public Connection getConnection() {
        try {
            if(connection == null) {
                String driverName = new String("org.hsqldb.jdbcDriver");
                Class.forName(driverName).newInstance();
                connection = DriverManager.getConnection("jdbc:hsqldb:"
                        + directoryName + "/" + databaseName, "sa", "");
            }
            return connection;
        } catch(Exception sqle) {
            sqle.printStackTrace();
            return null;
        }
    }

    public abstract void create() throws SQLException;

    protected Connection connection;

    protected String databaseName;

    protected String directoryName;
}// AbstractDb
