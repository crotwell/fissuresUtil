package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.StationId;
import edu.sc.seis.fissuresUtil.mockFissures.Defaults;

public class MockStationId {

    //this station is used as station in MockSite and Channel.
    //It has one site and three channels
    public static StationId createStationId() {
        return new StationId(MockNetworkId.createNetworkID(),
                             "STTN",
                             Defaults.EPOCH.getFissuresTime());
    }

    public static StationId createRestartedStationId() {
        return new StationId(MockNetworkId.createNetworkID(),
                             "STTN",
                             Defaults.WALL_FALL.getFissuresTime());
    }

    //this station is used as other station in MockSite and Channel as well.
    //It has one site and one channel
    public static StationId createOtherStationId() {
        return new StationId(MockNetworkId.createOtherNetworkID(),
                             "NTTS",
                             Defaults.WALL_FALL.getFissuresTime());
    }

    public static StationId createMultiSplendoredId(String code) {
        return new StationId(MockNetworkId.createMutliSplendoredNetworkID(),
                             code,
                             Defaults.WALL_FALL.getFissuresTime());
    }
}