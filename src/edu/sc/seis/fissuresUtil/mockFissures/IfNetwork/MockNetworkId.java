package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;

public class MockNetworkId{
    public static NetworkId createNetworkID(){
        NetworkId mockId = new NetworkId();
        mockId.network_code = "XX";
        mockId.begin_time = Defaults.EPOCH.getFissuresTime();
        return mockId;
    }

    public static NetworkId createOtherNetworkID(){
        NetworkId mockId = new NetworkId();
        mockId.network_code = "OTHERCODE";
        mockId.begin_time = Defaults.WALL_FALL.getFissuresTime();
        return mockId;
    }
}
