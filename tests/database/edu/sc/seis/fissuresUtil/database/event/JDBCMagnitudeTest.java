package edu.sc.seis.fissuresUtil.database.event;


import java.sql.Connection;
import java.sql.SQLException;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;
public class JDBCMagnitudeTest extends JDBCTearDown {

    protected void setUp() throws SQLException {
        Connection conn = ConnMgr.createConnection();
        JDBCContributor jdbcContributor = new JDBCContributor(conn);
        jdbcMagnitude = new JDBCMagnitude(conn, jdbcContributor);
    }

    public void testPutAndGet() throws NotFound, SQLException {
        int originId = 100;
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 0; i < mags.length; i++) {
            jdbcMagnitude.put(mags[i], originId);
        }
        Magnitude[] outMag = jdbcMagnitude.get(originId);
        for (int i = 0; i < outMag.length; i++) {
            assertTrue(areEqual(mags[i], outMag[i]));
        }
    }

    public void testDoubleInsert() throws SQLException, NotFound {
        Magnitude[] mags = MockMagnitude.MAGS;
        for (int i = 1; i < mags.length; i++) {
            int origingDbId = i+10;
            jdbcMagnitude.put(mags[i], origingDbId);
            jdbcMagnitude.put(mags[i], origingDbId);
            assertEquals(1, jdbcMagnitude.get(origingDbId).length);
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
