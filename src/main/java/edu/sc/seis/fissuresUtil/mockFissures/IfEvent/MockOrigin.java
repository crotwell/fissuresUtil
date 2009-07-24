package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfParameterMgr.MockParameterRef;

public class MockOrigin {

    public static OriginImpl create() {
        return create(Defaults.EPOCH, MockMagnitude.createMagnitudes());
    }

    public static OriginImpl create(MicroSecondDate time, Magnitude[] mags) {
        return new OriginImpl("Epoch in Central Alaska",
                              "Test Data",
                              "Charlie Groves",
                              time.getFissuresTime(),
                              MockLocation.create(),
                              mags,
                              MockParameterRef.createParams());
    }

    public static OriginImpl createWallFallOrigin() {
        return new OriginImpl("Fall of the Berlin Wall",
                              "Test Data",
                              "Charlie Groves",
                              Defaults.WALL_FALL.getFissuresTime(),
                              MockLocation.createBerlin(),
                              MockMagnitude.createMagnitudes(),
                              MockParameterRef.createParams());
    }

    public static OriginImpl[] createOrigins() {
        OriginImpl[] origins = new OriginImpl[2];
        origins[0] = create();
        origins[1] = createWallFallOrigin();
        return origins;
    }

    public static OriginImpl createOrigin() {
        return create();
    }
    

    public static OriginImpl[] createOrigins(int num) {
        OriginImpl[] out = new OriginImpl[num];
        for(int i = 0; i < out.length; i++) {
            out[i] = create(ClockUtil.now(), MockMagnitude.createMagnitudes());
        }
        return out;
    }
}
