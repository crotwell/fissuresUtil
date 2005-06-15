/**
 * RetryDataCenter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

public class RetrySeismogramDC implements ProxySeismogramDC {

    public RetrySeismogramDC(DataCenterOperations dc, int retry) {
        this.dc = dc;
        this.retry = retry;
    }

    public void reset() {
    // don't need to do anything
    }

    public DataCenterOperations getWrappedDC() {
        return dc;
    }

    public DataCenterOperations getWrappedDC(Class wrappedClass) {
        if(getWrappedDC().getClass().equals(wrappedClass)) {
            return getWrappedDC();
        } else if(getWrappedDC().getClass().equals(ProxySeismogramDC.class)) {
            ((ProxySeismogramDC)getWrappedDC()).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("This RetryDataCenter doesn't contain a DC of class "
                + wrappedClass);
    }

    public org.omg.CORBA.Object getCorbaObject() {
        if(dc instanceof ProxySeismogramDC) {
            return ((ProxySeismogramDC)dc).getCorbaObject();
        } else {
            // this is bad as the dc need not be a DataCenter, but hopefully
            // always will be. If not, the offending class should be recoded
            // to be a ProxySeismogramDC
            return (DataCenter)dc;
        }
    }

    public void cancel_request(String a_request) throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                dc.cancel_request(a_request);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /**
     * if long_lived is true then the request is "sticky" in that the client
     * wants the data center to return not just the data that it has in its
     * archive currently, but also any data that it receives up to the
     * expiration_time. For instance if a station sends its data by mailing
     * tapes, then a researcher could issue a request for data that is expected
     * to be delivered from a recent earthquake, even thought the data center
     * does not yet have the data. Note that expiration_time is ignored if
     * long_lived is false.
     */
    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      Time expiration_time)
            throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.request_seismograms(a_filterseq,
                                              a_client,
                                              long_lived,
                                              expiration_time);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.retrieve_seismograms(a_filterseq);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.retrieve_queue(a_request);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.available_data(a_filterseq);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public String request_status(String a_request) throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.request_status(a_request);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        SystemException lastException = null;
        while(count < retry) {
            try {
                return dc.queue_seismograms(a_filterseq);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count, t);
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public String toString() {
        return "Retry " + dc.toString();
    }

    DataCenterOperations dc;

    int retry;

    private static final Logger logger = Logger.getLogger(RetrySeismogramDC.class);
}