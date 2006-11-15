package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import org.omg.CORBA.UNKNOWN;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.Station;

public class NamedNetDC {

    public static final String VECTOR = "Vector";

    public static final String SINGLE_CHANNEL = "SingleChannel";

    public static final String DODGY = "Dodgy";

    public static NetworkDC create(String name) {
        if(name.equals(SINGLE_CHANNEL)) {
            return new SingleChannel();
        } else if(name.equals(VECTOR)) {
            return new Vector();
        }else if(name.equals(DODGY)){
            return new Dodgy();
        }
        throw new RuntimeException("No mock net dc by the name of " + name
                + " known");
    }

    public static class SingleChannel extends MockNetworkDC {

        public SingleChannel() {
            NetworkAccess net = new MockNetworkAccess(MockNetworkAttr.createNetworkAttr(),
                                                      MockStation.createStation(),
                                                      new Channel[] {MockChannel.createChannel()});
            ((MockNetworkFinder)finder).nets = new NetworkAccess[] {net};
        }
    }

    public static class Vector extends MockNetworkDC {

        public Vector() {
            NetworkAccess net = MockNetworkAccess.createNetworkAccess();
            ((MockNetworkFinder)finder).nets = new NetworkAccess[] {net};
        }
    }

    public static class Dodgy extends MockNetworkDC {

        public Dodgy() {
            NetworkAccess net = new MockNetworkAccess(MockNetworkAttr.createNetworkAttr(),
                                                      MockStation.createStation(),
                                                      new Channel[] {MockChannel.createChannel()}) {

                public Station[] retrieve_stations() {
                    if(numcalls++ % 2 == 0) {
                        throw new UNKNOWN("Try again!");
                    }
                    return super.retrieve_stations();
                }

                int numcalls = 0;
            };
            ((MockNetworkFinder)finder).nets = new NetworkAccess[] {net};
        }
    }
}
