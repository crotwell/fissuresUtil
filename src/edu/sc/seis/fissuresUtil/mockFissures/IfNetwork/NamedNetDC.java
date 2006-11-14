package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDC;

public class NamedNetDC {

    public static final String VECTOR = "Vector";
    public static final String SINGLE_CHANNEL = "SingleChannel";

    public static NetworkDC create(String name) {
        if(name.equals(SINGLE_CHANNEL)) {
            return new SingleChannel();
        } else if(name.equals(VECTOR)) {
            return new Vector();
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

    public static class Vector extends MockNetworkDC {

        public Vector() {
            super();
            NetworkAccess net = MockNetworkAccess.createNetworkAccess();
            ((MockNetworkFinder)finder).nets = new NetworkAccess[] {net};
        }
    }
}
