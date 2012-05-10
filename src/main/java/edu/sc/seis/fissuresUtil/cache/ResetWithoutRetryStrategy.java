package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;

public class ResetWithoutRetryStrategy implements RetryStrategy {

    public boolean shouldRetry(SystemException exc, CorbaServerWrapper server, int tryCount) {
        server.reset();
        return false;
    }

    public void serverRecovered(CorbaServerWrapper server) {}
}
