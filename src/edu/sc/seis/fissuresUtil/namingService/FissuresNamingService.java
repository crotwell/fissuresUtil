package edu.sc.seis.fissuresUtil.namingService;


import org.omg.CosNaming.*;

import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventDCHelper;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkDCHelper;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableDCHelper;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterHelper;
import edu.iris.Fissures.model.AllVTFactory;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Category;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.CosNaming.NamingContextPackage.NotFoundReason;

/**
 * Description: FissuresNamingService is a wrapper around CORBA Naming service. This class
 * makes the registration and resolve of CORBA objects easy. The user has to pass a dns, objectname
 * and CORBA object to register itself with the root Naming service. Resolve takes the parameters
 * dns, objectName and interfaceName.
 *
 *
 * Created: Wed Jan  9 11:30:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class FissuresNamingService {
    /**
     * Creates a new <code>FissuresNamingService</code> instance.
     *
     * @param props a <code>java.util.Properties</code> value
     * @exception InvalidName if an error occurs
     */

    public FissuresNamingService (java.util.Properties props) {
        this.props = props;
        String[] args = new String[0];

        orb =
            (org.omg.CORBA_2_3.ORB)org.omg.CORBA.ORB.init(args, props);

        // register valuetype factories
        AllVTFactory vt = new AllVTFactory();
        vt.register(orb);

    }

    /**
     * Creates a new <code>FissuresNamingService</code> instance.
     *
     * @param orb an <code>org.omg.CORBA_2_3.ORB</code> value
     * @exception InvalidName if an error occurs
     */
    public FissuresNamingService(org.omg.CORBA_2_3.ORB orb) {
        this.orb = orb;
    }

    public void setNameServiceCorbaLoc(String nameServiceCorbaLoc) {
        this.nameServiceCorbaLoc = nameServiceCorbaLoc;
        namingContext = null;
    }

    /**
     * returns the reference to the root Naming Service.
     *
     * @return an <code>org.omg.CORBA.Object</code> value
     */
    public org.omg.CORBA.Object getRoot() {
        org.omg.CORBA.Object rootObj = null;
        if (nameServiceCorbaLoc != null) {
            logger.debug("context and corbaloc are null");
            rootObj = orb.string_to_object(nameServiceCorbaLoc);
            logger.debug("got root object");
        } else {
            logger.debug("name context is still null after attempt to load, resolve initial references");
            // get a reference to the Naming Service root_context
            try {
                rootObj = orb.resolve_initial_references("NameService");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                // do nothing, return null in this case
            }
            if (rootObj == null) {
                //logger.error
                logger.info("Name service object is null!");
            }
        }
        return rootObj;
    }

    public NamingContextExt getNameService() {
        if (namingContext == null) {
            org.omg.CORBA.Object rootObj = getRoot();
            if (rootObj == null) {
                //logger.error
                logger.info("Name service object is null!");
                return null;
            }
            logger.debug("now trying narrow ");
            namingContext = NamingContextExtHelper.narrow(rootObj);
        }
        logger.debug("returning name service");
        return namingContext;
    }

    public void reset() {
        namingContext = null;
    }

    public org.omg.CORBA.Object resolveBySteps(NameComponent[] names)
        throws NotFound,CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        NameComponent[] subNames;
        org.omg.CORBA.Object out = null;
        for (int i = 0; i < names.length; i++) {
            subNames = new NameComponent[i+1];
            System.arraycopy(names, 0, subNames, 0, i+1);
            logger.debug("trying to resolve step id='"+names[i].id+"' kind='"+names[i].kind+"'");
            out = getNameService().resolve(subNames);
        }
        return out;
    }

    /**
     * resolves a CORBA object with the name objectname.
     *
     * @param dns a <code>String</code> value
     * @param interfacename a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public org.omg.CORBA.Object resolve(String dns, String interfacename, String objectname) throws NotFound,CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        dns = appendKindNames(dns);

        if(interfacename != null && interfacename.length() != 0)
            dns = dns + "/" + interfacename + ".interface";
        if(objectname != null && objectname.length() != 0) {
            objectname = objectname;// + getVersion();
            dns = dns + "/" + objectname + ".object"+getVersion();
        }
        logger.info("the final dns resolved is "+dns);
        try {
            NameComponent[] names = getNameService().to_name(dns);
            //return resolveBySteps(names); // for debugging
            return getNameService().resolve(names);
        } catch(NotFound nfe) {
            logger.info("NOT FOUND Exception caught while resolving name context and the name not found is "+nfe.rest_of_name[0].id);
            throw nfe;
        } catch(InvalidName ine) {
            logger.info("INVALID NAME Exception caught while resolving name context", ine);
            throw ine;
        } catch(CannotProceed cpe) {
            logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
            throw cpe;
        }

    }

    /**
     * rebinds the CORBA object. If any of the naming context specified in the dns doesnot exist
     * it creates a corresponding namingcontext and continues.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @param obj an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public void rebind(String dns, String objectname, org.omg.CORBA.Object obj) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {


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
            ncName = getNameService().to_name(dns);
        } catch(InvalidName ine) {

            logger.info("INVALID NAME EXCEPTION IS CAUGHT");
            throw new InvalidName();//"The DNS name "+dns+" that is passed is InValid ");
            //return false;

        }

        NameComponent[] ncName1 = new NameComponent[1];
        NamingContext namingContextTemp = (NamingContext)getNameService();

        int counter;

        for(counter = 0; counter < ncName.length; counter++) {
            //NameComponent temp[] = new NameComponent[counter];
            int subcounter;
            try {
                getNameService().rebind(getNameService().to_name(dns), obj);
                //namingContext.reslove(namingContext.to_name(dns));

            } catch(NotFound nfe) {
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
                                NamingContextExtHelper.narrow(getNameService().resolve(temp));
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
                        throw new NotFound(nfe.why, nfe.rest_of_name);
                        //break;
                    case NotFoundReason._not_object:
                        logger.info("Not an Object");
                        logger.info(nfe.rest_of_name[0].id+"  IS PASSED AS AN OBJECT. ACTUALLY IT IS ALREADY BOUND AS A CONTEXT");
                        throw new NotFound(nfe.why, nfe.rest_of_name);
                }
            } catch(CannotProceed cpe) {
                logger.info("Caught Exception cannot proceed");
                throw new CannotProceed();
            }
        }
    }


    /**
     * unbinds the CORBA object.
     *
     * @param dns a <code>String</code> value
     * @param interfacename a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public void unbind(String dns, String interfacename, String objectname) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {


        dns = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0)
            dns = dns + "/" + interfacename + ".interface";
        if(objectname != null && objectname.length() != 0) {
            objectname = objectname;// + getVersion();
            dns = dns + "/" + objectname + ".object" + getVersion();
        }
        try {

            getNameService().unbind(getNameService().to_name(dns));
        } catch(NotFound nfe) {
            logger.info("NOT FOUND Exception caught while resolving dns name context");
            throw new NotFound();
        } catch(InvalidName ine) {
            logger.info("INVALID NAME Exception caught while resolving dns name context");
            throw new InvalidName();
        } catch(CannotProceed cpe) {
            logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
            throw new CannotProceed();
        }


    }
    /**
     * unbinds the CORBA object.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @param obj an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public void unbind(String dns, String objectname, org.omg.CORBA.Object obj) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {


        dns = appendKindNames(dns);
        String interfacename = getInterfaceName(obj);
        if(interfacename != null && interfacename.length() != 0)
            dns = dns + "/" + interfacename + ".interface";
        if(objectname != null && objectname.length() != 0) {
            objectname = objectname;// + getVersion();
            dns = dns + "/" + objectname + ".object" + getVersion();
        }
        try {

            getNameService().unbind(getNameService().to_name(dns));
        } catch(NotFound nfe) {
            logger.info("NOT FOUND Exception caught while resolving dns name context");
            throw new NotFound();
        } catch(InvalidName ine) {
            logger.info("INVALID NAME Exception caught while resolving dns name context");
            throw new InvalidName();
        } catch(CannotProceed cpe) {
            logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
            throw new CannotProceed();
        }


    }



    /**
     * returns the NeworkDC object reference in the namingService.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return a <code>NetworkDC</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public NetworkDC getNetworkDC(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        NetworkDC netdc = NetworkDCHelper.narrow(getNetworkDCObject(dns, objectname));
        return netdc;

    }


    /**
     * returns the reference to the SeismogramDC inthe naming service.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return a <code>DataCenter</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public DataCenter getSeismogramDC(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        logger.debug("before get SeismogramDC Object");
        org.omg.CORBA.Object obj = getSeismogramDCObject(dns, objectname);
        logger.debug("before narrow");
        DataCenter datacenter = DataCenterHelper.narrow(obj);
        logger.debug("after narrow");
        return datacenter;

    }


    /**
     * returns the reference to the PlottableDC in the namingService.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return a <code>PlottableDC</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public PlottableDC getPlottableDC(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        PlottableDC plottabledc = PlottableDCHelper.narrow(getPlottableDCObject(dns, objectname));
        return plottabledc;

    }


    /**
     * returns the reference to the EventDC in the namingService.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>EventDC</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public EventDC getEventDC(String dns, String objectname)
        throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        EventDC eventdc = EventDCHelper.narrow(getEventDCObject(dns, objectname));
        return eventdc;

    }

    /**
     * returns the reference to NetworkDC  in the namingService as a CORBA object. The
     * returned CORBA object must be narrowed to NetworkDC.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public org.omg.CORBA.Object getNetworkDCObject(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        org.omg.CORBA.Object obj = resolve(dns, "NetworkDC", objectname);
        return obj;
    }


    /**
     * returns the reference to SeismogramDC  in the namingService as a CORBA object. The
     * returned CORBA object must be narrowed to SeismogramDC.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public org.omg.CORBA.Object getSeismogramDCObject(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        org.omg.CORBA.Object obj = resolve(dns, "DataCenter", objectname);
        return obj;
    }


    /**
     * returns the reference to PlottableDC  in the namingService as a CORBA object. The
     * returned CORBA object must be narrowed to PlottableDC.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public org.omg.CORBA.Object getPlottableDCObject(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        org.omg.CORBA.Object obj = resolve(dns, "PlottableDC", objectname);
        return obj;
    }


    /**
     * returns the reference to EventDC  in the namingService as a CORBA object. The
     * returned CORBA object must be narrowed to EventDC.
     *
     * @param dns a <code>String</code> value
     * @param objectname a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public org.omg.CORBA.Object getEventDCObject(String dns, String objectname)  throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        org.omg.CORBA.Object obj = resolve(dns, "EventDC", objectname);
        return obj;

    }

    /**
     * returns an array of all the CORBA objects registered under the interface name given
     * by interfaceName and path.
     *
     * @param interfaceName a <code>String</code> value
     * @param path a <code>String</code> value
     * @return an <code>org.omg.CORBA.Object[]</code> value
     */
    public org.omg.CORBA.Object[] getAllObjects(String interfaceName, String path) {

        org.omg.CORBA.Object obj;

        ArrayList arrayList = new ArrayList();
        try {
            if(path == null) {
                obj = getRoot();
            } else {
                obj =  getNameService().resolve(getNameService().to_name(path));
            }
            NamingContextExt namingContextTemp = NamingContextExtHelper.narrow(obj);
            BindingListHolder bindingList = new BindingListHolder();
            BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();

            namingContextTemp.list(0, bindingList, bindingIteratorHolder);

            BindingIterator bindingIterator = bindingIteratorHolder.value;
            BindingHolder bindingHolder = new BindingHolder();

            while( bindingIterator.next_one(bindingHolder)) {
                Binding binding = bindingHolder.value;
                String tempPath = new String();

                if(binding.binding_type == BindingType.ncontext) {

                    if(path == null) tempPath = binding.binding_name[0].id+"."+binding.binding_name[0].kind;
                    else tempPath = path + "/"+binding.binding_name[0].id+"."+binding.binding_name[0].kind;
                    org.omg.CORBA.Object[] str;
                    if(binding.binding_name[0].kind.equals("interface") &&
                       binding.binding_name[0].id.equals(interfaceName)) {
                        str = getAllObjects("__END__RECURSION__", tempPath);
                    } else {
                        str = getAllObjects(interfaceName, tempPath);
                    }
                    if(str != null) {
                        for(int i = 0; i < str.length; i++)
                            arrayList.add(str[i]);
                    }
                } else {
                    if(interfaceName.equals("__END__RECURSION__")) {
                        String objectPath = new String();
                        if(path == null) objectPath = binding.binding_name[0].id+"."+binding.binding_name[0].kind;
                        else objectPath = path + "/"+binding.binding_name[0].id+"."+"object"+getVersion();
                        org.omg.CORBA.Object object =  getNameService().resolve(getNameService().to_name(objectPath));
                        arrayList.add(object);

                    }//end of inner if
                }//end of if else
            }//end of while

            org.omg.CORBA.Object[] rtnValues = new org.omg.CORBA.Object[arrayList.size()];

            rtnValues = (org.omg.CORBA.Object[])arrayList.toArray(rtnValues);
            return rtnValues;

        } catch(Exception e) {

            logger.debug("caught Exception "+e);
        }
        return null;

    }

    /**
     * returns an array of all the NetworkDC objects registered with the naming Service.
     *
     * @return a <code>NetworkDC[]</code> value
     */
    public NetworkDC[] getNetworkDCObjects() {
        org.omg.CORBA.Object[] objects = getAllObjects("NetworkDC", null);
        NetworkDC[] networkDCObjects = new NetworkDC[objects.length];
        for(int counter = 0; counter < objects.length; counter++) {
            networkDCObjects[counter] = NetworkDCHelper.narrow(objects[counter]);
        }
        return networkDCObjects;
    }

    /**
     * returns an array of all the EventDC objects registered with the naming service.
     *
     * @return an <code>EventDC[]</code> value
     */
    public EventDC[] getEventDCObjects() {
        org.omg.CORBA.Object[] objects = getAllObjects("EventDC", null);
        EventDC[] eventDCObjects = new EventDC[objects.length];
        for(int counter = 0; counter < objects.length; counter++) {
            eventDCObjects[counter] = EventDCHelper.narrow(objects[counter]);
        }
        return eventDCObjects;
    }

    /**
     * returns an array of all the DataCenter objects registered with the naming service.
     *
     * @return a <code>DataCenter[]</code> value
     */
    public DataCenter[] getDataCenterObjects() {
        org.omg.CORBA.Object[] objects = getAllObjects("DataCenter", null);
        DataCenter[] dataCenterObjects = new DataCenter[objects.length];
        for(int counter = 0; counter < objects.length; counter++) {
            dataCenterObjects[counter] = DataCenterHelper.narrow(objects[counter]);
        }
        return dataCenterObjects;
    }


    /**
     * returns all the interfaceNames(these are namingContexts) registered with the naming Service
     *
     * @param dns a <code>String</code> value
     * @return a <code>String[]</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public String[] getInterfaceNames(String dns) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        dns = appendKindNames(dns);

        return getNames(dns, "interface");

    }

    /**
     * returns the object names registered under the interface name given by interfacename.
     *
     * @param dns a <code>String</code> value
     * @param interfacename a <code>String</code> value
     * @return a <code>String[]</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public String[] getInstanceNames(String dns, String interfacename) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        dns = appendKindNames(dns);
        if(interfacename != null && interfacename.length() != 0)
            dns = dns + "/" + interfacename + ".interface";

        return getNames(dns, "object" + getVersion());

    }


    /**
     * Describe <code>getDNSNames</code> method here.
     *
     * @param dns a <code>String</code> value
     * @return a <code>String[]</code> value
     * @exception NotFound if an error occurs
     * @exception CannotProceed if an error occurs
     * @exception InvalidName if an error occurs
     */
    public String[] getDNSNames(String dns) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {

        String tempdns = new String(dns);
        dns = appendKindNames(dns);

        String[] rtnValues = getNames(dns, "dns");
        for(int counter = 0; counter < rtnValues.length; counter++)
            rtnValues[counter] = tempdns + "/" + rtnValues[counter];

        return rtnValues;

    }

    private String[] getNames(String dns, String key) throws NotFound, CannotProceed, InvalidName, org.omg.CORBA.ORBPackage.InvalidName {
        ArrayList arrayList = new ArrayList();

        try {
            NamingContextExt namingContextTemp = NamingContextExtHelper.narrow(getNameService().resolve(getNameService().to_name(dns)));
            BindingListHolder bindingList = new BindingListHolder();
            BindingIteratorHolder bindingIteratorHolder = new BindingIteratorHolder();

            namingContextTemp.list(0, bindingList, bindingIteratorHolder);

            BindingIterator bindingIterator = bindingIteratorHolder.value;
            BindingHolder bindingHolder = new BindingHolder();

            while( bindingIterator.next_one(bindingHolder)) {
                Binding binding = bindingHolder.value;
                if(binding.binding_name[0].kind.equals(key)) arrayList.add(binding.binding_name[0].id);
            }

            String[] rtnValues = new String[arrayList.size()];
            rtnValues = (String[]) arrayList.toArray(rtnValues);
            return rtnValues;

        } catch(NotFound nfe) {
            logger.info("NOT FOUND Exception caught while resolving dns name context");
            throw new NotFound();
        } catch(InvalidName ine) {
            logger.info("INVALID NAME Exception caught while resolving dns name context");
            throw new InvalidName();
        } catch(CannotProceed cpe) {
            logger.info("CANNOT PROCEED Exception caught while resolving dns name context");
            throw new CannotProceed();
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

    private String nameServiceCorbaLoc = null;

    private java.util.Properties props;
    private org.omg.CORBA_2_3.ORB orb;
    private NamingContextExt namingContext;

    static Category logger = Category.getInstance(FissuresNamingService.class.getName());


}// FissuresNamingService
