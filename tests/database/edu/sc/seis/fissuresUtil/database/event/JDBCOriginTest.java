package edu.sc.seis.fissuresUtil.database.event;


import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;

public class JDBCOriginTest extends TestCase {
    public JDBCOriginTest(String testname) throws SQLException{
        super(testname);
        originTable = new JDBCOrigin(ConnMgr.createConnection());
    }

    public void testPutGet() throws SQLException, NotFound {
        Origin[] origins = MockOrigin.createOrigins();
        for (int i = 0; i < origins.length; i++) {
            int dbid = originTable.put(origins[i]);
            Origin extracted = originTable.get(dbid);
            assertEquals(origins[i].get_id(), extracted.get_id());
            for (int j = 0; j < origins[i].magnitudes.length; j++) {
                assertTrue(JDBCMagnitudeTest.areEqual(origins[i].magnitudes[j],
                                                      extracted.magnitudes[j]));
            }
            assertEquals(new MicroSecondDate(origins[i].origin_time),
                         new MicroSecondDate(extracted.origin_time));
        }
    }

    public void testDoubleInsert() throws SQLException{
        Origin[] origins = MockOrigin.createOrigins();
        for (int i = 0; i < origins.length; i++) {
            int dbidA = originTable.put(origins[i]);
            int dbidB = originTable.put(origins[i]);
            assertEquals(dbidA, dbidB);
        }
    }

    public void testWithEvents() throws SQLException, NoPreferredOrigin,NotFound{
        EventAccessOperations[] evs = MockEventAccessOperations.createEvents();
        for (int i = 0; i < evs.length; i++) {
            int dbidA = originTable.put(evs[i].get_preferred_origin());
            int dbidB = originTable.getDBId(evs[i].get_preferred_origin());
            assertEquals(dbidA, dbidB);
        }
        evs = MockEventAccessOperations.createEvents();
        for (int i = 0; i < evs.length; i++) {
            int dbidA = originTable.put(evs[i].get_preferred_origin());
            int dbidB = originTable.getDBId(evs[i].get_preferred_origin());
            assertEquals(dbidA, dbidB);
        }
    }

    public void testGetDBId()throws SQLException, NotFound{
        Origin[] origins = MockOrigin.createOrigins();
        for (int i = 0; i < origins.length; i++) {
            int dbid = originTable.put(origins[i]);
            assertEquals(dbid, originTable.getDBId(origins[i]));
        }
    }

    protected JDBCOrigin originTable;
}

