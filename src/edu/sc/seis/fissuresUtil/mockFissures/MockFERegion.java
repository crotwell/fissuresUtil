package edu.sc.seis.fissuresUtil.mockFissures;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.FlinnEngdahlType;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;

public class MockFERegion{
    public static FlinnEngdahlRegion create(){ return create(1); }
    
    public static FlinnEngdahlRegion create(int region){
        return new FlinnEngdahlRegionImpl(FlinnEngdahlType.from_int(1), region);
    }
}
