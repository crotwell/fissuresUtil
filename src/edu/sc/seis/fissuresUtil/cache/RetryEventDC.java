/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * @author oliverpa
 */
public class RetryEventDC extends ProxyEventDC {

    public RetryEventDC(ProxyEventDC eventDC, int retry, RetryStrategy strat) {
        setEventDC(eventDC);
        this.handler = strat;
        this.retry = retry;
    }

    public EventChannelFinder a_channel_finder() {
        int count = 0;
        while(true) {
            try {
                return getEventDC().a_channel_finder();
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    private boolean shouldRetry(int i, SystemException t) {
        return handler.shouldRetry(t, this, i, retry);
    }

    public EventFinder a_finder() {
        int count = 0;
        while(true) {
            try {
                return getEventDC().a_finder();
            } catch(SystemException t) {
                if(!shouldRetry(count++, t)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    protected int retry;

    private RetryStrategy handler;
}
