package edu.sc.seis.fissuresUtil.database.event;


import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import java.io.IOException;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCEventAccessTest extends TestCase{
    public JDBCEventAccessTest(String name) throws SQLException{
        super(name);
        this.eventTable = new JDBCEventAccess(ConnMgr.createConnection());
    }
    
    public void testGetAll() throws SQLException, NotFound{
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for (int i = 0; i < events.length; i++){
            int dbid= put(events[i], i);
            eventTable.getEvent(dbid);
            put(events[i], i);
        }
        assertEquals(events.length, eventTable.getAllEvents().length);
    }
    
    public void testPut() throws SQLException, NotFound {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for (int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            assertEquals(events[i], eventTable.getEvent(dbid));
            assertEquals("event"+i, eventTable.getIOR(dbid));
            assertEquals("localhost", eventTable.getServer(dbid));
            assertEquals("test/dns", eventTable.getDNS(dbid));
        }
    }
    
    public void testDoublePut() throws SQLException, NotFound{
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for (int i = 0; i < events.length; i++) {
            int dbidA = put(events[i], i);
            int dbidB = put(events[i], i);
            int gottenId = eventTable.getDBId(events[i]);
            assertEquals(dbidA, dbidB);
            assertEquals(dbidB, gottenId);
        }
    }
    
    public void testGetDBId() throws SQLException, NotFound{
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for (int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            assertEquals(dbid, eventTable.getDBId(events[i]));
        }
    }
    
    private int put(EventAccessOperations event, int i) throws SQLException{
        return eventTable.put(event, "event"+i, "localhost", "test/dns");
    }
    
    private JDBCEventAccess eventTable;
}
