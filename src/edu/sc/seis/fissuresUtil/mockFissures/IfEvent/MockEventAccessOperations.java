package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
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

    public synchronized static EventAccessOperations[] createEvents() {
        evs = new EventAccessOperations[2];
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

    public static CacheEvent[] createEventTimeRange() {
        Time t = new Time("20010101T000000.000Z", 0);
        MicroSecondTimeRange tr = new MicroSecondTimeRange(new MicroSecondDate(t),
                                                           new TimeInterval(30,
                                                                            UnitImpl.DAY));
        return createEvents(tr, 120);
    }

    public static CacheEvent[] createEvents(MicroSecondTimeRange timeRange,
                                            int numEvents) {
        TimeInterval timeBetweenEvents = (TimeInterval)timeRange.getInterval()
                .divideBy(numEvents);
        float latStep = 180 / (float)(numEvents/2);
        float lonStep = 360 / (float)(numEvents/2);
        CacheEvent[] events = new CacheEvent[numEvents];
        for(int i = 0; i < numEvents/2; i++) {
            MicroSecondDate eventBegin = timeRange.getBeginTime()
                    .add((TimeInterval)timeBetweenEvents.multiplyBy(i));
            Origin o = new OriginImpl("Mock Event " + i,
                                      "Mockalog",
                                      "Charlie Groves",
                                      eventBegin.getFissuresTime(),
                                      MockLocation.create(90 - i * latStep,
                                                          -180 + i * lonStep),
                                      MockMagnitude.MAGS,
                                      MockParameterRef.params);
            EventAttr ea = MockEventAttr.create();
            events[i] = new CacheEvent(ea, o);
        }
        for(int i =numEvents/2; i < numEvents; i++) {
            MicroSecondDate eventBegin = timeRange.getBeginTime()
                    .add((TimeInterval)timeBetweenEvents.multiplyBy(i));
            int shift = i - numEvents/2;
            Origin o = new OriginImpl("Mock Event " + i,
                                      "Mockalog",
                                      "Charlie Groves",
                                      eventBegin.getFissuresTime(),
                                      MockLocation.create(90 - shift * latStep,
                                                          180 - shift * lonStep),
                                      MockMagnitude.MAGS,
                                      MockParameterRef.params);
            EventAttr ea = MockEventAttr.create();
            events[i] = new CacheEvent(ea, o);
        }
        return events;
    }

    private static EventAccessOperations[] evs;
}