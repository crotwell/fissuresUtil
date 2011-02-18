package edu.sc.seis.fissuresUtil.flow.querier;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventFinder;

public class EventFinderIteratorTest extends TestCase {


    public void testAllInInitial() {
        runQuery(new EventFinderQuery(), 100);
    }

    public void testWithSubIterations() {
        runQuery(new EventFinderQuery(), 1);
    }

    public void testCallNextTooManyTimes() {
        Iterator it = runQuery(new EventFinderQuery(), 100);
        try {
            it.next();
            assertTrue("An exception should be thrown by next", false);
        } catch(NoSuchElementException e) {
            assertTrue("Need to reach this spot", true);
        }
    }

    private Iterator runQuery(EventFinderQuery efq, int eventsPerServerCall) {
        EventAccessOperations[] ea = MockEventAccessOperations.createEvents();
        Iterator it = EventFinderIterator.create(new MockEventFinder(ea),
                                                 efq,
                                                 eventsPerServerCall);
        int count = 0;
        for(; it.hasNext(); count++) {
            it.next();
        }
        assertEquals(ea.length, count);
        return it;
    }
}
