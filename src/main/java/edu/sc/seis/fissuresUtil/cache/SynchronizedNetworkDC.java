package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;

public class SynchronizedNetworkDC extends AbstractProxyNetworkDC {

    public SynchronizedNetworkDC(NetworkDCOperations netDC) {
        super(netDC);
    }

    public NetworkFinder a_finder() {
        synchronized(SynchronizedNetworkAccess.class) {
            return netDC.a_finder();
        }
    }

    public NetworkExplorer a_explorer() {
        synchronized(SynchronizedNetworkAccess.class) {
            return netDC.a_explorer();
        }
    }
}
