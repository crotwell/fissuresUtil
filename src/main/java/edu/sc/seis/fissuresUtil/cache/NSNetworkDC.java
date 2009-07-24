package edu.sc.seis.fissuresUtil.cache;

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
public class NSNetworkDC implements ProxyNetworkDC {

    public NSNetworkDC(String serverDNS,
                       String serverName,
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
        }
        NetworkDCOperations tmp = getWrappedDC();
        if(tmp instanceof ProxyNetworkDC) {
            return ((ProxyNetworkDC)tmp).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("Can't find class "
                + wrappedClass.getName());
    }

    public org.omg.CORBA.Object getCorbaObject() {
        return getNetworkDC().getCorbaObject();
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
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }


    public String getServerType() {
        return NETDC_TYPE;
    }

    public synchronized void reset() {
        if(netDC != null){
            getCorbaObject()._release();
        }
        netDC = null;
    }

    public synchronized ProxyNetworkDC getNetworkDC() {
        if(netDC == null) {
            try {
                NetworkDCOperations realDC;
                try {
                    realDC = namingService.getNetworkDC(serverDNS, serverName);
                } catch(Throwable t) {
                    namingService.reset();
                    realDC = namingService.getNetworkDC(serverDNS, serverName);
                }
                netDC = new SynchronizedNetworkDC(realDC);
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
            reset();
            return getNetworkDC().a_explorer();
        } // end of try-catch
    }

    public NetworkFinder a_finder() {
        try {
            return getNetworkDC().a_finder();
        } catch(Throwable e) {
            reset();
            return getNetworkDC().a_finder();
        } // end of try-catch
    }

    protected SynchronizedNetworkDC netDC;

    protected String serverDNS, serverName;

    protected FissuresNamingService namingService;

} // NSNetworkDC
