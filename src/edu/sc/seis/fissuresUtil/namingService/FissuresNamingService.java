package edu.sc.seis.fissuresUtil.namingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Category;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIterator;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.NotFoundReason;
import org.omg.PortableServer.Servant;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCHelper;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkDCHelper;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableDCHelper;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterHelper;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.NSPlottableDC;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockDC;

/**
 * FissuresNamingService is a wrapper around CORBA Naming service. This class
 * makes the registration and resolve of CORBA objects easy. The user has to
 * pass a dns, objectname and CORBA object to register itself with the root
 * Naming service. Resolve takes the parameters dns, objectName and
 * interfaceName.
 * 
 * @created: Wed Jan 9 11:30:48 2002
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 */
public class FissuresNamingService {

    /**
     * Creates a new <code>FissuresNamingService</code> instance.
     */
    public FissuresNamingService(java.util.Properties props) {
        String[] args = new String[0];
        orb = (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);
        // register valuetype factories
        AllVTFactory vt = new AllVTFactory();
        vt.register(orb);
    }

    /**
     * Creates a new <code>FissuresNamingService</code> instance.
     */
    public FissuresNamingService(org.omg.CORBA_2_3.ORB orb) {
        this.orb = orb;
    }

    public void setNameServiceCorbaLoc(String nameServiceCorbaLoc) {
        this.nameServiceCorbaLoc = nameServiceCorbaLoc;
        rootNamingContext = null;
    }

    /**
     * Adds another name service to which all registrations should be sent. This
     * other name service is not used for queries, only when addind new servers.
     */
    public void addOtherNameServiceCorbaLoc(String otherNSCorbaLoc) {
        otherNS.add(otherNSCorbaLoc);
    }

    public String[] getOtherNameServices() {
        return (String[])otherNS.toArray(new String[0]);
    }

    /**
     * returns the reference to the root Naming Service.
     */
    public org.omg.CORBA.Object getRoot() {
        org.omg.CORBA.Object rootObj = null;
        if(nameServiceCorbaLoc != null) {
            logger.debug("corbaloc not null");
            logger.info("Using name service corba loc=" + nameServiceCorbaLoc);
            rootObj = orb.string_to_object(nameServiceCorbaLoc);
            logger.debug("got root object");
        } else {
            logger.debug("corbaloc is  null, resolve initial references");
            try {
                // get a reference to the Naming Service root_context
                rootObj = orb.resolve_initial_references("NameService");
            } catch(org.omg.CORBA.ORBPackage.InvalidName e) {
                // do nothing, return null in this case
            }
            if(rootObj == null) {
                logger.info("Name service object is null! Resolve initial references gave null.");
            }
        }
        return rootObj;
    }

    public NamingContextExt getNameService() {
        if(rootNamingContext == null) {
            org.omg.CORBA.Object rootObj = getRoot();
            if(rootObj == null) {
                logger.info("Name service object is null!");
                return null;
            }
            logger.debug("now trying narrow ");
            rootNamingContext = NamingContextExtHelper.narrow(rootObj);
        }
        return rootNamingContext;
    }

    public void reset() {
        rootNamingContext = null;
    }

    public org.omg.CORBA.Object resolveBySteps(NameComponent[] names)
            throws NotFound, CannotProceed, InvalidName {
        org.omg.CORBA.Object out = null;
        for(int i = 0; i < names.length; i++) {
            NameComponent[] subNames = new NameComponent[i + 1];
            System.arraycopy(names, 0, subNames, 0, i + 1);
            logger.debug("trying to resolve step id='" + names[i].id
                    + "' kind='" + names[i].kind + "'");
            out = getNameService().resolve(subNames);
        }
        return out;
    }

    /**
     * resolves a CORBA object with the name objectname.
     */
    public org.omg.CORBA.Object resolve(String dns,
                                        String interfacename,
                                        String objectname) throws NotFound,
            CannotProceed, InvalidName {
        dns = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0) {
            dns = dns + "/" + interfacename + ".interface";
        }
        if(objectname != null && objectname.length() != 0) {
            dns = dns + "/" + objectname + ".object" + getVersion();
        }
        logger.info("the final dns resolved is " + dns);
        // try 2 times in case of an exception
        int maxTry = 1;
        for(int i = 0; i <= maxTry; i++) {
            try {
                NameComponent[] names = getNameService().to_name(dns);
                //return resolveBySteps(names); // for debugging
                return getNameService().resolve(names);
            } catch(org.omg.CORBA.SystemException e) {
                logger.info("retry=" + i + " " + e);
                if(i == maxTry) { throw e; }
            } catch(NotFound nfe) {
                logger.info("retry="
                        + i
                        + "NOT FOUND Exception caught while resolving name context and the name not found is "
                        + nfe.rest_of_name[0].id + " from "
                        + nameServiceCorbaLoc);
                if(i == maxTry) { throw nfe; }
            } catch(InvalidName ine) {
                logger.info("retry="
                                    + i
                                    + "INVALID NAME Exception caught while resolving name context",
                            ine);
                if(i == maxTry) { throw ine; }
            } catch(CannotProceed cpe) {
                logger.info("retry="
                        + i
                        + "CANNOT PROCEED Exception caught while resolving dns name context");
                if(i == maxTry) { throw cpe; }
            }
        }
        throw new RuntimeException("This code should never happen");
    }

    /**
     * rebinds the CORBA object. If any of the naming context specified in the
     * dns doesnot exist it creates a corresponding namingcontext and continues.
     */
    public void rebind(String dns, String objectname, org.omg.CORBA.Object obj)
            throws NotFound, CannotProceed, InvalidName {
        rebind(dns, objectname, obj, getInterfaceName(obj));
    }

    /**
     * rebinds the CORBA object. If any of the naming context specified in the
     * dns doesnot exist it creates a corresponding namingcontext and continues.
     */
    public void rebind(String dns,
                       String objectname,
                       org.omg.CORBA.Object obj,
                       String interfacename) throws NotFound, CannotProceed,
            InvalidName {
        rebind(dns, objectname, obj, getNameService(), interfacename);
        Iterator it = otherNS.iterator();
        while(it.hasNext()) {
            String corbaloc = (String)it.next();
            org.omg.CORBA.Object ncObj = orb.string_to_object(corbaloc);
            if(ncObj != null) {
                NamingContextExt nc = NamingContextExtHelper.narrow(ncObj);
                rebind(dns, objectname, obj, nc, interfacename);
            } else {
                throw new InvalidName("Can't narrow NameContext for "
                        + corbaloc);
            }
        }
    }

    /**
     * rebinds the CORBA object on the given name service. If any of the naming
     * context specified in the dns doesnot exist it creates a corresponding
     * namingcontext and continues.
     */
    public void rebind(String dns,
                       String objectname,
                       org.omg.CORBA.Object obj,
                       NamingContextExt topLevelNameContext) throws NotFound,
            CannotProceed, InvalidName {
        logger.info("Rebind a class with name " + obj.getClass().getName());
        String interfacename = getInterfaceName(obj);
        rebind(dns, objectname, obj, topLevelNameContext, interfacename);
    }

    /**
     * rebinds the CORBA object on the given name service. If any of the naming
     * context specified in the dns doesnot exist it creates a corresponding
     * namingcontext and continues.
     */
    public void rebind(String dns,
                       String objectname,
                       org.omg.CORBA.Object obj,
                       NamingContextExt topLevelNameContext,
                       String interfacename) throws NotFound, CannotProceed,
            InvalidName {
         if(dns == null || dns.length() == 0) {
             throw new NullPointerException("dns must have characters: "+dns);
         }
         if( objectname == null || objectname.length() == 0) {
             throw new NullPointerException("objectname must have characters: "+objectname);
         }
         if( interfacename == null || interfacename.length() == 0) {
             throw new NullPointerException("interfacename must have characters: "+interfacename);
         }
        logger.info("rebind dns=" + dns + " interface=" + interfacename
                + " object=" + objectname);
        if(topLevelNameContext == null) {
            logger.warn("top level name context is null! Skipping rebind...");
            return;
        }
        String nameString = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0) {
            nameString = nameString + "/" + interfacename + ".interface";
        }
        if(objectname != null && objectname.length() != 0) {
            nameString = nameString + "/" + objectname + ".object"
                    + getVersion();
        }
        logger.info("the dns to be rebind is " + nameString);
        NameComponent[] ncName = topLevelNameContext.to_name(nameString);
        NamingContextExt namingContextTemp = topLevelNameContext;
        for(int counter = 0; counter < ncName.length; counter++) {
            try {
                topLevelNameContext.rebind(topLevelNameContext.to_name(nameString),
                                           obj);
            } catch(NotFound nfe) {
                switch(nfe.why.value()){
                    case NotFoundReason._missing_node:
                        NameComponent[] ncName1 = {nfe.rest_of_name[0]};
                        logger.info("The id of the context is " + ncName1[0].id);
                        int subcounter;
                        for(subcounter = 0; subcounter < ncName.length; subcounter++) {
                            if(ncName[subcounter].id.equals(ncName1[0].id)) {
                                break;
                            }
                        }
                        NameComponent temp[] = new NameComponent[subcounter];
                        for(int i = 0; i < ncName.length
                                && !ncName[i].id.equals(ncName1[0].id); i++) {
                            temp[i] = ncName[i];
                        }
                        if(subcounter != 0) {
                            logger.info("resolving new naming context");
                            namingContextTemp = NamingContextExtHelper.narrow(topLevelNameContext.resolve(temp));
                        }
                        if(ncName1[0].id.equals(interfacename)) {
                            ncName1[0].kind = INTERFACE;
                        } else if(ncName1[0].id.equals(objectname)) {
                            ncName1[0].kind = "object" + getVersion();
                        } else {
                            ncName1[0].kind = DNS;
                        }
                        try {
                            namingContextTemp.bind_new_context(ncName1);
                        } catch(AlreadyBound e) {
                            logger.error("Caught AlreadyBound, should not happen, ignoring...",
                                         e);
                        } // end of try-catch
                        topLevelNameContext.rebind(topLevelNameContext.to_name(nameString),
                                                   obj);
                        break;
                    case NotFoundReason._not_context:
                        logger.info("Not a Context");
                        logger.info(nfe.rest_of_name[0].id
                                + "  IS PASSED AS A CONTEXT. ACTUALLY IT IS ALREADY BOUND AS AN OBJECT");
                        throw nfe;
                    //break;
                    case NotFoundReason._not_object:
                        logger.info("Not an Object");
                        logger.info(nfe.rest_of_name[0].id
                                + "  IS PASSED AS AN OBJECT. ACTUALLY IT IS ALREADY BOUND AS A CONTEXT");
                        throw nfe;
                }
            }
        }
    }

    /**
     * unbinds the CORBA object specified by dns, objectname and interface name
     * from the default naming service and all other naming services this
     * instance has
     */
    public void unbind(String dns, String interfacename, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        logger.info("unbind " + dns + "/" + interfacename + "/" + objectname);
        unbind(dns, interfacename, objectname, getNameService());
        Iterator it = otherNS.iterator();
        while(it.hasNext()) {
            String corbaloc = (String)it.next();
            org.omg.CORBA.Object ncObj = orb.string_to_object(corbaloc);
            if(ncObj != null) {
                NamingContextExt nc = NamingContextExtHelper.narrow(ncObj);
                unbind(dns, interfacename, objectname, nc);
            }
        }
    }

    /**
     * unbinds the CORBA object specified by dns, objectname and interface name
     * from the given NamingContextExt
     */
    public void unbind(String dns,
                       String interfacename,
                       String objectname,
                       NamingContextExt topLevelNameContext) throws NotFound,
            CannotProceed, InvalidName {
        dns = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0) {
            dns = dns + "/" + interfacename + ".interface";
        }
        if(objectname != null && objectname.length() != 0) {
            dns = dns + "/" + objectname + ".object" + getVersion();
        }
        topLevelNameContext.unbind(topLevelNameContext.to_name(dns));
    }

    /**
     * unbinds the CORBA object from all naming services known by this
     * FissuresNamingService
     */
    public void unbind(String dns, String objectname, org.omg.CORBA.Object obj)
            throws NotFound, CannotProceed, InvalidName {
        String interfacename = getInterfaceName(obj);
        unbind(dns, interfacename, objectname);
    }

    /**
     * returns the NeworkDC object reference in the default namingService.
     */
    public NetworkDC getNetworkDC(String dns, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        if(isMock(dns, objectname)) { return new MockNetworkDC(); }
        return NetworkDCHelper.narrow(getNetworkDCObject(dns, objectname));
    }

    /**
     * returns the reference to NetworkDC in the namingService as a CORBA
     * object. The returned CORBA object must be narrowed to NetworkDC.
     */
    public org.omg.CORBA.Object getNetworkDCObject(String dns, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        return resolve(dns, NETWORKDC, objectname);
    }

    /**
     * returns the reference to the SeismogramDC inthe naming service.
     */
    public DataCenter getSeismogramDC(String dns, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        if(isMock(dns, objectname)) { return new MockDC(); }
        logger.debug("before get SeismogramDC Object");
        org.omg.CORBA.Object obj = getSeismogramDCObject(dns, objectname);
        logger.debug("before narrow");
        DataCenter datacenter = DataCenterHelper.narrow(obj);
        logger.debug("after narrow");
        return datacenter;
    }

    /**
     * returns the reference to SeismogramDC in the namingService as a CORBA
     * object. The returned CORBA object must be narrowed to SeismogramDC.
     */
    public org.omg.CORBA.Object getSeismogramDCObject(String dns,
                                                      String objectname)
            throws NotFound, CannotProceed, InvalidName {
        org.omg.CORBA.Object obj = resolve(dns, SEISDC, objectname);
        return obj;
    }

    /**
     * returns the reference to the PlottableDC in the namingService.
     */
    public PlottableDC getPlottableDC(String dns, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        return PlottableDCHelper.narrow(getPlottableDCObject(dns, objectname));
    }

    /**
     * returns the reference to PlottableDC in the namingService as a CORBA
     * object. The returned CORBA object must be narrowed to PlottableDC.
     */
    public org.omg.CORBA.Object getPlottableDCObject(String dns,
                                                     String objectname)
            throws NotFound, CannotProceed, InvalidName {
        org.omg.CORBA.Object obj = resolve(dns, PLOTTABLEDC, objectname);
        return obj;
    }

    /**
     * returns the reference to the EventDC in the namingService.
     */
    public EventDC getEventDC(String dns, String objectname) throws NotFound,
            CannotProceed, InvalidName {
        if(isMock(dns, objectname)) { return new MockEventDC(); }
        return EventDCHelper.narrow(getEventDCObject(dns, objectname));
    }

    /**
     * returns the reference to EventDC in the namingService as a CORBA object.
     * The returned CORBA object must be narrowed to EventDC.
     */
    public org.omg.CORBA.Object getEventDCObject(String dns, String objectname)
            throws NotFound, CannotProceed, InvalidName {
        org.omg.CORBA.Object obj = resolve(dns, EVENTDC, objectname);
        return obj;
    }

    public List getAllObjects(String interfaceName) {
        return getAllObjects(interfaceName, getRootNamingContextWithPath());
    }

    public NamingContextWithPath getRootNamingContextWithPath() {
        return new NamingContextWithPath(getNameService(), "/");
    }

    public List getAllObjects(String interfaceName,
                              NamingContextWithPath startingBranch) {
        List leaves = new ArrayList();
        NamingContext namingContext = startingBranch.getNamingContext();
        BindingListHolder bindings = new BindingListHolder();
        BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();
        namingContext.list(0, bindings, bindingIteratorHolder);
        BindingIterator bindingIterator = bindingIteratorHolder.value;
        BindingHolder bindingHolder = new BindingHolder();
        while(bindingIterator != null
                && bindingIterator.next_one(bindingHolder)) {
            Binding binding = bindingHolder.value;
            if(binding.binding_type == BindingType.ncontext) {
                if((binding.binding_name[0].kind.equals(INTERFACE) && binding.binding_name[0].id.equals(interfaceName))
                        || binding.binding_name[0].kind.equals(DNS)) {
                    String newPath;
                    if(binding.binding_name[0].kind.equals(DNS)) {
                        newPath = startingBranch.getPath()
                                + binding.binding_name[0].id + "/";
                    } else {
                        newPath = startingBranch.getPath();
                    }
                    NamingContext newNC = null;
                    try {
                        newNC = NamingContextHelper.narrow(namingContext.resolve(binding.binding_name));
                    } catch(UserException e) {
                        throw new RuntimeException("This should not happen as the naming context should have come from the server.  This probably indicates a programming error.",
                                                   e);
                    }
                    leaves.addAll(getAllObjects(interfaceName,
                                                new NamingContextWithPath(newNC,
                                                                          newPath)));
                }
            } else if(binding.binding_name[0].kind.equals(OBJECT)) {
                Object o = null;
                if(interfaceName.equals(NETWORKDC)) {
                    o = new NSNetworkDC(startingBranch.trimFissuresPath(),
                                        binding.binding_name[0].id,
                                        this);
                } else if(interfaceName.equals(EVENTDC)) {
                    o = new NSEventDC(startingBranch.trimFissuresPath(),
                                      binding.binding_name[0].id,
                                      this);
                } else if(interfaceName.equals(SEISDC)) {
                    o = new NSSeismogramDC(startingBranch.trimFissuresPath(),
                                           binding.binding_name[0].id,
                                           this);
                } else if(interfaceName.equals(PLOTTABLEDC)) {
                    o = new NSPlottableDC(startingBranch.trimFissuresPath(),
                                          binding.binding_name[0].id,
                                          this);
                } else {
                    try {
                        o = startingBranch.getNamingContext()
                                .resolve(binding.binding_name);
                    } catch(UserException e) {
                        throw new RuntimeException("This should not happen as the naming context should have come from the server.  This probably indicates a programming error.",
                                                   e);
                    }
                }
                leaves.add(o);
            }
        }
        return leaves;
    }

    /**
     * returns an array of all the NetworkDC objects registered with the default
     * naming Service.
     */
    public NSNetworkDC[] getAllNetworkDC() {
        return getAllNetworkDC(getRootNamingContextWithPath());
    }

    public NSNetworkDC[] getAllNetworkDC(NamingContextWithPath nc) {
        return (NSNetworkDC[])getAllObjects(NETWORKDC, nc).toArray(new NSNetworkDC[0]);
    }

    /**
     * returns an array of all the EventDC objects registered with the default
     * naming service.
     */
    public NSEventDC[] getAllEventDC() {
        return getAllEventDC(getRootNamingContextWithPath());
    }

    public NSEventDC[] getAllEventDC(NamingContextWithPath nc) {
        return (NSEventDC[])getAllObjects(EVENTDC, nc).toArray(new NSEventDC[0]);
    }

    /**
     * returns an array of all the PlottableDC objects registered with the
     * default naming service.
     */
    public NSPlottableDC[] getAllPlottableDC() {
        return getAllPlottableDC(getRootNamingContextWithPath());
    }

    public NSPlottableDC[] getAllPlottableDC(NamingContextWithPath nc) {
        return (NSPlottableDC[])getAllObjects(PLOTTABLEDC, nc).toArray(new NSPlottableDC[0]);
    }

    /**
     * returns an array of all the DataCenter objects registered with the
     * default naming service.
     */
    public NSSeismogramDC[] getAllSeismogramDC() {
        return getAllSeismogramDC(getRootNamingContextWithPath());
    }

    public NSSeismogramDC[] getAllSeismogramDC(NamingContextWithPath nc) {
        return (NSSeismogramDC[])getAllObjects(SEISDC, nc).toArray(new NSSeismogramDC[0]);
    }

    /**
     * returns all the interfaceNames(these are namingContexts) registered with
     * the naming Service
     */
    public String[] getInterfaceNames(String dns) throws NotFound,
            CannotProceed, InvalidName {
        dns = appendKindNames(dns);
        return getNames(dns, INTERFACE);
    }

    /**
     * returns the object names registered under the interface name given by
     * interfacename.
     */
    public String[] getInstanceNames(String dns, String interfacename)
            throws NotFound, CannotProceed, InvalidName {
        dns = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0) {
            dns = dns + "/" + interfacename + ".interface";
        }
        return getNames(dns, "object" + getVersion());
    }

    /**
     * returns the DNS names bound at dns
     */
    public String[] getDNSNames(String dns) throws NotFound, CannotProceed,
            InvalidName {
        String tempdns = new String(dns);
        dns = appendKindNames(dns);
        String[] rtnValues = getNames(dns, DNS);
        for(int counter = 0; counter < rtnValues.length; counter++) {
            rtnValues[counter] = tempdns + "/" + rtnValues[counter];
        }
        return rtnValues;
    }

    private String[] getNames(String dns, String kind) throws NotFound,
            CannotProceed, InvalidName {
        List bindings = new ArrayList();
        NamingContextExt myNS = getNameService();
        NameComponent[] nameComponents = myNS.to_name(dns);
        org.omg.CORBA.Object dnsNamingContextObj = myNS.resolve(nameComponents);
        NamingContextExt dnsNamingContext = NamingContextExtHelper.narrow(dnsNamingContextObj);
        BindingListHolder bindingList = new BindingListHolder();
        BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();
        dnsNamingContext.list(0, bindingList, bindingIteratorHolder);
        BindingIterator bindingIterator = bindingIteratorHolder.value;
        BindingHolder bindingHolder = new BindingHolder();
        while(bindingIterator.next_one(bindingHolder)) {
            Binding binding = bindingHolder.value;
            if(binding.binding_name[0].kind.equals(kind)) {
                bindings.add(binding.binding_name[0].id);
            }
        }
        return (String[])bindings.toArray(new String[bindings.size()]);
    }

    private String appendKindNames(String dns) {
        dns = FISSURES + "/" + dns + "/";
        StringTokenizer tokenizer = new StringTokenizer(dns, "/");
        String rtnValue = new String();
        while(tokenizer.hasMoreElements()) {
            String temp = (String)tokenizer.nextElement();
            temp = temp + "." + DNS + "/";
            rtnValue = rtnValue + temp;
        }
        rtnValue = rtnValue.substring(0, rtnValue.length() - 1);
        logger.info("The String returned is " + rtnValue);
        return rtnValue;
    }

    private static String getVersion() {
        String version = edu.iris.Fissures.VERSION.value;
        String rtnValue = new String();
        String prefix = new String("_FVer");
        StringTokenizer tokenizer = new StringTokenizer(version, ".");
        while(tokenizer.hasMoreElements()) {
            String temp = (String)tokenizer.nextElement();
            temp = temp + "\\.";
            rtnValue = rtnValue + temp;
        }
        rtnValue = prefix + rtnValue.substring(0, rtnValue.length() - 2);
        return rtnValue;
    }

    /**
     * This method works correctly with only the Fissures servers. Custom server
     * types will cause a IllegalArgumentException
     */
    public static String getInterfaceName(Servant obj) {
        return getInterfaceNameForClass(obj.getClass());
    }

    /**
     * This method works correctly with only the Fissures servers. Custom server
     * types will cause a IllegalArgumentException
     */
    public static String getInterfaceName(org.omg.CORBA.Object obj) {
        return getInterfaceNameForClass(obj.getClass());
    }

    public static Class[] getAllInterfaces(Class c) {
        List interfaces = new ArrayList();
        while(!c.equals(Object.class)) {
            interfaces.addAll(Arrays.asList(c.getInterfaces()));
            c = c.getSuperclass();
        }
        return (Class[])interfaces.toArray(new Class[0]);
    }

    /**
     * This method works correctly with only the Fissures servers. Custom server
     * types will cause a IllegalArgumentException
     */
    public static String getInterfaceNameForClass(Class cl) {
        String fissuresInterfaceName = null;
        Class[] interfaces = getAllInterfaces(cl);
        for(int counter = 0; counter < interfaces.length; counter++) {
            if(interfaces[counter].getName().startsWith("edu.iris.Fissures")) {
                fissuresInterfaceName = interfaces[counter].getName();
                StringTokenizer tokenizer = new StringTokenizer(fissuresInterfaceName,
                                                                ".");
                String rtnValue = null;
                while(tokenizer.hasMoreElements()) {
                    rtnValue = (String)tokenizer.nextElement();
                }
                if (rtnValue.endsWith("Operations")) {
                    rtnValue = rtnValue.substring(0, rtnValue.lastIndexOf("Operations"));
                }
                return rtnValue;
            }
        }
        throw new IllegalArgumentException(cl.getName()
                + " not recognized");
    }

    private String nameServiceCorbaLoc = null;

    private org.omg.CORBA_2_3.ORB orb;

    private NamingContextExt rootNamingContext;

    protected List otherNS = new LinkedList();

    static Category logger = Category.getInstance(FissuresNamingService.class.getName());

    public static boolean isMock(String dns, String name) {
        return dns.equals("edu/sc/seis")
                && (name.equals("Mock") || name.equals("Timeout"));
    }

    public static final String FISSURES = "Fissures";

    public static final String NETWORKDC = "NetworkDC";

    public static final String EVENTDC = "EventDC";

    public static final String PLOTTABLEDC = "PlottableDC";

    public static final String SEISDC = "DataCenter";

    public static final String INTERFACE = "interface";

    public static final String DNS = "dns";

    public static final String CORBALOC_PROP = "edu.sc.seis.fissuresUtil.nameServiceCorbaLoc";

    public static final String OBJECT = "object_FVer"
            + edu.iris.Fissures.VERSION.value;
}// FissuresNamingService
