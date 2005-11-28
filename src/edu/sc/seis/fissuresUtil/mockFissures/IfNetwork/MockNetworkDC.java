/*
 * Created on Jul 19, 2004
 */
package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;
import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.sc.seis.fissuresUtil.cache.AbstractProxyNetworkDC;

/**
 * @author Charlie Groves
 */
public class MockNetworkDC extends AbstractProxyNetworkDC implements NetworkDC {

    public NetworkFinder a_finder() {
        return finder;
    }

    protected NetworkFinder finder = new MockNetworkFinder();

    public boolean _is_a(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean _is_equivalent(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean _non_existent() {
        // TODO Auto-generated method stub
        return false;
    }

    public int _hash(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Object _duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    public void _release() {
    // TODO Auto-generated method stub
    }

    public Object _get_interface_def() {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _request(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _create_request(Context arg0,
                                   String arg1,
                                   NVList arg2,
                                   NamedValue arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _create_request(Context arg0,
                                   String arg1,
                                   NVList arg2,
                                   NamedValue arg3,
                                   ExceptionList arg4,
                                   ContextList arg5) {
        // TODO Auto-generated method stub
        return null;
    }

    public Policy _get_policy(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public DomainManager[] _get_domain_managers() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object _set_policy_override(Policy[] arg0, SetOverrideType arg1) {
        // TODO Auto-generated method stub
        return null;
    }
}
