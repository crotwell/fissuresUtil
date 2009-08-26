package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public class ClassicRetryStrategy extends BaseRetryStrategy {

    public ClassicRetryStrategy(int numRetries) {
        super(numRetries);
    }

    public boolean shouldRetry(SystemException exc,
                               CorbaServerWrapper server,
                               int tryCount) {
        String tryString;
        if(numRetries != -1) {
            tryString = "" + numRetries;
        } else {
            tryString = "infinity";
        }
        logger.debug("Caught exception on " + server.getFullName()
                + ", retrying " + tryCount + " of " + tryString, exc);
        return basicShouldRetry(exc, server, tryCount);
    }

    public void serverRecovered(CorbaServerWrapper server) {
        logger.debug(server.getFullName() + " recovered");
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ClassicRetryStrategy.class);
}
