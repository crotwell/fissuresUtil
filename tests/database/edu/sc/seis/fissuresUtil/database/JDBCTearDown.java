package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.fissuresUtil.database.util.TableSetup;

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
        TableSetup.clearCreatedTableList();
    }
}
