package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.network.NetworkAttrImpl;

public class MockNetworkAttr{
    public static NetworkAttrImpl createNetworkAttr(){
        return new NetworkAttrImpl(MockNetworkId.createNetworkID(),
                                   "A network", "yes, a network", "Joe also");
    }

    public static NetworkAttrImpl createOtherNetworkAttr(){
        return new NetworkAttrImpl(MockNetworkId.createOtherNetworkID(),
                                   "krowten A", "krowten a ,sey", "osla knarF");
    }
    
    public static NetworkAttrImpl createMultiSplendoredAttr(){
        return new NetworkAttrImpl(MockNetworkId.createMutliSplendoredNetworkID(),
                                   "A network with many stations", "Many station network", "Charlie Groves");
    }
}
