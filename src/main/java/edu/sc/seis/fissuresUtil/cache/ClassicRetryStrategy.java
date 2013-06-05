package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public class ClassicRetryStrategy extends BaseRetryStrategy {

    public ClassicRetryStrategy(int numRetries) {
        super(numRetries);
    }

    public boolean shouldRetry(Throwable exc,
                               Object server,
                               int tryCount) {
        String tryString;
        if(numRetries != -1) {
            tryString = "" + numRetries;
        } else {
            tryString = "infinity";
        }
        String name;
        if (server instanceof CorbaServerWrapper) {
            name = ((CorbaServerWrapper)server).getFullName();
        } else {
            name = server.toString();
        }
        logger.debug("Caught exception on " + name
                + ", retrying " + tryCount + " of " + tryString, exc);
        return basicShouldRetry(exc, server, tryCount);
    }

    public void serverRecovered(Object server) {
        String name;
        if (server instanceof CorbaServerWrapper) {
            name = ((CorbaServerWrapper)server).getFullName();
        } else {
            name = server.toString();
        }
        logger.debug(name + " recovered");
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClassicRetryStrategy.class);
}
