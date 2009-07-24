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
        SystemException latest;
        try {
            return getEventDC().a_channel_finder();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventChannelFinder result = getEventDC().a_channel_finder();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public EventFinder a_finder() {
        int count = 0;
        SystemException latest;
        try {
            return getEventDC().a_finder();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventFinder result = getEventDC().a_finder();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    private boolean shouldRetry(int i, SystemException t) {
        return handler.shouldRetry(t, this, i, retry);
    }

    protected int retry;

    private RetryStrategy handler;
}
