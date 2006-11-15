package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public class ClassicRetryStrategy implements RetryStrategy {

    public boolean shouldRetry(SystemException exc,
                       CorbaServerWrapper server,
                       int tryCount,
                       int numRetries) {
        String tryString;
        if(numRetries != -1) {
            tryString = "" + numRetries;
        } else {
            tryString = "infinity";
        }
        logger.debug("Caught exception on " + server + ", retrying " + tryCount
                + " of " + tryString, exc);
        BulletproofVestFactory.retrySleep(tryCount);
        server.reset();
        return numRetries == -1 || tryCount <= numRetries;
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ClassicRetryStrategy.class);
}
