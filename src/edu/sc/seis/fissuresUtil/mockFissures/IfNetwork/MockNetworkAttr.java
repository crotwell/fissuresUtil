package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.network.NetworkAttrImpl;

public class MockNetworkAttr{
    public static NetworkAttr createNetworkAttr(){
        return new NetworkAttrImpl(MockNetworkId.createNetworkID(),
                                   "A network", "yes, a network", "Joe also");
    }

    public static NetworkAttr createOtherNetworkAttr(){
        return new NetworkAttrImpl(MockNetworkId.createOtherNetworkID(),
                                   "krowten A", "krowten a ,sey", "osla knarF");
    }
}
