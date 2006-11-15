package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

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
        NetworkAccess[] vested = new NetworkAccess[accesses.length];
        for(int i = 0; i < accesses.length; i++) {
            vested[i] = vest(accesses[i]);
        }
        return vested;
    }

    public NetworkAccess vest(NetworkAccess na) {
        return vest(na, this, numRetry, handler);
    }

    private static ProxyNetworkAccess vest(NetworkAccess na,
                                           VestingNetworkFinder vnf,
                                           int numRetry,
                                           RetryStrategy handler) {
        SynchronizedNetworkAccess synch = new SynchronizedNetworkAccess(na);
        RetryNetworkAccess retry = new RetryNetworkAccess(synch,
                                                          numRetry,
                                                          handler);
        CacheNetworkAccess cache = new CacheNetworkAccess(retry);
        NetworkId id = cache.get_attributes().get_id();
        NSNetworkAccess nsNetworkAccess = new NSNetworkAccess(synch, id, vnf);
        retry.setNetworkAccess(nsNetworkAccess);
        cache.setNetworkAccess(retry);
        return cache;
    }

    int numRetry;

    private RetryStrategy handler;
}
