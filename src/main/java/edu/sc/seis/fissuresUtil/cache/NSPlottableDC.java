/**
 * NSPlottableDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class NSPlottableDC implements ServerNameDNS, ProxyPlottableDC {

    public NSPlottableDC(String serverDNS, String serverName, FissuresNamingService fissuresNamingService) {
        this.serverDNS = serverDNS;
        this.serverName = serverName;
        this.namingService = fissuresNamingService;
    } // NSEventDC constructor

    public String getServerDNS() {
        return serverDNS;
    }

    public String getServerName() {
        return serverName;
    }

    public String getFullName() {
        return getServerDNS() + "/" + getServerName();
    }

    public String getServerType() {
        return PLOTTABLEDC_TYPE;
    }

    public synchronized void reset() {
        if (plottableDC.get() != null) {
            ((PlottableDC)plottableDC.get())._release();
        }
        plottableDC.set(null);
    }

    public org.omg.CORBA.Object getRealCorbaObject() {
        return getCorbaObject();
    }

    public synchronized PlottableDC getCorbaObject() {
        return getPlottableDC();
    }

    public synchronized PlottableDC getPlottableDC() {
        if (plottableDC.get() == null) {
            try {
                try {
                    plottableDC.set(namingService.getPlottableDC(serverDNS, serverName));
                } catch(Throwable t) {
                    namingService.reset();
                    plottableDC.set(namingService.getPlottableDC(serverDNS, serverName));
                }
            } catch(org.omg.CosNaming.NamingContextPackage.NotFound e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve " + serverName + " " + serverDNS + " "
                        + e.toString(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch(org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve " + serverName + " " + serverDNS + " "
                        + e.toString(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                throw new org.omg.CORBA.TRANSIENT("Unable to resolve " + serverName + " " + serverDNS + " "
                        + e.toString(), 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            } // end of try-catch
        } // end of if ()
        return (PlottableDC)plottableDC.get();
    }

    public Dimension[] get_whole_day_sizes() {
        try {
            return getPlottableDC().get_whole_day_sizes();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in get_whole_day_sizes(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().get_whole_day_sizes();
        } // end of try-catch
    }

    public Dimension[] get_event_sizes() {
        try {
            return getPlottableDC().get_event_sizes();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in get_event_sizes(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().get_event_sizes();
        } // end of try-catch
    }

    public Plottable[] get_for_day(ChannelId chan, int year, int jday, Dimension dim) throws PlottableNotAvailable,
            UnsupportedDimension {
        try {
            return getPlottableDC().get_for_day(chan, year, jday, dim);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in get_for_day(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().get_for_day(chan, year, jday, dim);
        } // end of try-catch
    }

    public boolean custom_sizes() {
        try {
            return getPlottableDC().custom_sizes();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in custom_sizes(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().custom_sizes();
        } // end of try-catch
    }

    public Plottable[] get_for_event(EventAccess event, ChannelId chan, Dimension dim) throws PlottableNotAvailable,
            UnsupportedDimension {
        try {
            return getPlottableDC().get_for_event(event, chan, dim);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in get_for_event(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().get_for_event(event, chan, dim);
        } // end of try-catch
    }

    public Plottable[] get_plottable(RequestFilter rf, Dimension dim) throws PlottableNotAvailable,
            UnsupportedDimension, NotImplemented {
        try {
            return getPlottableDC().get_plottable(rf, dim);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in get_plottable(), regetting from nameservice to try again.", e);
            reset();
            return getPlottableDC().get_plottable(rf, dim);
        } // end of try-catch
    }

    protected ThreadLocal<PlottableDC> plottableDC = new ThreadLocal<PlottableDC>();

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService namingService;

    private static Logger logger = LoggerFactory.getLogger(NSPlottableDC.class);

    public PlottableDCOperations getWrappedDC() {
        return getPlottableDC();
    }

    public PlottableDCOperations getWrappedDC(Class wrappedClass) {
        if (wrappedClass.equals(PlottableDC.class)) {
            return getCorbaObject();
        } else {
            throw new IllegalArgumentException("NSPlottableDCs only contain PlottableDCs, so it can't contain a ProxyDC of class "
                    + wrappedClass);
        }
    }
}
