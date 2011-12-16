/**
 * RetryDataCenter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
        addKnownSeisDC(this);
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
    
    @Override
    public void reset() {
        synchronized(RetrySeismogramDC.class) {
            super.reset();
            // do not tell all other seis dc to reset unless this is the
            // first time through
            // otherwise every seis dc tells every other seis dc
            // to rest...StackOverflow
            if (!insideReset) {
                insideReset = true;
                Set<SoftReference<ProxySeismogramDC>> allKnowDCsCopy = new HashSet<SoftReference<ProxySeismogramDC>>();
                allKnowDCsCopy.addAll(allKnownDCs);
                Iterator<SoftReference<ProxySeismogramDC>> it = allKnowDCsCopy.iterator();
                while (it.hasNext()) {
                    SoftReference<ProxySeismogramDC> dcref = it.next();
                    ProxySeismogramDC dc = dcref.get();
                    if (dc != null) {
                        dc.reset();
                    } else {
                        it.remove();
                    }
                }
                try {
                    // give a chance for outstanding requests to server to come
                    // back
                    // idea is to give jacorb a chance to garbage collect
                    // connection/socket
                    // so we get a clean fresh socket to server
                    Thread.sleep(10000);
                } catch(InterruptedException e) {}
                insideReset = false;
            }
        }
    }

    private static transient boolean insideReset = false;

    private static int numSeisDCsAdded = 0;

    protected static void addKnownSeisDC(ProxySeismogramDC cache) {
        synchronized(RetrySeismogramDC.class) {
            allKnownDCs.add(new SoftReference<ProxySeismogramDC>(cache));
        }
        numSeisDCsAdded += 1;
        if (numSeisDCsAdded % 1000 == 0) {
            synchronized(RetrySeismogramDC.class) {
                // zap any soft references with null refs
                Iterator<SoftReference<ProxySeismogramDC>> it = allKnownDCs.iterator();
                while (it.hasNext()) {
                    SoftReference<ProxySeismogramDC> net = it.next();
                    if (net.get() == null) {
                        it.remove();
                    }
                }
            }
        }
    }
    
    /**
     * map of all known networkAccesses from this finder in case we need to
     * reset.
     */
    private static Set<SoftReference<ProxySeismogramDC>> allKnownDCs = new HashSet<SoftReference<ProxySeismogramDC>>();

    public String toString() {
        return "Retry " + getWrappedDC().toString();
    }

    private RetryStrategy strat;
}