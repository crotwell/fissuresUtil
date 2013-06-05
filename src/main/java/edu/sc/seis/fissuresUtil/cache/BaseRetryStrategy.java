package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public abstract class BaseRetryStrategy implements RetryStrategy {
    
    public BaseRetryStrategy(int numRetries) {
        this.numRetries = numRetries;
    }

    public abstract boolean shouldRetry(Throwable exc,
                                        Object server,
                                        int tryCount);

    protected boolean basicShouldRetry(Throwable exc,
                                       ServerWrapper server,
                                       int tryCount) {
        if (numRetries == -1 || tryCount < numRetries) {
            // do a reset every other time
            if (tryCount % 2 == 0) {
                server.reset();
            }
            BulletproofVestFactory.retrySleep(tryCount);
            return true;
        } else {
            return false;
        }
    }
    
    public void serverRecovered(Object server){}
    
    int numRetries;
}
