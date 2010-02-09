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
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

public class RetrySeismogramDC extends AbstractProxySeismogramDC {

    public RetrySeismogramDC(NSSeismogramDC dc, int retry) {
        this(dc, new ClassicRetryStrategy(retry));
    }

    public RetrySeismogramDC(NSSeismogramDC dc, RetryStrategy strat) {
        super(dc);
        this.strat = strat;
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        int count = 0;
        SystemException latest;
        try {
            return getWrappedDC().available_data(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                RequestFilter[] result = getWrappedDC().available_data(a_filterseq);
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
            getWrappedDC().cancel_request(a_request);
            return;
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                getWrappedDC().cancel_request(a_request);
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
            return getWrappedDC().queue_seismograms(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                String result = getWrappedDC().queue_seismograms(a_filterseq);
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
            return getWrappedDC().request_seismograms(a_filterseq,
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
                String result = getWrappedDC().request_seismograms(a_filterseq,
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
            return getWrappedDC().request_status(a_request);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                String result = getWrappedDC().request_status(a_request);
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
            return getWrappedDC().retrieve_queue(a_request);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                LocalSeismogram[] result = getWrappedDC().retrieve_queue(a_request);
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
            return getWrappedDC().retrieve_seismograms(a_filterseq);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(latest, this, count++)) {
            try {
                LocalSeismogram[] result = getWrappedDC().retrieve_seismograms(a_filterseq);
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
        return "Retry " + getWrappedDC().toString();
    }

    private RetryStrategy strat;
}