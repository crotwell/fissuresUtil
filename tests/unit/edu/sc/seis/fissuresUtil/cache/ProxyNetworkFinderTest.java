package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.BasicConfigurator;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkId;

public class ProxyNetworkFinderTest extends TestCase {
    
    public ProxyNetworkFinderTest() {
        BasicConfigurator.configure();
    }

    public void testNSAndRetry() {
        try {
            ProxyNetworkDC netDC = getVestedNetDC();
            ProxyNetworkFinder finder = BulletproofVestFactory.vestNetworkFinder(netDC.a_finder(),
                                                                                 netDC);
            NetworkAccess net = finder.retrieve_by_id(MockNetworkId.createNetworkID());
            System.out.println(NetworkIdUtil.toString(net.get_attributes().get_id()));
        } catch(Exception e) {
            e.printStackTrace();
        }        
    }

    private static ProxyNetworkDC getVestedNetDC() {
        return new RetryNetworkDC(new MockNSNetworkDC(true), 2);
    }
}
