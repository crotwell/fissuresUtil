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
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkIdUtil;

/**
 * @author Charlie Groves
 */
public class MockNetworkFinder implements NetworkFinder {

    public NetworkAccess retrieve_by_id(NetworkId arg0) throws NetworkNotFound {
        if (NetworkIdUtil.areEqual(arg0, net.get_attributes().get_id())) {
            return net;
        } else if (NetworkIdUtil.areEqual(arg0, other.get_attributes().get_id())) { return other; }
        throw new NetworkNotFound(getExceptionString());
    }

    public NetworkAccess[] retrieve_by_code(String arg0) throws NetworkNotFound {
        if (net.get_attributes().get_code().equals(arg0)) {
            return new NetworkAccess[] { net };
        } else if (other.get_attributes().get_code().equals(arg0)) { return new NetworkAccess[] { other }; }
        throw new NetworkNotFound(getExceptionString());
    }

    public NetworkAccess[] retrieve_by_name(String arg0) throws NetworkNotFound {
        if (net.get_attributes().name.equals(arg0)) {
            return new NetworkAccess[] { net };
        } else if (other.get_attributes().name.equals(arg0)) { return new NetworkAccess[] { other }; }
        throw new NetworkNotFound(getExceptionString());
    }

    private String getExceptionString() {
        return "I only have two networks, "
                + NetworkIdUtil.toString(net.get_attributes().get_id())
                + " and "
                + NetworkIdUtil.toString(other.get_attributes().get_id());
    }

    public NetworkAccess[] retrieve_all() {
        return new NetworkAccess[] { net, other };
    }

    private NetworkAccess net = MockNetworkAccess.createNetworkAccess();

    private NetworkAccess other = MockNetworkAccess.createOtherNetworkAccess();

    public void _release() {
    // TODO Auto-generated method stub

    }

    public boolean _non_existent() {
        // TODO Auto-generated method stub
        return false;
    }

    public int _hash(int maximum) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean _is_a(String repositoryIdentifier) {
        // TODO Auto-generated method stub
        return false;
    }

    public DomainManager[] _get_domain_managers() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object _duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object _get_interface_def() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean _is_equivalent(Object other) {
        // TODO Auto-generated method stub
        return false;
    }

    public Policy _get_policy(int policy_type) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _request(String operation) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object _set_policy_override(Policy[] policies,
            SetOverrideType set_add) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _create_request(Context ctx, String operation,
            NVList arg_list, NamedValue result) {
        // TODO Auto-generated method stub
        return null;
    }

    public Request _create_request(Context ctx, String operation,
            NVList arg_list, NamedValue result, ExceptionList exclist,
            ContextList ctxlist) {
        // TODO Auto-generated method stub
        return null;
    }
}