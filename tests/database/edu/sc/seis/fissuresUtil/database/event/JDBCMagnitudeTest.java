package edu.sc.seis.fissuresUtil.database.event;


import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;
public class JDBCMagnitudeTest extends TestCase {
    public JDBCMagnitudeTest(String testname) { super(testname); }

    protected void setUp() throws SQLException {
        Connection conn = ConnMgr.createConnection();
        JDBCContributor jdbcContributor = new JDBCContributor(conn);
        jdbcMagnitude = new JDBCMagnitude(conn, jdbcContributor);
    }

    public void testPutAndGet() throws NotFound, SQLException {
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 0; i < mags.length; i++) {
            jdbcMagnitude.put(mags[i], 0);
        }
        Magnitude[] outMag = jdbcMagnitude.get(0);
        for (int i = 0; i < outMag.length; i++) {
            assertTrue(areEqual(mags[i], outMag[i]));
        }
    }

    public void testDoubleInsert() throws SQLException, NotFound {
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 1; i < mags.length; i++) {
            jdbcMagnitude.put(mags[i], i);
            jdbcMagnitude.put(mags[i], i);
            assertEquals(1, jdbcMagnitude.get(i).length);
        }
    }
    
    public void tearDown() throws SQLException {
        jdbcMagnitude.clean();
    }

    public static boolean areEqual(Magnitude first, Magnitude second){
        if(first.contributor.equals(second.contributor) &&
           first.type.equals(second.type) &&
           first.value == second.value) return true;
        return false;
    }

    protected JDBCMagnitude jdbcMagnitude;
}
