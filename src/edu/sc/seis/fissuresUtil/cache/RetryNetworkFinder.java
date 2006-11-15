package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class RetryNetworkFinder extends ProxyNetworkFinder {

    public RetryNetworkFinder(NetworkFinder nf, int retry, RetryStrategy strategy) {
        super(nf);
        this.strategy = strategy;
        this.retry = retry;
    }

    public NetworkAccess[] retrieve_all() {
        int count = 0;
        while(true) {
            try {
                return nf.retrieve_all();
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        int count = 0;
        while(true) {
            try {
                return nf.retrieve_by_code(code);
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        int count = 0;
        while(true) {
            try {
                return nf.retrieve_by_id(id);
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        int count = 0;
        while(true) {
            try {
                return nf.retrieve_by_name(name);
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    private boolean shouldRetry(int count, SystemException t) {
        return strategy.shouldRetry(t, this, count, retry);
    }


    private int retry;
    
    private RetryStrategy strategy;

}
