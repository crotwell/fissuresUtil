/*
 * Created on Jul 21, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;

import edu.iris.Fissures.Dimension;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableDCOperations;
import edu.iris.Fissures.IfPlottable.PlottableNotAvailable;
import edu.iris.Fissures.IfPlottable.UnsupportedDimension;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * @author oliverpa
 */
public class RetryPlottableDC implements PlottableDCOperations {

	public RetryPlottableDC(PlottableDC plottableDC, int retry){
		this.plottableDC = plottableDC;
		this.retry = retry;
	}
	
	public boolean custom_sizes() {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.custom_sizes();
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

	public Plottable[] get_plottable(RequestFilter request, Dimension dimension)
			throws PlottableNotAvailable, UnsupportedDimension, NotImplemented {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.get_plottable(request, dimension);
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

	public Dimension[] get_whole_day_sizes() {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.get_whole_day_sizes();
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

	public Plottable[] get_for_day(ChannelId chan, int year, int day,
			Dimension dimension) throws PlottableNotAvailable, UnsupportedDimension {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.get_for_day(chan, year, day, dimension);
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

	public Dimension[] get_event_sizes() {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.get_event_sizes();
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

	public Plottable[] get_for_event(EventAccess event, ChannelId chan,
			Dimension dim) throws PlottableNotAvailable, UnsupportedDimension {
	       int count = 0;
	        SystemException lastException = null;
	        while (count < retry) {
	            try {
	                return plottableDC.get_for_event(event, chan, dim);
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

	protected PlottableDC plottableDC;
	protected int retry;
	
	private static final Logger logger = Logger.getLogger(RetryPlottableDC.class);
}
