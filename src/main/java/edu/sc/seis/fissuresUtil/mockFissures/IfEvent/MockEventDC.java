package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

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

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventDC;
import edu.iris.Fissures.IfEvent.EventFinder;


/**
 * @author groves
 * Created on Nov 9, 2004
 */
public class MockEventDC implements EventDC{

    public EventFinder a_finder() {
        return new MockEventFinder();
    }

    public EventChannelFinder a_channel_finder() {
        return null;
    }

    public void _release() {}

    public boolean _non_existent() {
        return false;
    }

    public int _hash(int maximum) {
        return 0;
    }

    public boolean _is_a(String repositoryIdentifier) {
        return false;
    }

    public DomainManager[] _get_domain_managers() {
        return null;
    }

    public Object _duplicate() {
        return null;
    }

    public Object _get_interface_def() {
        return null;
    }

    public boolean _is_equivalent(Object other) {
        return false;
    }

    public Policy _get_policy(int policy_type) {
        return null;
    }

    public Request _request(String operation) {
        return null;
    }

    public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {
        return null;
    }

    public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result) {
        return null;
    }

    public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result, ExceptionList exclist, ContextList ctxlist) {
        return null;
    }

    // needed to compile under java11?
    public org.omg.CORBA.InterfaceDef _get_interface() {
      throw new RuntimeException("should never be called");
    }
    public org.omg.CORBA.Object _get_component() {
      throw new RuntimeException("should never be called");
    }
}
