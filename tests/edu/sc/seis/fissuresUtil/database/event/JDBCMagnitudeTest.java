package edu.sc.seis.fissuresUtil.database.event;


import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfEvent.MockMagnitude;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
public class JDBCMagnitudeTest extends TestCase {
    public JDBCMagnitudeTest(String testname) { super(testname); }
    
    protected void setUp() throws SQLException {
        Connection conn = ConnMgr.getConnection();
        JDBCContributor jdbcContributor = new JDBCContributor(conn);
        jdbcMagnitude = new JDBCMagnitude(conn, jdbcContributor);
    }
    
    public void testPutAndGet() throws NotFound, SQLException {
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 0; i < mags.length; i++) {
            int dbid = jdbcMagnitude.put(mags[i]);
            Magnitude outMag = jdbcMagnitude.get(dbid);
            assertTrue(areEqual(mags[i], outMag));
        }
    }
    
    public void testDoubleInsert() throws SQLException {
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 0; i < mags.length; i++) {
            int dbidA = jdbcMagnitude.put(mags[i]);
            int dbidB = jdbcMagnitude.put(mags[i]);
            assertEquals(dbidA, dbidB);
        }
    }
    
    public void testGetDBID() throws SQLException, NotFound{
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 0; i < mags.length; i++) {
            int dbid = jdbcMagnitude.put(mags[i]);
            int gottenID = jdbcMagnitude.getDBId(mags[i]);
            assertEquals(dbid, gottenID);
        }
    }
    
    public static boolean areEqual(Magnitude first, Magnitude second){
        if(first.contributor.equals(second.contributor) &&
           first.type.equals(second.type) &&
           first.value == second.value) return true;
        return false;
    }
    
    protected JDBCMagnitude jdbcMagnitude;
}
