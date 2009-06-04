package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class RetryNetworkFinder extends ProxyNetworkFinder {

    public RetryNetworkFinder(NetworkFinder nf,
                              int retry,
                              RetryStrategy strategy) {
        super(nf);
        this.strategy = strategy;
        this.retry = retry;
    }

    public NetworkAccess[] retrieve_all() {
        int count = 0;
        SystemException latest;
        try {
            return super.retrieve_all();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                NetworkAccess[] result = super.retrieve_all();
                strategy.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        int count = 0;
        SystemException latest;
        try {
            return super.retrieve_by_code(code);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                NetworkAccess[] result = super.retrieve_by_code(code);
                strategy.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        int count = 0;
        SystemException latest;
        try {
            return super.retrieve_by_id(id);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                NetworkAccess result = super.retrieve_by_id(id);
                strategy.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        int count = 0;
        SystemException latest;
        try {
            return super.retrieve_by_name(name);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                NetworkAccess[] result = super.retrieve_by_name(name);
                strategy.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    private boolean shouldRetry(int count, SystemException t) {
        return strategy.shouldRetry(t, this, count, retry);
    }

    private int retry;

    private RetryStrategy strategy;
}
