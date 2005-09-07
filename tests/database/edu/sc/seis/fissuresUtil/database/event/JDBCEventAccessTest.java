package edu.sc.seis.fissuresUtil.database.event;

import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class JDBCEventAccessTest extends JDBCTearDown {

    public void setUp() throws SQLException {
        this.eventTable = new JDBCEventAccess(ConnMgr.createConnection());
    }

    public void tearDown() throws SQLException {
        super.tearDown();
        JDBCEventAccess.emptyCache();
    }

    public void testGetAll() throws SQLException, NotFound {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for(int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            eventTable.getEvent(dbid);
            put(events[i], i);
        }
        assertEquals(events.length, eventTable.getAllEvents().length);
    }

    public void testPut() throws SQLException, NotFound {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        CacheEvent[] caches = new CacheEvent[events.length];
        for(int i = 0; i < events.length; i++) {
            caches[i] = new CacheEvent(events[i]);
        }
        for(int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            assertEquals(caches[i], eventTable.getEvent(dbid));
            assertEquals(caches[i].hashCode(), eventTable.getEvent(dbid)
                    .hashCode());
            assertEquals("event" + i, eventTable.getIOR(dbid));
            assertEquals("localhost", eventTable.getServer(dbid));
            assertEquals("test/dns", eventTable.getDNS(dbid));
        }
    }

    public void testDoublePut() throws SQLException, NotFound {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for(int i = 0; i < events.length; i++) {
            int dbidA = put(events[i], i);
            int dbidB = put(events[i], i);
            int gottenId = eventTable.getDBId(events[i]);
            assertEquals(dbidA, dbidB);
            assertEquals(dbidB, gottenId);
        }
    }

    public void testGetDBId() throws SQLException, NotFound {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for(int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            assertEquals(dbid, eventTable.getDBId(events[i]));
        }
    }

    private int put(EventAccessOperations event, int i) throws SQLException {
        return eventTable.put(event, "event" + i, "localhost", "test/dns");
    }

    private JDBCEventAccess eventTable;
}