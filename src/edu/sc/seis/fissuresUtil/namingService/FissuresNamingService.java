package edu.sc.seis.fissuresUtil.namingService;


import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.IfEvent.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NamingContextPackage.*;

/**
 * FissuresNamingService.java
 *
 *
 * Created: Wed Jan  9 11:26:18 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */



public interface FissuresNamingService {
    org.omg.CORBA.Object resolve(String dns, String interfacename, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    void rebind(String dns, String objectname, org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, Exception;

    void unbind(String dns, String interfacename, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;


    NetworkDC getNetworkDC(String dns, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    DataCenter getSeismogramDC(String dns, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
    
    PlottableDC getPlottableDC(String dns, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
    
    org.omg.CORBA.Object getRoot();

    EventDC getEventDC(String dns, String objectname)  throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;

    String[] getInterfaceNames(String dns) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
    
    String[] getInstanceNames(String dns, String interfacename) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
  
    String[] getDNSNames(String dns) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName;
    
  
}// FissuresNamingService
