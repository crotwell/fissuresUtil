package edu.sc.seis.fissuresUtil.namingService;

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
 * @deprecated Please use the FissuresNamingService class instead, there is no
 * need for interface/impl with this class.
 */

public class FissuresNamingServiceImpl extends FissuresNamingService {
    /**
     * Creates a new <code>FissuresNamingServiceImpl</code> instance.
     *
     * @param props a <code>java.util.Properties</code> value
     * @exception InvalidName if an error occurs
     * @deprecated Please use FissuresNamingService instead
     */

    public FissuresNamingServiceImpl (java.util.Properties props) {
        super(props);
    }

    /**
     * Creates a new <code>FissuresNamingServiceImpl</code> instance.
     *
     * @param orb an <code>org.omg.CORBA_2_3.ORB</code> value
     * @exception InvalidName if an error occurs
     * @deprecated Please use FissuresNamineService instead
     */
    public FissuresNamingServiceImpl(org.omg.CORBA_2_3.ORB orb) {
        super(orb);
    }

}// FissuresNamingServiceImpl
