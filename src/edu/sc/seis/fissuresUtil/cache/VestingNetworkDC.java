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
        this(new RetryNetworkDC(new NSNetworkDC(dns, name, fisName), 2));
    }

    public NetworkFinder a_finder() {
        return new VestingNetworkFinder(proxy);
    }

    private ProxyNetworkDC proxy;
}
