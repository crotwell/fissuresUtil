package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class MockEventAccessOperations{
    public synchronized static EventAccessOperations[] createEvents(){
        evs = new EventAccessOperations[2];
        evs[0] = createEvent();
        evs[1] = createFallEvent();
        return evs;
    }

    public static CacheEvent createEvent(){
        return createEvent(MockOrigin.create(), MockEventAttr.create());
    }

    public static CacheEvent createFallEvent(){
        return createEvent(MockOrigin.createWallFallOrigin(),
                           MockEventAttr.createWallFallAttr());
    }

    public static CacheEvent createEvent(MicroSecondDate eventTime,
                                                    int magnitudeAndDepth,
                                                    int feRegion){
        Magnitude[] mags = {new Magnitude("test", magnitudeAndDepth, "another") };
        return createEvent(MockOrigin.create(eventTime, mags),
                           MockEventAttr.create(feRegion));
    }

    public static CacheEvent createEvent(Origin origin, EventAttr attr){
        Origin[] origins = { origin};
        return new CacheEvent(attr, origins, origins[0]);
    }

    private static EventAccessOperations[] evs;
}
