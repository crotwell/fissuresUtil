package edu.sc.seis.fissuresUtil.database.event;

import java.sql.SQLException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.PointDistanceAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class JDBCEventAccessTest extends JDBCTearDown {

    public void setUp() throws SQLException {
        this.eventTable = new JDBCEventAccess(ConnMgr.createConnection());
    }

    public void tearDown() throws SQLException {
        super.tearDown();
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
        try {
            eventTable.getDBId(caches[0]);
            fail("Not found should've been thrown");
        } catch(NotFound e) {
            assertTrue(true);
        }
        for(int i = 0; i < events.length; i++) {
            int dbid = put(events[i], i);
            assertEquals(caches[i], eventTable.getEvent(dbid));
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

    public void testAllInclusiveQuery() throws SQLException {
        assertEquals(populateDefaults().length,
                     eventTable.query(new EventFinderQuery()).length);
    }

    private EventAccessOperations[] populateDefaults() throws SQLException {
        EventAccessOperations[] events = MockEventAccessOperations.createEvents();
        for(int i = 0; i < events.length; i++) {
            put(events[i], i);
        }
        return events;
    }

    public void testMagnitudeRestricedQuery() throws SQLException {
        EventAccessOperations[] events = populateDefaults();
        EventFinderQuery q = new EventFinderQuery();
        q.setMinMag(7);
        assertEquals(0, eventTable.query(q).length);
        populateDefaults();
        q.setMaxMag(3);
        assertEquals(0, eventTable.query(q).length);
        q.setMinMag(3);
        q.setMaxMag(7);
        assertEquals(events.length, eventTable.query(q).length);
    }

    public void testLocationRestrictingQuery() throws SQLException {
        populateDefaults();
        EventFinderQuery q = new EventFinderQuery();
        q.setArea(new BoxAreaImpl(-1, 1, -1, 1));
        assertEquals(1, eventTable.query(q).length);
        q.setArea(new PointDistanceAreaImpl(0,
                                            0,
                                            ZERO,
                                            TEN_DEG));
        assertEquals(1, eventTable.query(q).length);
        q.setArea(new PointDistanceAreaImpl(0,
                                            0,
                                            ZERO,
                                            new QuantityImpl(90,
                                                             UnitImpl.DEGREE)));
        assertEquals(2, eventTable.query(q).length);
        q.setArea(new BoxAreaImpl(-2, -1, -180, 180));
        assertEquals(0, eventTable.query(q).length);
    }

    public void testLocationAroundDateLine() throws SQLException {
        populateDefaults();
        CacheEvent ev = MockEventAccessOperations.createEvent();
        EventUtil.extractOrigin(ev).my_location.longitude = 179;
        put(ev, 2);
        EventFinderQuery q = new EventFinderQuery();
        q.setArea(new PointDistanceAreaImpl(0,
                                            179,
                                            ZERO,
                                            TEN_DEG));
        assertEquals(1, eventTable.query(q).length);
    }

    public void testTimeQuery() throws SQLException {
        populateDefaults();
        EventFinderQuery q = new EventFinderQuery();
        MicroSecondDate start = new MicroSecondDate(0);
        MicroSecondDate end = start.add(new TimeInterval(1, UnitImpl.DAY));
        q.setTime(new MicroSecondTimeRange(start, end));
        assertEquals(1, eventTable.query(q).length);
        q.setTime(new MicroSecondTimeRange(end, end));
        assertEquals(0, eventTable.query(q).length);
    }

    public void testDepthQuery() throws SQLException {
        EventAccessOperations[] events = populateDefaults();
        EventFinderQuery q = new EventFinderQuery();
        assertEquals(events.length, eventTable.query(q).length);
        q.setMinDepth(9);
        q.setMaxDepth(11);
        assertEquals(1, eventTable.query(q).length);
        q.setMinDepth(q.getMaxDepth());
        assertEquals(0, eventTable.query(q).length);
        q.setMinDepth(0);
        assertEquals(events.length, eventTable.query(q).length);
    }

    private int put(EventAccessOperations event, int i) throws SQLException {
        return eventTable.put(event, "event" + i, "localhost", "test/dns");
    }

    private JDBCEventAccess eventTable;

    public static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.DEGREE);

    public static final QuantityImpl TEN_DEG = new QuantityImpl(10,
                                                                UnitImpl.DEGREE);
}