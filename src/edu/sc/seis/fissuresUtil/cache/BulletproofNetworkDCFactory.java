package edu.sc.seis.fissuresUtil.cache;


public class BulletproofNetworkDCFactory{

    public static ProxyNetworkDC vest(ProxyNetworkDC netDC) {

        NSNetworkDC nsNetDC = (NSNetworkDC)netDC.getWrappedDC(NSNetworkDC.class);
        RetryNetworkDC retryNetDC = new RetryNetworkDC(nsNetDC, 3);
        return retryNetDC;
    }

}
