/**
 * RetryNetworkDC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;


/** Just a pass thru class for the remote networkdc, but this will retry
 *  if there are errors, up to the specified number. This can help in the
 *  case of temporary network/server errors, but may simply waste time in
 *  the case of bigger errors. */
public class RetryNetworkDC implements NetworkDCOperations {

    public RetryNetworkDC(NetworkDCOperations netDC, int retry) {
        this.netDC = netDC;
        this.retry = retry;
    }

    public NetworkExplorer a_explorer() {
        int count = 0;
        SystemException lastException = null;
        while (count < retry) {
            try {
                return netDC.a_explorer();
            } catch (SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            } catch (OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public NetworkFinder a_finder() {
        int count = 0;
        SystemException lastException = null;
        while (count < retry) {
            try {
                return netDC.a_finder();
            } catch (SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            } catch (OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    NetworkDCOperations netDC;

    int retry;

    static Logger logger =
        Logger.getLogger(RetryNetworkDC.class);

}

