package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.StationId;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;

public class MockStationId{
    //this station is used as station in MockSite and Channel.  
    //It has one site and three channels
    public static StationId createStationId(){
        return new StationId(MockNetworkId.createNetworkID(), "STTN",
                             Defaults.EPOCH.getFissuresTime());
    }

   
    //this station is used as other station in MockSite and Channel as well.  
    //It has one site and one channel
    public static StationId createOtherStationId(){
        return new StationId(MockNetworkId.createOtherNetworkID(), "NTTS",
                             Defaults.WALL_FALL.getFissuresTime());
    }
}
