package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.Location;
import edu.sc.seis.fissuresUtil.database.event.JDBCLocation;
import edu.sc.seis.mockFissures.MockLocation;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCLocationTest extends TestCase {
    public JDBCLocationTest(String testname) { super(testname); }
    
    protected void setUp() throws SQLException {
        Connection conn = ConnMgr.createConnection();
        jdbcLocation = new JDBCLocation(conn);
    }
    
    public void testPutAndGet() throws SQLException, NotFound {
        Location[] locs = MockLocation.createMultiple();
        for (int i = 0; i < locs.length; i++) {
            int dbid = jdbcLocation.put(locs[i]);
            Location gottenLoc = jdbcLocation.get(dbid);
            assertEquals(locs[i].depth, gottenLoc.depth);
            assertEquals(locs[i].elevation, gottenLoc.elevation);
            assertEquals(locs[i].latitude, gottenLoc.latitude, 0.001f);
            assertEquals(locs[i].longitude, gottenLoc.longitude, 0.001f);
        }
    }
    
    public void testGetDBId() throws NotFound, SQLException {
        Location[] locs = MockLocation.createMultiple();
        for (int i = 0; i < locs.length; i++) {
            int dbid = jdbcLocation.put(locs[i]);
            int gottenid = jdbcLocation.getDBId(locs[i]);
            assertEquals(dbid, gottenid);
        }
    }
    
    public void testDoubleInsert() throws SQLException {
        Location[] locs = MockLocation.createMultiple();
        for (int i = 0; i < locs.length; i++) {
            int dbidA = jdbcLocation.put(locs[i]);
            int dbidB = jdbcLocation.put(locs[i]);
            assertEquals(dbidA, dbidB);
        }
    }
    
    protected JDBCLocation jdbcLocation;
}
