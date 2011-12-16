package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;

public class ProxyEventFinder implements EventFinder, CorbaServerWrapper {

    public ProxyEventFinder() {}

    public ProxyEventFinder(EventFinder ef) {
        this.ef = ef;
    }

    
    public EventFinder getWrappedEventFinder() {
        return ef;
    }
    
    public String getServerDNS() {
        if(getWrappedEventFinder() instanceof ProxyEventFinder) {
            return ((ProxyEventFinder)getWrappedEventFinder()).getServerDNS();
        }
        return null;
    }

    public String getServerName() {
        if(getWrappedEventFinder() instanceof ProxyEventFinder) {
            return ((ProxyEventFinder)getWrappedEventFinder()).getServerName();
        }
        return null;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }


    public String getServerType() {
        return EVENTFINDER_TYPE;
    }

    public void reset() {
        if(getWrappedEventFinder() instanceof ProxyEventFinder) {
            ((ProxyEventFinder)getWrappedEventFinder()).reset();
        }
    }

    public boolean hasCorbaObject() {
        return getCorbaObject() != null;
    }

    public EventFinder getCorbaObject() {
        if(getWrappedEventFinder() instanceof ProxyEventFinder) {
            return ((ProxyEventFinder)getWrappedEventFinder()).getCorbaObject();
        } else if(!(getWrappedEventFinder() instanceof ProxyEventFinder)) {
            return getWrappedEventFinder();
        }
        return null;
    }

    public EventAccess[] get_by_name(String name) {
        return getWrappedEventFinder().get_by_name(name);
    }

    public String[] catalogs_from(String contrib) {
        return getWrappedEventFinder().catalogs_from(contrib);
    }

    public String[] known_catalogs() {
        return getWrappedEventFinder().known_catalogs();
    }

    public String[] known_contributors() {
        return getWrappedEventFinder().known_contributors();
    }

    public EventAccess[] query_events(Area the_area,
                                      Quantity min_depth,
                                      Quantity max_depth,
                                      TimeRange time_range,
                                      String[] search_types,
                                      float min_magnitude,
                                      float max_magnitude,
                                      String[] catalogs,
                                      String[] contributors,
                                      int seq_max,
                                      EventSeqIterHolder iter) {
        return getWrappedEventFinder().query_events(the_area,
                                             min_depth,
                                             max_depth,
                                             time_range,
                                             search_types,
                                             min_magnitude,
                                             max_magnitude,
                                             catalogs,
                                             contributors,
                                             seq_max,
                                             iter);
    }

    public EventFactory a_factory() {
        return getWrappedEventFinder().a_factory();
    }

    public EventChannelFinder a_channel_finder() {
        return getWrappedEventFinder().a_channel_finder();
    }

    public EventFinder a_finder() {
        return this;
    }

    public void _release() {
        if(hasCorbaObject()) {
            getCorbaObject()._release();
        } else {
            throw new NO_IMPLEMENT();
        }
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result) {
        if(hasCorbaObject()) {
            getCorbaObject()._create_request(ctx, operation, arg_list, result);
        }
        throw new NO_IMPLEMENT();
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result,
                                   ExceptionList exclist,
                                   ContextList ctxlist) {
        if(hasCorbaObject()) {
            getCorbaObject()._create_request(ctx,
                                             operation,
                                             arg_list,
                                             result,
                                             exclist,
                                             ctxlist);
        }
        throw new NO_IMPLEMENT();
    }

    public Object _duplicate() {
        if(hasCorbaObject()) {
            getCorbaObject()._duplicate();
        }
        throw new NO_IMPLEMENT();
    }

    public DomainManager[] _get_domain_managers() {
        if(hasCorbaObject()) {
            getCorbaObject()._get_domain_managers();
        }
        throw new NO_IMPLEMENT();
    }

    public Object _get_interface_def() {
        if(hasCorbaObject()) {
            getCorbaObject()._get_interface_def();
        }
        throw new NO_IMPLEMENT();
    }

    public Policy _get_policy(int policy_type) {
        if(hasCorbaObject()) {
            getCorbaObject()._get_policy(policy_type);
        }
        throw new NO_IMPLEMENT();
    }

    public int _hash(int maximum) {
        if(hasCorbaObject()) {
            getCorbaObject()._hash(maximum);
        }
        throw new NO_IMPLEMENT();
    }

    public boolean _is_a(String repositoryIdentifier) {
        if(hasCorbaObject()) {
            getCorbaObject()._is_a(repositoryIdentifier);
        }
        throw new NO_IMPLEMENT();
    }

    public boolean _is_equivalent(Object other) {
        if(hasCorbaObject()) {
            getCorbaObject()._is_equivalent(other);
        }
        throw new NO_IMPLEMENT();
    }

    public boolean _non_existent() {
        if(hasCorbaObject()) {
            getCorbaObject()._non_existent();
        }
        throw new NO_IMPLEMENT();
    }

    public Request _request(String operation) {
        if(hasCorbaObject()) {
            getCorbaObject()._request(operation);
        }
        throw new NO_IMPLEMENT();
    }

    public Object _set_policy_override(Policy[] policies,
                                       SetOverrideType set_add) {
        if(hasCorbaObject()) {
            getCorbaObject()._set_policy_override(policies, set_add);
        }
        throw new NO_IMPLEMENT();
    }

    protected EventFinder ef;
}
