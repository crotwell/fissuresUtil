package edu.sc.seis.fissuresUtil.namingService;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.IfEvent.*;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.ORBPackage.InvalidName;

import java.util.*;

import org.apache.log4j.*;

/**
 * FissuresNamingServiceImpl.java
 *
 *
 * Created: Wed Jan  9 11:30:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FissuresNamingServiceImpl implements FissuresNamingService {
    public FissuresNamingServiceImpl (java.util.Properties props) throws InvalidName {
	this.props = props;
	String[] args = new String[0];

	orb = 
	    (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
	
	// register valuetype factories
	AllVTFactory vt = new AllVTFactory();
	vt.register(orb);

	// get a reference to the Naming Service root_context
	org.omg.CORBA.Object rootObj = 
	    orb.resolve_initial_references("NameService");
	if (rootObj == null) {
	    //logger.error
	    logger.info("Name service object is null!");
	    return;
	}
	namingContext = NamingContextExtHelper.narrow(rootObj);
	//logger.info
	logger.info("got Name context");
	
    }

    public FissuresNamingServiceImpl(org.omg.CORBA_2_3.ORB orb) throws InvalidName {
	org.omg.CORBA.Object obj = null;
	obj = orb.resolve_initial_references("NameService");
	namingContext = NamingContextExtHelper.narrow(obj);	
    }

    public org.omg.CORBA.Object resolve(String dns, String interfacename, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
	
	dns = appendKindNames(dns);

	if(interfacename != null && interfacename.length() != 0)
	    dns = dns + "/" + interfacename + ".interface";
	if(objectname != null && objectname.length() != 0) {
	    objectname = objectname;// + getVersion();
	    dns = dns + "/" + objectname + ".object"+getVersion();
	}
	logger.info("the final dns resolved is "+dns);
	try {
	
	    return namingContext.resolve(namingContext.to_name(dns));
	} catch(org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
	    logger.info("NOT FOUND Exception caught while resolving dns name context and the name not found is "+nfe.rest_of_name[0].id);
	    throw new org.omg.CosNaming.NamingContextPackage.NotFound();
	} catch(org.omg.CosNaming.NamingContextPackage.InvalidName ine) {
	    logger.info("INVALID NAME Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();
	} catch(org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
	    logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.CannotProceed();
	} 

    }

    public void rebind(String dns, String objectname, org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	
	logger.info("The CLASS Name is "+obj.getClass().getName());

	
	String interfacename = getInterfaceName(obj);
	
	dns = appendKindNames(dns);

	if(interfacename != null && interfacename.length() != 0)
	    dns = dns + "/" + interfacename + ".interface";
	if(objectname != null && objectname.length() != 0) {    
	    objectname = objectname;// + getVersion();
	    dns = dns + "/" + objectname  +  ".object" + getVersion();
	}
	logger.info("the dns to be bind is "+dns);
	
	NameComponent[] ncName;

	try { 
	    ncName = namingContext.to_name(dns);
	} catch(org.omg.CosNaming.NamingContextPackage.InvalidName ine) {

	    logger.info("INVALID NAME EXCEPTION IS CAUGHT");
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();//"The DNS name "+dns+" that is passed is InValid ");
	    //return false;

	}

	NameComponent[] ncName1 = new NameComponent[1];
	NamingContext namingContextTemp = (NamingContext)namingContext;

	int counter;
	
	for(counter = 0; counter < ncName.length; counter++) {
	    //NameComponent temp[] = new NameComponent[counter];
	    int subcounter;
	    try {
		namingContext.rebind(namingContext.to_name(dns), obj);
		//namingContext.reslove(namingContext.to_name(dns));
				
	    } catch(org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
		switch(nfe.why.value()) {
		case NotFoundReason._missing_node:
		    ncName1[0] = nfe.rest_of_name[0];
		    logger.info("The id of the context is "+ncName1[0].id) ;
	
		    subcounter = 0;
		    for(int i = 0 ; i < ncName.length && !ncName[i].id.equals(ncName1[0].id); i++) {
			subcounter++;
	     
		    }
		  
		    NameComponent temp[] = new NameComponent[subcounter];
		    for(int i = 0 ; i < ncName.length && !ncName[i].id.equals(ncName1[0].id); i++) {
			temp[i] = ncName[i];
		     
		    }

		    if(subcounter != 0){
			logger.info("resolving new naming context");
			namingContextTemp = 
			    NamingContextExtHelper.narrow(namingContext.resolve(temp));
		    }

		    if(ncName1[0].id.equals(interfacename))
			ncName1[0].kind = "interface";
		    else if(ncName1[0].id.equals(objectname))
			ncName1[0].kind = "object" + getVersion();
		    else ncName1[0].kind = "dns";

		    try {
			namingContextTemp.bind_new_context(ncName1);
		    } catch (AlreadyBound e) {
			logger.error("Caught AlreadyBound, should not happen, ignoring...",
				     e);
		    } // end of try-catch
		    		    
		
		    break;
		case NotFoundReason._not_context:
		    logger.info("Not a Context");
		    logger.info(nfe.rest_of_name[0].id+"  IS PASSED AS A CONTEXT. ACTUALLY IT IS ALREADY BOUND AS AN OBJECT");
		    throw new org.omg.CosNaming.NamingContextPackage.NotFound(nfe.why, nfe.rest_of_name);
		    //break;
		case NotFoundReason._not_object:
		    logger.info("Not an Object");
		    logger.info(nfe.rest_of_name[0].id+"  IS PASSED AS AN OBJECT. ACTUALLY IT IS ALREADY BOUND AS A CONTEXT");
		    throw new org.omg.CosNaming.NamingContextPackage.NotFound(nfe.why, nfe.rest_of_name);
		}
	    } catch(org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
		logger.info("Caught Exception cannot proceed");
		throw new org.omg.CosNaming.NamingContextPackage.CannotProceed();
	    }
	}
    }


    public void unbind(String dns, String interfacename, String objectname) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
	
	
	dns = appendKindNames(dns);
	if(interfacename != null && interfacename.length() != 0)
	    dns = dns + "/" + interfacename + ".interface";
	if(objectname != null && objectname.length() != 0) {
	    objectname = objectname;// + getVersion();
	    dns = dns + "/" + objectname + ".object" + getVersion();
	}
	try {
	
	    namingContext.unbind(namingContext.to_name(dns));
	} catch(org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
	    logger.info("NOT FOUND Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.NotFound();
	} catch(org.omg.CosNaming.NamingContextPackage.InvalidName ine) {
	    logger.info("INVALID NAME Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();
	} catch(org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
	    logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.CannotProceed();
	} 


    }
    public void unbind(String dns, String objectname, org.omg.CORBA.Object obj) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
	

	dns = appendKindNames(dns);
	String interfacename = getInterfaceName(obj);
	if(interfacename != null && interfacename.length() != 0)
	    dns = dns + "/" + interfacename + ".interface";
	if(objectname != null && objectname.length() != 0) {
	    objectname = objectname;// + getVersion();
	    dns = dns + "/" + objectname + ".object" + getVersion();
	}
	try {
	
	    namingContext.unbind(namingContext.to_name(dns));
	} catch(org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
	    logger.info("NOT FOUND Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.NotFound();
	} catch(org.omg.CosNaming.NamingContextPackage.InvalidName ine) {
	    logger.info("INVALID NAME Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();
	} catch(org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
	    logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.CannotProceed();
	} 


    }



    public NetworkDC getNetworkDC(String dns, String objectname)  throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	org.omg.CORBA.Object obj = resolve(dns, "NetworkDC", objectname);
	
	NetworkDC netdc = NetworkDCHelper.narrow(obj);
	return netdc;

    }


    public DataCenter getSeismogramDC(String dns, String objectname)  throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	org.omg.CORBA.Object obj = resolve(dns, "DataCenter", objectname);
	
	DataCenter datacenter = DataCenterHelper.narrow(obj);
	return datacenter;

    }


    public PlottableDC getPlottableDC(String dns, String objectname)  throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	org.omg.CORBA.Object obj = resolve(dns, "PlottableDC", objectname);
	
	PlottableDC plottabledc = PlottableDCHelper.narrow(obj);
	return plottabledc;

    }


    public EventDC getEventDC(String dns, String objectname)  throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	org.omg.CORBA.Object obj = resolve(dns, "EventDC", objectname);
	
	EventDC eventdc = EventDCHelper.narrow(obj);
	return eventdc;

    }

    public org.omg.CORBA.Object getRoot() {

	return namingContext;
    }

    public String[] getInterfaceNames(String dns) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
	
	dns = appendKindNames(dns);
	
	return getNames(dns, "interface");

    }

    public String[] getInstanceNames(String dns, String interfacename) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	dns = appendKindNames(dns);
	if(interfacename != null && interfacename.length() != 0)
	    dns = dns + "/" + interfacename + ".interface";

	return getNames(dns, "object" + getVersion());

    }


    public String[] getDNSNames(String dns) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {

	String tempdns = new String(dns);
	dns = appendKindNames(dns);

	String[] rtnValues = getNames(dns, "dns");
	for(int counter = 0; counter < rtnValues.length; counter++)
	    rtnValues[counter] = tempdns + "/" + rtnValues[counter];

	return rtnValues;

    }
    
    private String[] getNames(String dns, String key) throws org.omg.CosNaming.NamingContextPackage.NotFound, org.omg.CosNaming.NamingContextPackage.CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
	ArrayList arrayList = new ArrayList();

	try {
	    NamingContextExt namingContextTemp = NamingContextExtHelper.narrow(namingContext.resolve(namingContext.to_name(dns)));
	    BindingListHolder bindingList = new BindingListHolder();
	    BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();

	    namingContextTemp.list(0, bindingList, bindingIteratorHolder);
	    
	    BindingIterator bindingIterator = bindingIteratorHolder.value;
	    BindingHolder bindingHolder = new BindingHolder();

	    while( bindingIterator.next_one(bindingHolder)) {
		
		Binding binding = bindingHolder.value;
		if(binding.binding_name[0].kind.equals(key))
		    arrayList.add(binding.binding_name[0].id);
	    }

	    
	    String[] rtnValues = new String[arrayList.size()];
	    rtnValues = (String[]) arrayList.toArray(rtnValues);
	    return rtnValues;

	} catch(org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
	    logger.info("NOT FOUND Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.NotFound();
	} catch(org.omg.CosNaming.NamingContextPackage.InvalidName ine) {
	    logger.info("INVALID NAME Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.InvalidName();
	} catch(org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
	    logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
	    throw new org.omg.CosNaming.NamingContextPackage.CannotProceed();
	} 

    }

    private String appendKindNames(String dns) {
	
	dns = "Fissures/" + dns+"/";

	StringTokenizer tokenizer = new StringTokenizer(dns, "/");
	String rtnValue = new String();
	
	while( tokenizer.hasMoreElements() ) {
	    
	    String temp = (String) tokenizer.nextElement();
	    temp = temp + ".dns/";
	    rtnValue = rtnValue + temp;
	}
	
	rtnValue = rtnValue.substring(0, rtnValue.length()-1);
	logger.info("The String returned is "+rtnValue);

	return rtnValue;

    }


    private String getVersion() {
    
	String version = edu.iris.Fissures.VERSION.value;
	String rtnValue = new String();
	String prefix = new String("_FVer");

	StringTokenizer tokenizer = new StringTokenizer(version, ".");
	
	while( tokenizer.hasMoreElements() ) {
	    
	    String temp = (String) tokenizer.nextElement();
	    temp = temp + "\\.";
	    rtnValue = rtnValue + temp;
	}
	rtnValue = prefix + rtnValue.substring(0, rtnValue.length() - 2);

	return rtnValue;

    }


    private String getInterfaceName(org.omg.CORBA.Object obj) {

	String temp = new String();
	Class[] interfacenames = obj.getClass().getInterfaces();

	for(int counter = 0; counter < interfacenames.length; counter++) {
	    
	    if(interfacenames[counter].getName().startsWith("edu.iris.Fissures")) {
		temp = interfacenames[counter].getName();
	        break;
	    }
	}

	StringTokenizer tokenizer = new StringTokenizer(temp, ".");
	String rtnValue = new String();
	while( tokenizer.hasMoreElements() ) {

	    rtnValue = (String) tokenizer.nextElement();
	}
	return rtnValue.substring(0, rtnValue.length());
  }
	
    private java.util.Properties props;
    private org.omg.CORBA_2_3.ORB orb;
    private NamingContextExt namingContext; 

     static Category logger = Category.getInstance(FissuresNamingServiceImpl.class.getName());


}// FissuresNamingServiceImpl
