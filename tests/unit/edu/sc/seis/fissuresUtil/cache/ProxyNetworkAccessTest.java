package edu.sc.seis.fissuresUtil.cache;

import java.util.Properties;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.NamedNetDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class ProxyNetworkAccessTest extends TestCase {

    public void testGettingNameAndDNS() {
        BasicConfigurator.configure();
        FissuresNamingService fns = new FissuresNamingService(new Properties());
        VestingNetworkDC ndc = new VestingNetworkDC(FissuresNamingService.MOCK_DNS,
                                                    NamedNetDC.SINGLE_CHANNEL,
                                                    fns);
        NetworkAccess[] nets = ndc.a_finder().retrieve_all();
        assertEquals(FissuresNamingService.MOCK_DNS,
                     ((ProxyNetworkAccess)nets[0]).getDNS());
        assertEquals(NamedNetDC.SINGLE_CHANNEL,
                     ((ProxyNetworkAccess)nets[0]).getName());
    }
}
