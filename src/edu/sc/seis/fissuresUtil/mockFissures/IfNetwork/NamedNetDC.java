package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDC;

public class NamedNetDC {

    public static NetworkDC create(String name) {
        if(name.equals("SingleChannel")) {
            return new SingleChannel();
        }
        throw new RuntimeException("No mock net dc by the name of " + name
                + " known");
    }

    public static class SingleChannel extends MockNetworkDC {

        public SingleChannel() {
            super();
            NetworkAccess net = new MockNetworkAccess(MockNetworkAttr.createNetworkAttr(),
                                                      MockStation.createStation(),
                                                      new Channel[] {MockChannel.createChannel()});
            ((MockNetworkFinder)finder).nets = new NetworkAccess[] {net};
        }
    }
}
