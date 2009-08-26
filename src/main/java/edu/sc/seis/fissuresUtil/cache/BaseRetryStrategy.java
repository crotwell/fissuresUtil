package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public abstract class BaseRetryStrategy implements RetryStrategy {
    
    public BaseRetryStrategy(int numRetries) {
        this.numRetries = numRetries;
    }

    public abstract boolean shouldRetry(SystemException exc,
                                        CorbaServerWrapper server,
                                        int tryCount);

    protected boolean basicShouldRetry(SystemException exc,
                                       CorbaServerWrapper server,
                                       int tryCount) {
        BulletproofVestFactory.retrySleep(tryCount);
        // do a reset every other time
        if (tryCount % 2 == 0) {
            server.reset();
        }
        return numRetries == -1 || tryCount <= numRetries;
    }
    
    public void serverRecovered(CorbaServerWrapper server){}
    
    int numRetries;
}
