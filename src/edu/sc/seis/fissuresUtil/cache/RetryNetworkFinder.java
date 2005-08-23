package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class RetryNetworkFinder extends ProxyNetworkFinder {

    public RetryNetworkFinder(NetworkFinder nf, int retry) {
        super(nf);
        this.retry = retry;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return nf.retrieve_by_id(id);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return nf.retrieve_by_code(code);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return nf.retrieve_by_name(name);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public NetworkAccess[] retrieve_all() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return nf.retrieve_all();
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    int retry;

    private static Logger logger = Logger.getLogger(RetryNetworkFinder.class);
}
