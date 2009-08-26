/**
 * RetryNetworkDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;

/**
 * Just a pass thru class for the remote networkdc, but this will retry if there
 * are errors, up to the specified number. This can help in the case of
 * temporary network/server errors, but may simply waste time in the case of
 * bigger errors.
 */
public class RetryNetworkDC extends AbstractProxyNetworkDC {

    public RetryNetworkDC(NetworkDCOperations netDC, int retry) {
        this(netDC, new ClassicRetryStrategy(retry));
    }

    public RetryNetworkDC(NetworkDCOperations netDC,
                          RetryStrategy handler) {
        super(netDC);
        this.handler = handler;
    }

    public int getNumRetry() {
        return retry;
    }

    public NetworkExplorer a_explorer() {
        int count = 0;
        SystemException latest;
        try {
            return netDC.a_explorer();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(handler.shouldRetry(latest, this, count++)) {
            try {
                NetworkExplorer result = netDC.a_explorer();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public NetworkFinder a_finder() {
        int count = 0;
        SystemException latest;
        try {
            return netDC.a_finder();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(handler.shouldRetry(latest, this, count++)) {
            try {
                NetworkFinder result = netDC.a_finder();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    private RetryStrategy handler;

    private int retry;
}
