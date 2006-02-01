package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;

/**
 * @author groves Created on Oct 28, 2004
 */
public abstract class JDBCTearDown extends TestCase {
    public JDBCTearDown() {
        Logger.getRootLogger().setLevel(Level.INFO);
        BasicConfigurator.configure();
    }

    public void tearDown() throws SQLException {
        cleanupDB();
    }

    public static void cleanupDB() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
        JDBCEventAccess.emptyCache();
        JDBCStation.emptyCache();
    }
}
