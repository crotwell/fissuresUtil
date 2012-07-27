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
        super(new RetryNetworkDC(new NSNetworkDC(dns, name, fisName),
                                handler));
        this.handler = handler;
    }

    public NetworkFinder a_finder() {
        if (finder == null) {
            // don't want to give this to VestingNetworkFinder as the NSNetworkFinder calls a_finder on the netdc
            // vnf -> nsf -> vndc -> vnf -> nsf -> vncd -> ...
            // resulting in StackOverflow. Passing in the wrapped stops this infinite loop
            
            finder = new CleanDupNetworks(new VestingNetworkFinder((ProxyNetworkDC)getWrappedDC(), handler));
            //finder = new VestingNetworkFinder((ProxyNetworkDC)getWrappedDC(), handler);
        }
        return finder;
    }

    private RetryStrategy handler;
    
    private ProxyNetworkFinder finder = null;
}
