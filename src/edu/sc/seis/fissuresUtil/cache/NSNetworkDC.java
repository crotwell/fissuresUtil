package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import org.apache.log4j.*;

/**
 * NSNetworkDC.java
 *
 *
 * Created: Mon Jan 27 13:14:23 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NSNetworkDC implements NetworkDCOperations {

    public NSNetworkDC(String serverDNS, 
		       String serverName, 
		       FissuresNamingService fissuresNamingService) {
	this.serverDNS = serverDNS;
	this.serverName = serverName;
	this.fissuresNamingService = fissuresNamingService;
    } // NSNetworkDC constructor
    
    public String getServerDNS() {
	return serverDNS;
    }

    public String getServerName() {
	return serverName;
    }

    public NetworkDC getNetworkDC() {
	if ( netDC == null) {
	    try {
		netDC = fissuresNamingService.getNetworkDC(serverDNS, 
							   serverName);
	    } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
		throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
						  0,
						  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	    } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
		throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
						  0,
						  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	    } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
		throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
						  0,
						  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	    } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
		throw new org.omg.CORBA.TRANSIENT("Unable to resolve "+serverName+" "+serverDNS+" "+e.toString(),
						  0,
						  org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	    } // end of try-catch
	} // end of if ()
	return netDC;
    }

    public NetworkExplorer a_explorer() {
	try {
	    return getNetworkDC().a_explorer();
	} catch (Exception e) {
	    // retry in case regetting from name service helps
	    logger.warn("Exception in a_explorer(), regetting from nameservice to try again.", e);
	    netDC = null;
	    return getNetworkDC().a_explorer();
	} // end of try-catch
    }

    public NetworkFinder a_finder() {
	try {
	    return getNetworkDC().a_finder();
	} catch (Exception e) {
	    // retry in case regetting from name service helps
	    logger.warn("Exception in a_finder(), regetting from nameservice to try again.", e);
	    netDC = null;
	    return getNetworkDC().a_finder();
	} // end of try-catch
    }

    protected NetworkDC netDC = null;

    protected String serverDNS;

    protected String serverName;

    protected FissuresNamingService fissuresNamingService;

    static Category logger = 
        Category.getInstance(NSNetworkDC.class.getName());

} // NSNetworkDC
