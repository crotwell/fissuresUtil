package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;

/**
 * @author groves Created on Oct 28, 2004
 */
public abstract class JDBCTearDown extends TestCase {

    public void tearDown() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
    }
}