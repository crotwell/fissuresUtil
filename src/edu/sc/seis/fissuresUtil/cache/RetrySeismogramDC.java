/**
 * RetryDataCenter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

public class RetrySeismogramDC implements ProxySeismogramDC, CorbaServerWrapper {

    public RetrySeismogramDC(NSSeismogramDC dc, int retry) {
        this(dc, retry, new ClassicRetryStrategy());
    }

    public RetrySeismogramDC(NSSeismogramDC dc, int retry, RetryStrategy strat) {
        this.dc = dc;
        this.retry = retry;
        this.strat = strat;
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
        return dc.getCorbaObject();
    }

    public void cancel_request(String a_request) throws FissuresException {
        int count = 0;
        while(true) {
            try {
                dc.cancel_request(a_request);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
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
        while(true) {
            try {
                return dc.request_seismograms(a_filterseq,
                                              a_client,
                                              long_lived,
                                              expiration_time);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        while(true) {
            try {
                return dc.retrieve_seismograms(a_filterseq);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        int count = 0;
        while(true) {
            try {
                return dc.retrieve_queue(a_request);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        int count = 0;
        while(true) {
            try {
                return dc.available_data(a_filterseq);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public String request_status(String a_request) throws FissuresException {
        int count = 0;
        while(true) {
            try {
                return dc.request_status(a_request);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        while(true) {
            try {
                return dc.queue_seismograms(a_filterseq);
            } catch(SystemException t) {
                if(!strat.shouldRetry(t, this, count++, retry)) {
                    throw t;
                }
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
    }

    public String toString() {
        return "Retry " + dc.toString();
    }

    public String getServerDNS() {
        return dc.getServerDNS();
    }

    public String getServerName() {
        return dc.getServerName();
    }

    public String getServerType() {
        return dc.getServerType();
    }

    private NSSeismogramDC dc;

    private int retry;

    private RetryStrategy strat;
}