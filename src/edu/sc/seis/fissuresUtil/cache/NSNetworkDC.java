package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Category;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

/**
 * NSNetworkDC.java Created: Mon Jan 27 13:14:23 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public class NSNetworkDC implements ServerNameDNS, ProxyNetworkDC {

    public NSNetworkDC(String serverDNS, String serverName,
            FissuresNamingService fissuresNamingService) {
        this.serverDNS = serverDNS;
        this.serverName = serverName;
        this.namingService = fissuresNamingService;
    }

    public NetworkDCOperations getWrappedDC() {
        return getNetworkDC();
    }

    public NetworkDCOperations getWrappedDC(Class wrappedClass) {
        if(this.getClass().isAssignableFrom(wrappedClass)) {
            return this;
        } else {
            NetworkDCOperations tmp = getWrappedDC();
            if(tmp instanceof ProxyNetworkDC) { return ((ProxyNetworkDC)tmp).getWrappedDC(wrappedClass); }
        }
        throw new IllegalArgumentException("Can't find class "
                + wrappedClass.getName());
    }

    public org.omg.CORBA.Object getRealCorbaObject() {
        return getCorbaObject();
    }

    public NetworkDC getCorbaObject() {
        // side effect - make sure netDC is loaded
        NetworkDC n = getNetworkDC();
        if(n instanceof NetworkDC) {
            return (NetworkDC)n;
        } else {
            return ((ProxyNetworkDC)n).getCorbaObject();
        }
    }

    public FissuresNamingService getFissuresNamingService() {
        return namingService;
    }

    public String getServerDNS() {
        return serverDNS;
    }

    public String getServerName() {
        return serverName;
    }

    public synchronized void reset() {
        netDC = null;
    }

    public synchronized NetworkDC getNetworkDC() {
        if(netDC == null) {
            try {
                try {
                    netDC = namingService.getNetworkDC(serverDNS, serverName);
                } catch(Throwable t) {
                    namingService.reset();
                    netDC = namingService.getNetworkDC(serverDNS, serverName);
                }
            } catch(org.omg.CosNaming.NamingContextPackage.NotFound e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
                repackageException(e);
            } catch(org.omg.CosNaming.NamingContextPackage.InvalidName e) {
                repackageException(e);
            } // end of try-catch
        } // end of if ()
        return netDC;
    }

    protected void repackageException(org.omg.CORBA.UserException e) {
        org.omg.CORBA.TRANSIENT t = new org.omg.CORBA.TRANSIENT("Unable to resolve "
                                                                        + serverName
                                                                        + " "
                                                                        + serverDNS
                                                                        + " "
                                                                        + e.toString(),
                                                                0,
                                                                org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        t.initCause(e);
        throw t;
    }

    public NetworkExplorer a_explorer() {
        try {
            return getNetworkDC().a_explorer();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_explorer(), regetting from nameservice to try again.",
                        e);
            reset();
            return getNetworkDC().a_explorer();
        } // end of try-catch
    }

    public NetworkFinder a_finder() {
        try {
            return getNetworkDC().a_finder();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in a_finder(), regetting from nameservice to try again.",
                        e);
            reset();
            return getNetworkDC().a_finder();
        } // end of try-catch
    }

    protected NetworkDC netDC = null;

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService namingService;

    private static Category logger = Category.getInstance(NSNetworkDC.class.getName());
} // NSNetworkDC
