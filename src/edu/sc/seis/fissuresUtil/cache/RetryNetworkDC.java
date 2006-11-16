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
        this(netDC, retry, new ClassicRetryStrategy());
    }

    public RetryNetworkDC(NetworkDCOperations netDC,
                          int retry,
                          RetryStrategy handler) {
        super(netDC);
        this.retry = retry;
        this.handler = handler;
    }

    public int getNumRetry() {
        return retry;
    }

    public NetworkExplorer a_explorer() {
        int count = 0;
        while(true) {
            try {
                return netDC.a_explorer();
            } catch(SystemException t) {
                if(!handler.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public NetworkFinder a_finder() {
        int count = 0;
        while(true) {
            try {
                return netDC.a_finder();
            } catch(SystemException t) {
                if(!handler.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    private RetryStrategy handler;

    private int retry;
}
