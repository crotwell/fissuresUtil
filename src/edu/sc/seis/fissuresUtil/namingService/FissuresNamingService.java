package edu.sc.seis.fissuresUtil.namingService;


import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.IfEvent.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.InvalidName;

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
    org.omg.CORBA.Object resolve(String dns, String interfacename, String objectname) throws NotFound, CannotProceed, InvalidName;

    void rebind(String dns, String objectname, org.omg.CORBA.Object obj) throws NotFound, CannotProceed, InvalidName, Exception;

    void unbind(String dns, String interfacename, String objectname) throws NotFound, CannotProceed, InvalidName;


    NetworkDC getNetworkDC(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;

    DataCenter getSeismogramDC(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;
    
    PlottableDC getPlottableDC(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;
    
    org.omg.CORBA.Object getRoot();

    EventDC getEventDC(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName;

    String[] getInterfaceNames(String dns) throws NotFound, CannotProceed, InvalidName;
    
    String[] getInstanceNames(String dns, String interfacename) throws NotFound, CannotProceed, InvalidName;
  
    String[] getDNSNames(String dns) throws NotFound, CannotProceed, InvalidName;

    org.omg.CORBA.Object getNetworkDCObject(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;

    org.omg.CORBA.Object getSeismogramDCObject(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;
    
    org.omg.CORBA.Object getPlottableDCObject(String dns, String objectname) throws NotFound, CannotProceed, InvalidName;
    
  
    org.omg.CORBA.Object getEventDCObject(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName;

    
  
}// FissuresNamingService
