package edu.sc.seis.fissuresUtil.flow.querier;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAccessSeqHolder;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIter;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;

public class EventFinderIterator implements Iterator {

    /**
     * Returns an iterator over all the events in the finder
     */
    public static EventFinderIterator create(EventFinder ef) {
        return create(ef, new EventFinderQuery());
    }

    /**
     * Returns an iterator over the events in the finder returned by the given
     * query
     */
    public static EventFinderIterator create(EventFinder ef, EventFinderQuery eq) {
        return create(ef, eq, 100);
    }

    /**
     * Returns an iterator over the events in the finder returned by the given
     * query with eventsPerServerCall events returned per call to the server
     */
    public static EventFinderIterator create(EventFinder ef,
                                             EventFinderQuery eq,
                                             int eventsPerServerCall) {
        EventSeqIterHolder holder = new EventSeqIterHolder();
        EventAccessOperations[] events = getEvents(eq,
                                                   ef,
                                                   holder,
                                                   eventsPerServerCall);
        return new EventFinderIterator(events,
                                       holder.value,
                                       eventsPerServerCall);
    }

    public static EventAccessOperations[] getEvents(EventFinderQuery q,
                                                    EventFinder finder,
                                                    EventSeqIterHolder holder,
                                                    int eventsPerServerCall) {
        return finder.query_events(q.getArea(),
                                   q.getMinDepthQuantity(),
                                   q.getMaxDepthQuantity(),
                                   q.getTime().getFissuresTimeRange(),
                                   q.getTypes(),
                                   q.getMinMag(),
                                   q.getMaxMag(),
                                   q.getCatalogs(),
                                   q.getContributors(),
                                   eventsPerServerCall,
                                   holder);
    }

    public EventFinderIterator(EventAccessOperations[] initialEvents,
                               EventSeqIter iter,
                               int eventsPerNext) {
        this.eventsPerNext = eventsPerNext;
        logger.debug("got " + initialEvents.length
                + " events from initial query");
        this.currentEvents = initialEvents;
        this.iter = iter;
    }

    public boolean hasNext() {
        boolean result = positionInCurrent < currentEvents.length
                || eventsInIter();
        if(!result && iter != null) {
            iter.destroy();
        }
        return result;
    }

    public Object next() {
        if(positionInCurrent >= currentEvents.length) {
            if(eventsInIter()) {
                EventAccessSeqHolder eHolder = new EventAccessSeqHolder();
                iter.next_n(eventsPerNext, eHolder);
                currentEvents = eHolder.value;
                positionInCurrent = 0;
            } else {
                throw new NoSuchElementException("No more items in event iterator.  Call hasNext() before calling next()");
            }
        }
        return currentEvents[positionInCurrent++];
    }

    private boolean eventsInIter() {
        return iter != null && iter.how_many_remain() > 0;
    }

    public void remove() {
        throw new UnsupportedOperationException("Can't remove events returned from server");
    }

    private EventAccessOperations[] currentEvents;

    private int positionInCurrent;

    private final int eventsPerNext;

    private EventSeqIter iter;

    private static final Logger logger = LoggerFactory.getLogger(EventFinderIterator.class);
}
