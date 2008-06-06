package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkAttrImpl;

public class VestingNetworkFinder extends ProxyNetworkFinder {

    public VestingNetworkFinder(ProxyNetworkDC netDC, int numRetry) {
        this(netDC, numRetry, new ClassicRetryStrategy());
    }

    public VestingNetworkFinder(ProxyNetworkDC netDC,
                                int numRetry,
                                RetryStrategy handler) {
        super(new NSNetworkFinder(netDC, numRetry, handler));
        this.numRetry = numRetry;
        this.handler = handler;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        return vest(nf.retrieve_by_id(id));
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return vest(nf.retrieve_by_code(code));
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return vest(nf.retrieve_by_name(name));
    }

    public NetworkAccess[] retrieve_all() {
        return vest(nf.retrieve_all());
    }

    public NetworkAccess[] vest(NetworkAccess[] accesses) {
        CacheNetworkAccess[] vested = new CacheNetworkAccess[accesses.length];
        for(int i = 0; i < accesses.length; i++) {
            vested[i] = vest(accesses[i]);
        }
        return vested;
    }

    public CacheNetworkAccess vest(NetworkAccess na) {
        return vest(na, this, numRetry, handler);
    }

    private static CacheNetworkAccess vest(NetworkAccess na,
                                           VestingNetworkFinder vnf,
                                           int numRetry,
                                           RetryStrategy handler) {
        SynchronizedNetworkAccess synch = new SynchronizedNetworkAccess(na);
        ProxyNetworkAccess justToHaveServerAndName = new JustToHaveServerAndName(synch, vnf);
        RetryNetworkAccess retry = new RetryNetworkAccess(justToHaveServerAndName,
                                                          numRetry,
                                                          handler);
        CacheNetworkAccess cache = new CacheNetworkAccess(retry);
        
        NetworkId id = cache.get_attributes().get_id();
        NSNetworkAccess nsNetworkAccess = new NSNetworkAccess(synch, id, vnf);
        retry.setNetworkAccess(nsNetworkAccess);
        cache.setNetworkAccess(retry);
        return cache;
    }
    
    public CacheNetworkAccess vest(NetworkAttrImpl attr) throws NetworkNotFound {
        NetworkId id = attr.get_id();
        NSNetworkAccess nsNetworkAccess = new NSNetworkAccess(id, this);
        RetryNetworkAccess retry = new RetryNetworkAccess(nsNetworkAccess,
                                                          numRetry,
                                                          handler);
        return new CacheNetworkAccess(retry, attr);
    }

    int numRetry;

    private RetryStrategy handler;
    
    static class JustToHaveServerAndName extends ProxyNetworkAccess {

        public JustToHaveServerAndName(NetworkAccess net, VestingNetworkFinder vnf) {
            super(net);
            this.vnf = vnf;
        }

        
        protected NetworkAttrImpl setSource(NetworkAttr attr) {
            NetworkAttrImpl impl = (NetworkAttrImpl)attr;
            impl.setSourceServerDNS(getServerDNS());
            impl.setSourceServerName(getServerName());
            return impl;
        }

        @Override
        public NetworkAttr get_attributes() {
            NetworkAttrImpl impl = (NetworkAttrImpl)super.get_attributes();
            impl.setSourceServerDNS(getServerDNS());
            impl.setSourceServerName(getServerName());
            return impl;
        }

        public String getServerDNS() {
            return vnf.getServerDNS();
        }

        public String getServerName() {
            return vnf.getServerName();
        }

        private VestingNetworkFinder vnf;
    }
}
