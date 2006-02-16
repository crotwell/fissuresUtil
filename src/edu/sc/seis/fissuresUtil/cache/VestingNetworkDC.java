package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class VestingNetworkDC extends AbstractProxyNetworkDC {

    public VestingNetworkDC(ProxyNetworkDC netDC) {
        super(netDC);
        proxy = netDC;
    }

    public VestingNetworkDC(String dns,
                            String name,
                            FissuresNamingService fisName) {
        this(dns, name, fisName, BulletproofVestFactory.getDefaultNumRetry());
    }
    public VestingNetworkDC(String dns,
                            String name,
                            FissuresNamingService fisName,
                            int numRetry) {
        this(new RetryNetworkDC(new NSNetworkDC(dns, name, fisName), numRetry));
        this.numRetry = numRetry;
    }

    public NetworkFinder a_finder() {
        return new VestingNetworkFinder(proxy, numRetry);
    }

    private ProxyNetworkDC proxy;
    
    private int numRetry;
}
