package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.mockFissures.MockFERegion;
import edu.sc.seis.fissuresUtil.mockFissures.IfParameterMgr.MockParameterRef;

public class MockEventAttr{
    public static EventAttr create(){ return create(1); }
    
    public static EventAttr create(int feRegion){
        return create("Test Event", feRegion);
    }
    
    public static EventAttr create(String name, int feRegion){
        return create(name, feRegion, MockParameterRef.params);
    }
    
    public static EventAttr create(String name, int feRegion,
                                            ParameterRef[] parms){
        return new EventAttrImpl(name, MockFERegion.create(feRegion), parms);
    }
    
    public static EventAttr createWallFallAttr(){
        return create("Fall of the Berlin Wall Event", 543);
    }
}
