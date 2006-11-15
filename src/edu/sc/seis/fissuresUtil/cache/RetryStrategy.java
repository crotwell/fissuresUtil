package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public interface RetryStrategy {

    public boolean shouldRetry(SystemException exc,
                               CorbaServerWrapper server,
                               int tryCount,
                               int numRetries);
}
