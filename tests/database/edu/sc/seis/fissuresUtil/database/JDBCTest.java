package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import java.sql.Statement;
import junit.framework.TestCase;

/**
 * @author groves Created on Oct 28, 2004
 */
public abstract class JDBCTest extends TestCase {

    public void tearDown() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
    }
}