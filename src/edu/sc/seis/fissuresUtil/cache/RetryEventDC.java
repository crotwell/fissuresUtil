/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCOperations;
import edu.iris.Fissures.IfEvent.EventFinder;

/**
 * @author oliverpa
 */
public class RetryEventDC implements EventDCOperations {

	public RetryEventDC(EventDC eventDC, int retry){
		this.eventDC = eventDC;
		this.retry = retry;
	}
	
	public EventFinder a_finder() {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return eventDC.a_finder();
	            } catch (SystemException t) {
	                lastException = t;
	                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
	            } catch (OutOfMemoryError e) {
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
	        while (count < retry) {
	            try {
	                return eventDC.a_channel_finder();
	            } catch (SystemException t) {
	                lastException = t;
	                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
	            } catch (OutOfMemoryError e) {
	                // repackage to get at least a partial stack trace
	                throw new RuntimeException("Out of memory", e);
	            }
	            count++;
	        }
	        throw lastException;
	}
	
	protected EventDC eventDC;
	protected int retry;
	
	private static final Logger logger = Logger.getLogger(RetryEventDC.class);

}
