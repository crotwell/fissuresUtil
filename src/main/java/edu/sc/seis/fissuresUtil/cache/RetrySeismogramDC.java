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
        this(dc, new ClassicRetryStrategy(retry));
    }

    public RetrySeismogramDC(NSSeismogramDC dc, RetryStrategy strat) {
        this.dc = dc;
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

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        int count = 0;
        SystemException latest;
        try {
            return dc.available_data(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                RequestFilter[] result = dc.available_data(a_filterseq);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public void cancel_request(String a_request) throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            dc.cancel_request(a_request);
            return;
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                dc.cancel_request(a_request);
                strat.serverRecovered(this);
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            return dc.queue_seismograms(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                String result = dc.queue_seismograms(a_filterseq);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      Time expiration_time)
            throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            return dc.request_seismograms(a_filterseq,
                                          a_client,
                                          long_lived,
                                          expiration_time);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                String result = dc.request_seismograms(a_filterseq,
                                                       a_client,
                                                       long_lived,
                                                       expiration_time);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public String request_status(String a_request) throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            return dc.request_status(a_request);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                String result = dc.request_status(a_request);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            return dc.retrieve_queue(a_request);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                LocalSeismogram[] result = dc.retrieve_queue(a_request);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        int count = 0;
        SystemException latest;
        try {
            return dc.retrieve_seismograms(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                LocalSeismogram[] result = dc.retrieve_seismograms(a_filterseq);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
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

    public String getFullName() {
        return getServerDNS() + "/" + getServerName();
    }

    public String getServerType() {
        return dc.getServerType();
    }

    private NSSeismogramDC dc;

    private int retry;

    private RetryStrategy strat;
}