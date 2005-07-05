package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfParameterMgr.MockParameterRef;

public class MockEventAccessOperations {

    public synchronized static CacheEvent[] createEvents() {
        evs = new CacheEvent[2];
        evs[0] = createEvent();
        evs[1] = createFallEvent();
        return evs;
    }

    public static CacheEvent createEvent() {
        return createEvent(MockOrigin.create(), MockEventAttr.create());
    }

    public static CacheEvent createFallEvent() {
        return createEvent(MockOrigin.createWallFallOrigin(),
                           MockEventAttr.createWallFallAttr());
    }

    public static CacheEvent createEvent(MicroSecondDate eventTime,
                                         int magnitudeAndDepth,
                                         int feRegion) {
        Magnitude[] mags = {new Magnitude("test", magnitudeAndDepth, "another")};
        return createEvent(MockOrigin.create(eventTime, mags),
                           MockEventAttr.create(feRegion));
    }

    public static CacheEvent createEvent(Origin origin, EventAttr attr) {
        Origin[] origins = {origin};
        return new CacheEvent(attr, origins, origins[0]);
    }

    /**
     * @return 18 events evenly spaced in time from the 1st of January 2001 to
     *         the 31st of that month and evenly spaced over the entire globe
     */
    public static CacheEvent[] createEventTimeRange() {
        Time t = new Time("20010101T000000.000Z", 0);
        MicroSecondTimeRange tr = new MicroSecondTimeRange(new MicroSecondDate(t),
                                                           new TimeInterval(30,
                                                                            UnitImpl.DAY));
        return createEvents(tr, 3, 6);
    }

    public static CacheEvent[] createEvents(MicroSecondTimeRange timeRange,
                                            int rows,
                                            int cols) {
        int numEvents = rows * cols;
        TimeInterval timeBetweenEvents = (TimeInterval)timeRange.getInterval()
                .divideBy(numEvents);
        CacheEvent[] events = new CacheEvent[numEvents];
        Location[] locs = MockLocation.create(rows, cols);
        Magnitude[][] mags = MockMagnitude.getMagnitudes(4.0f, 10.0f, numEvents);
        for(int i = 0; i < numEvents; i++) {
            MicroSecondDate eventBegin = timeRange.getBeginTime()
                    .add((TimeInterval)timeBetweenEvents.multiplyBy(i));
            Origin o = new OriginImpl("Mock Event " + i,
                                      "Mockalog",
                                      "Charlie Groves",
                                      eventBegin.getFissuresTime(),
                                      locs[i],
                                      mags[i],
                                      MockParameterRef.params);
            EventAttr ea = MockEventAttr.create();
            events[i] = new CacheEvent(ea, o);
        }
        return events;
    }

    private static CacheEvent[] evs;
}