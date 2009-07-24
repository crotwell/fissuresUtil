package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public abstract class BaseRetryStrategy implements RetryStrategy {

    public abstract boolean shouldRetry(SystemException exc,
                                        CorbaServerWrapper server,
                                        int tryCount,
                                        int numRetries);

    protected boolean basicShouldRetry(SystemException exc,
                                       CorbaServerWrapper server,
                                       int tryCount,
                                       int numRetries) {
        BulletproofVestFactory.retrySleep(tryCount);
        // do a reset every other time
        if (tryCount % 2 == 0) {
            server.reset();
        }
        return numRetries == -1 || tryCount <= numRetries;
    }
    
    public void serverRecovered(CorbaServerWrapper server){}
}
