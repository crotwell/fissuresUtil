package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class MockStation{

    public static Station createStation(){
        return new StationImpl(MockStationId.createStationId(), "Test Station",
                               MockLocation.SIMPLE, "Joe", "this is a test",
                               "still, a test", MockNetworkAttr.createNetworkAttr());
    }

    public static Station createOtherStation(){
        return new StationImpl(MockStationId.createOtherStationId(), "Noitats tset",
                               MockLocation.BERLIN, "Frank", "tset a si siht",
                               "tset a ,llits", MockNetworkAttr.createOtherNetworkAttr());
    }

}
