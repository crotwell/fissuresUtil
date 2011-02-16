/**
 * RetryPlottableDC.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.fissuresUtil.cache;

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

public class RetryPlottableDC implements ProxyPlottableDC, CorbaServerWrapper {

    public RetryPlottableDC(NSPlottableDC plottable, int retry) {
        this(plottable, new ClassicRetryStrategy(retry));
    }

    public RetryPlottableDC(NSPlottableDC plottable,
                            RetryStrategy strat) {
        this.plottable = plottable;
        this.strat = strat;
    }

    public boolean custom_sizes() {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.custom_sizes();
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            // repackage to get at least a partial stack trace
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                boolean result = plottable.custom_sizes();
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public Plottable[] get_plottable(RequestFilter request, Dimension pixel_size)
            throws PlottableNotAvailable, UnsupportedDimension, NotImplemented {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.get_plottable(request, pixel_size);
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                Plottable[] result = plottable.get_plottable(request,
                                                             pixel_size);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public Dimension[] get_whole_day_sizes() {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.get_whole_day_sizes();
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                Dimension[] result = plottable.get_whole_day_sizes();
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public Plottable[] get_for_day(ChannelId channel_id,
                                   int year,
                                   int jday,
                                   Dimension pixel_size)
            throws PlottableNotAvailable, UnsupportedDimension {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.get_for_day(channel_id, year, jday, pixel_size);
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                Plottable[] result = plottable.get_for_day(channel_id,
                                                           year,
                                                           jday,
                                                           pixel_size);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public Dimension[] get_event_sizes() {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.get_event_sizes();
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                Dimension[] result = plottable.get_event_sizes();
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public Plottable[] get_for_event(EventAccess event,
                                     ChannelId channel_id,
                                     Dimension pixel_size)
            throws PlottableNotAvailable, UnsupportedDimension {
        int count = 0;
        SystemException lastException = null;
        try {
            return plottable.get_for_event(event, channel_id, pixel_size);
        } catch(SystemException t) {
            lastException = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(strat.shouldRetry(lastException, this, count++)) {
            try {
                Plottable[] result = plottable.get_for_event(event,
                                                             channel_id,
                                                             pixel_size);
                strat.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                lastException = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public PlottableDCOperations getWrappedDC() {
        return plottable;
    }

    public PlottableDCOperations getWrappedDC(Class wrappedClass) {
        if(getWrappedDC().getClass().equals(wrappedClass)) {
            return getWrappedDC();
        } else if(getWrappedDC() instanceof ProxySeismogramDC) {
            ((ProxyPlottableDC)getWrappedDC()).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("This doesn't contain a DC of class "
                + wrappedClass);
    }

    public void reset() {
        if(plottable instanceof ProxyPlottableDC) {
            ((ProxyPlottableDC)plottable).reset();
        }
    }

    public PlottableDC getCorbaObject() {
        if(plottable instanceof PlottableDC) {
            return (PlottableDC)plottable;
        } else if(plottable instanceof ProxyPlottableDC) {
            return ((ProxyPlottableDC)plottable).getCorbaObject();
        } else {
            throw new RuntimeException("subplottable not a PlottableDC or ProxyPlottableDC");
        }
    }

    public String getFullName() {
        return getServerDNS() + "/" + getServerName();
    }

    public String getServerDNS() {
        return plottable.getServerDNS();
    }

    public String getServerName() {
        return plottable.getServerName();
    }

    public String getServerType() {
        return plottable.getServerType();
    }

    NSPlottableDC plottable;

    int retry;

    RetryStrategy strat;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RetryPlottableDC.class);
}
