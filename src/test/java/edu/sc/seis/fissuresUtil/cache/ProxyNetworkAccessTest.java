package edu.sc.seis.fissuresUtil.cache;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.omg.CORBA.SystemException;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.NamedNetDC;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class ProxyNetworkAccessTest extends TestCase {

    private final class Counter implements RetryStrategy {

        public boolean shouldRetry(SystemException exc,
                           CorbaServerWrapper server,
                           int retryCount) {
            assertEquals(0, retryCount);
            count++;
            return true;
        }

        public void serverRecovered(CorbaServerWrapper server) {
            recovered = true;
        }

        private int count = 0;
        
        private boolean recovered = false;
    }

    public void testGettingNameAndDNS() {
        BasicConfigurator.configure();
        FissuresNamingService fns = new FissuresNamingService(new Properties());
        VestingNetworkDC ndc = new VestingNetworkDC(FissuresNamingService.MOCK_DNS,
                                                    NamedNetDC.SINGLE_CHANNEL,
                                                    fns);
        NetworkAccess[] nets = ndc.a_finder().retrieve_all();
        assertEquals(FissuresNamingService.MOCK_DNS,
                     ((ProxyNetworkAccess)nets[0]).getServerDNS());
        assertEquals(NamedNetDC.SINGLE_CHANNEL,
                     ((ProxyNetworkAccess)nets[0]).getServerName());
    }

    public void testSystemExceptionReporter() {
        BasicConfigurator.configure();
        Counter c = new Counter();
        FissuresNamingService fns = new FissuresNamingService(new Properties());
        VestingNetworkDC ndc = new VestingNetworkDC(FissuresNamingService.MOCK_DNS,
                                                    NamedNetDC.DODGY,
                                                    fns,
                                                    c);
        NetworkAccess[] nets = ndc.a_finder().retrieve_all();
        nets[0].retrieve_stations();
        assertEquals(1, c.count);
        assertEquals(true,c.recovered);
    }
}
