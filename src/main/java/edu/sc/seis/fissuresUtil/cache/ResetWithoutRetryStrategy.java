package edu.sc.seis.fissuresUtil.cache;


public class ResetWithoutRetryStrategy implements RetryStrategy {

    public boolean shouldRetry(Throwable exc, Object server, int tryCount) {
        if (server instanceof CorbaServerWrapper) {
            ((CorbaServerWrapper)server).reset();
        }
        return false;
    }

    public void serverRecovered(Object server) {}
}
