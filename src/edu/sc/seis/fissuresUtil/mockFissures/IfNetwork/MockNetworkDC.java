/*
 * Created on Jul 19, 2004
 */
package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;

/**
 * @author Charlie Groves
 */
public class MockNetworkDC implements NetworkDCOperations {

    public NetworkExplorer a_explorer() {
        return null;
    }

    public NetworkFinder a_finder() {
        return finder;
    }
    
    NetworkFinder finder = new MockNetworkFinder();

}
