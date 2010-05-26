package edu.sc.seis.fissuresUtil.flow.querier;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.PointDistanceAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtilTest;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.extractor.model.LocationExtractor;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class EventFinderQueryTest extends TestCase {

    public void testMagnitudeRestricedQuery() {
        EventFinderQuery q = new EventFinderQuery();
        q.setMinMag(7);
        assertEquals(0, testDefaults(q));
        q.setMaxMag(3);
        assertEquals(0, testDefaults(q));
        q.setMinMag(3);
        q.setMaxMag(7);
        assertEquals(2, testDefaults(q));
    }

    private int testDefaults(EventFinderQuery q) {
        return test(q, MockEventAccessOperations.createEvents());
    }

    private int test(EventFinderQuery q, EventAccessOperations[] all) {
        int passed = 0;
        for(int i = 0; i < all.length; i++) {
            if(q.test(all[i]).passed()) {
                passed++;
            }
        }
        return passed;
    }

    public void testLocationRestrictingQuery() {
        EventFinderQuery q = new EventFinderQuery();
        q.setArea(new BoxAreaImpl(-1, 1, -1, 1));
        assertEquals(1, testDefaults(q));
        q.setArea(new PointDistanceAreaImpl(0,
                                            0,
                                            AreaUtilTest.ZERO,
                                            AreaUtilTest.TEN_DEG));
        assertEquals(1, testDefaults(q));
        q.setArea(new PointDistanceAreaImpl(0,
                                            0,
                                            AreaUtilTest.ZERO,
                                            new QuantityImpl(90,
                                                             UnitImpl.DEGREE)));
        assertEquals(2, testDefaults(q));
        q.setArea(new BoxAreaImpl(-2, -1, -180, 180));
        assertEquals(0, testDefaults(q));
    }

    public void testLocationAroundDateLine() {
        CacheEvent ev = MockEventAccessOperations.createEvent();
        new LocationExtractor().extract(ev).longitude = 179;
        EventFinderQuery q = new EventFinderQuery();
        q.setArea(new PointDistanceAreaImpl(0,
                                            179,
                                            AreaUtilTest.ZERO,
                                            AreaUtilTest.TEN_DEG));
        assertEquals(0, testDefaults(q));
        assertTrue(q.test(ev).passed());
    }

    public void testTimeQuery() {
        EventFinderQuery q = new EventFinderQuery();
        MicroSecondDate start = new MicroSecondDate(0);
        MicroSecondDate end = start.add(new TimeInterval(1, UnitImpl.DAY));
        q.setTime(new MicroSecondTimeRange(start, end));
        assertEquals(1, testDefaults(q));
        q.setTime(new MicroSecondTimeRange(end, end));
        assertEquals(0, testDefaults(q));
    }

    public void testDepthQuery() {
        EventFinderQuery q = new EventFinderQuery();
        assertEquals(2, testDefaults(q));
        q.setMinDepth(9);
        q.setMaxDepth(11);
        assertEquals(1, testDefaults(q));
        q.setMinDepth(q.getMaxDepth());
        assertEquals(0, testDefaults(q));
        q.setMinDepth(0);
        assertEquals(2, testDefaults(q));
    }
}
