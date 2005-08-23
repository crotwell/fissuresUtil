package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkDC;


public class SpottyNetworkDC extends MockNetworkDC {
    
    public SpottyNetworkDC(boolean initiallyEvil) {
        evil = initiallyEvil;
    }
    
    public NetworkFinder a_finder() {
        System.out.println("a_finder() called.  evil is " + evil);
        NetworkFinder finder = new GoodOrEvilNetworkFinder(evil);
        evil = !evil;
        return finder;
    }
    
    boolean evil = true;
}
