package edu.sc.seis.fissuresUtil.cache;

import java.util.WeakHashMap;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;

public class VestingNetworkFinder extends ProxyNetworkFinder {

    public VestingNetworkFinder(ProxyNetworkDC netDC, int numRetry) {
        this(netDC, new ClassicRetryStrategy(numRetry));
    }

    public VestingNetworkFinder(ProxyNetworkDC netDC,
                                RetryStrategy handler) {
        super(new CacheByIdNetworkFinder(new RetryNetworkFinder(new NSNetworkFinder(netDC), handler)));
        this.handler = handler;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        return vest(super.retrieve_by_id(id));
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return vest(super.retrieve_by_code(code));
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return vest(super.retrieve_by_name(name));
    }

    public NetworkAccess[] retrieve_all() {
        return vest(super.retrieve_all());
    }

    public NetworkAccess[] vest(NetworkAccess[] accesses) {
        CacheNetworkAccess[] vested = new CacheNetworkAccess[accesses.length];
        for(int i = 0; i < accesses.length; i++) {
            vested[i] = vest(accesses[i]);
        }
        return vested;
    }

    public CacheNetworkAccess vest(NetworkAccess na) {
        CacheNetworkAccess cache = vest(na, this, handler);
        synchronized(SynchronizedNetworkAccess.class) {
            allKnownNetworkAccess.put(cache, null);
        }
        return cache;
    }

    private static CacheNetworkAccess vest(NetworkAccess na,
                                           VestingNetworkFinder vnf,
                                           RetryStrategy handler) {
        SynchronizedNetworkAccess synch = new SynchronizedNetworkAccess(na);
        ProxyNetworkAccess justToHaveServerAndName = new JustToHaveServerAndName(synch,
                                                                                 vnf);
        RetryNetworkAccess retry = new RetryNetworkAccess(justToHaveServerAndName,
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
        logger.debug("(CacheById test) vest "+NetworkIdUtil.toString(id));
        NSNetworkAccess nsNetworkAccess = new NSNetworkAccess(id, this);
        RetryNetworkAccess retry = new RetryNetworkAccess(nsNetworkAccess,
                                                          handler);
        CacheNetworkAccess cache = new CacheNetworkAccess(retry, attr);
        synchronized(SynchronizedNetworkAccess.class) {
            allKnownNetworkAccess.put(cache, null);
        }
        return cache;
    }

    @Override
    public void reset() {
        synchronized(SynchronizedNetworkAccess.class) {
            super.reset();
            // do not tell all other network access to reset unless this is the
            // first time through
            // otherwise every network access tells the finder to reset, which
            // tells every network access
            // to rest...StackOverflow
            if(!insideReset) {
                insideReset = true;
                // copy to array instead of iterating over keyset as weakHashMap might loose keys during iteration
                // causing ConcurrentModificationException
                CacheNetworkAccess[] nets = allKnownNetworkAccess.keySet().toArray(new CacheNetworkAccess[0]);
                for (int i = 0; i < nets.length; i++) {
                    nets[i].reset();
                }
                try {
                    // give a chance for outstanding requests to server to come
                    // back
                    // idea is to give jacorb a chance to garbage collect
                    // connection/socket
                    // so we get a clean fresh socket to server
                    Thread.sleep(10000);
                } catch(InterruptedException e) {}
                insideReset = false;
            }
        }
    }

    private transient boolean insideReset = false;

    private RetryStrategy handler;

    /** map of all known networkAccesses from this finder in case we need to reset. */
    private WeakHashMap<CacheNetworkAccess, Object> allKnownNetworkAccess = new WeakHashMap<CacheNetworkAccess, Object>();

    static class JustToHaveServerAndName extends ProxyNetworkAccess {

        public JustToHaveServerAndName(NetworkAccess net,
                                       VestingNetworkFinder vnf) {
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
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(VestingNetworkFinder.class);
}
