package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class VestingNetworkDC extends AbstractProxyNetworkDC {

    protected VestingNetworkDC(ProxyNetworkDC netDC) {
        super(netDC);
        handler = new ClassicRetryStrategy(BulletproofVestFactory.getDefaultNumRetry());
    }

    public VestingNetworkDC(String dns,
                            String name,
                            FissuresNamingService fisName) {
        this(dns, name, fisName, new ClassicRetryStrategy(BulletproofVestFactory.getDefaultNumRetry()));
    }

    public VestingNetworkDC(String dns,
                            String name,
                            FissuresNamingService fisName,
                            RetryStrategy handler) {
        this(new RetryNetworkDC(new NSNetworkDC(dns, name, fisName),
                                handler));
        this.handler = handler;
    }

    public NetworkFinder a_finder() {
        if (finder == null) {
            finder = new VestingNetworkFinder((ProxyNetworkDC)getWrappedDC(), handler);
        }
        return finder;
    }

    private RetryStrategy handler;
    
    private VestingNetworkFinder finder = null;
}
