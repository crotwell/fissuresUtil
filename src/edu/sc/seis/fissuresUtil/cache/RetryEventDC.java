/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * @author oliverpa
 */
public class RetryEventDC extends ProxyEventDC {

    public RetryEventDC(ProxyEventDC eventDC, int retry) {
        setEventDC(eventDC);
        this.retry = retry;
    }

    public EventFinder a_finder() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return getEventDC().a_finder();
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count + " of "
                        + retry, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public EventChannelFinder a_channel_finder() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return getEventDC().a_channel_finder();
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count + " of "
                        + retry, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    protected int retry;

    private static final Logger logger = Logger.getLogger(RetryEventDC.class);
}
