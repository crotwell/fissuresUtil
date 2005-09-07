package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

/**
 * @author groves Created on Oct 28, 2004
 */
public abstract class JDBCTearDown extends TestCase {
    public JDBCTearDown(){
    BasicConfigurator.configure();
    }

    public void tearDown() throws SQLException {
        ConnMgr.createConnection().createStatement().execute("SHUTDOWN");
    }
}