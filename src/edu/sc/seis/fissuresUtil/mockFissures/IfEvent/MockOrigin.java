package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfParameterMgr.MockParameterRef;

public class MockOrigin{
    public static Origin create(){
        return create(Defaults.EPOCH, MockMagnitude.MAGS);
    }
    
    public static Origin create(MicroSecondDate time, Magnitude[] mags){
        return new OriginImpl("Epoch in Central Alaska", "Test Data",
                              "Charlie Groves", time.getFissuresTime(),
                              MockLocation.SIMPLE, mags, MockParameterRef.params);
    }

    public static Origin createWallFallOrigin(){
        return new OriginImpl("Fall of the Berlin Wall", "Test Data",
                              "Charlie Groves", Defaults.WALL_FALL.getFissuresTime(),
                              MockLocation.BERLIN, MockMagnitude.MAGS,
                              MockParameterRef.params);
    }
    
    public static Origin[] createOrigins(){
        Origin[] origins = new Origin[2];
        origins[0] = create();
        origins[1] = createWallFallOrigin();
        return origins;
    }
}
