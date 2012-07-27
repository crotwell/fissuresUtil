/*
 * Created on Jul 20, 2004
 */
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

import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.Locator;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.IfParameterMgr.ParameterComponent;
import edu.iris.Fissures.event.OriginImpl;

/**
 * @author oliverpa
 */
public abstract class ProxyEventAccessOperations implements
        EventAccess, CorbaServerWrapper {

    public EventAccess getCorbaObject() {
        if(event instanceof ProxyEventAccessOperations) {
            return ((ProxyEventAccessOperations)event).getCorbaObject();
        } else {
            return (EventAccess)event;
        }
    }


    public String getServerDNS() {
        if(getWrappedEventAccess() instanceof ProxyEventAccessOperations) {
            return ((ProxyEventAccessOperations)getWrappedEventAccess()).getServerDNS();
        }
        return null;
    }

    public String getServerName() {
        if(getWrappedEventAccess() instanceof ProxyEventAccessOperations) {
            return ((ProxyEventAccessOperations)getWrappedEventAccess()).getServerName();
        }
        return null;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }

    public String getServerType() {
        return EVENTACCESS_TYPE;
    }

    public boolean hasCorbaObject() {
        return getCorbaObject() != null;
    }

    public EventAccessOperations getWrappedEventAccess() {
        return event;
    }

    public EventAccessOperations getWrappedEventAccess(Class wrappedClass) {
        if(getClass().equals(wrappedClass)) {
            return this;
        }
        if(getWrappedEventAccess().getClass().equals(wrappedClass)) {
            return getWrappedEventAccess();
        } else if(getWrappedEventAccess().getClass()
                .equals(ProxyEventAccessOperations.class)) {
            return ((ProxyEventAccessOperations)getWrappedEventAccess()).getWrappedEventAccess(wrappedClass);
        }
        throw new IllegalArgumentException("This doesn't contain an Event of class "
                + wrappedClass);
    }

    public void reset() {
        if(event instanceof ProxyEventAccessOperations) {
            ((ProxyEventAccessOperations)event).reset();
        }
    }

    public EventAccessOperations getEventAccess() {
        if(event instanceof ProxyEventAccessOperations) {
            return ((ProxyEventAccessOperations)event).getEventAccess();
        } else {
            return event;
        }
    }

    protected void setEventAccess(EventAccessOperations evo) {
        event = evo;
    }

    public Event a_writeable() {
        if(event != null) {
            return event.a_writeable();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public ParameterComponent parm_svc() {
        if(event != null) {
            return event.parm_svc();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventAttr get_attributes() {
        return event.get_attributes();
    }

    public Origin[] get_origins() {
        return event.get_origins();
    }

    public Origin get_origin(String the_origin) throws OriginNotFound {
        return event.get_origin(the_origin);
    }

    public Origin get_preferred_origin() throws NoPreferredOrigin {
        return event.get_preferred_origin();
    }

    public Locator[] get_locators(String an_origin) throws OriginNotFound,
            NotImplemented {
        if(event != null) {
            return event.get_locators(an_origin);
        }
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public AuditElement[] get_audit_trail_for_origin(String the_origin)
            throws OriginNotFound, NotImplemented {
        if(event != null) {
            return event.get_audit_trail_for_origin(the_origin);
        }
        throw new NotImplemented();
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        if(event != null) {
            return event.get_audit_trail();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventFactory a_factory() {
        if(event != null) {
            return event.a_factory();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventFinder a_finder() {
        if(event != null) {
            return event.a_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventChannelFinder a_channel_finder() {
        if(event != null) {
            return event.a_channel_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(getEventAccess() != null
                && o instanceof ProxyEventAccessOperations
                && ((ProxyEventAccessOperations)o).getEventAccess() != null
                && getEventAccess().equals(((ProxyEventAccessOperations)o).getEventAccess())) {
            return true;
        } else if(o instanceof EventAccessOperations) {
            EventAccessOperations oEvent = (EventAccessOperations)o;
            if(get_attributes().equals(oEvent.get_attributes())) {
                OriginImpl thisOrigin = getOrigin();
                OriginImpl otherOrigin = EventUtil.extractOrigin(oEvent);
                if(thisOrigin == otherOrigin) {
                    return true;
                } else if(thisOrigin != null
                        && thisOrigin.equals(EventUtil.extractOrigin(oEvent))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * does an equals, except origin times within 1 millisecond are judged to be
     * the same.
     */
    public boolean close(EventAccessOperations event) {
        if(get_attributes().equals(event.get_attributes())
                && getOrigin() instanceof OriginImpl
                && EventUtil.extractOrigin(event) instanceof OriginImpl
                && ((OriginImpl)getOrigin()).close(((OriginImpl)EventUtil.extractOrigin(event)))) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if(!hashSet) {
            int result = 52;
            result = 48 * result + getOrigin().hashCode();
            result = 48 * result + get_attributes().hashCode();
            hashValue = result;
            hashSet = true;
        }
        return hashValue;
    }

    public OriginImpl getOrigin() {
        return EventUtil.extractOrigin(this);
    }

    public String toString() {
        return EventUtil.getEventInfo(this);
    }

    public void _release() {
        if(hasCorbaObject()) {
            getCorbaObject()._release();
        }else{
        throw new NO_IMPLEMENT();
        }
    }

    public boolean _non_existent() {
        if(hasCorbaObject()) { return getCorbaObject()._non_existent(); }
        throw new NO_IMPLEMENT();
    }

    public int _hash(int maximum) {
        if(hasCorbaObject()) { return getCorbaObject()._hash(maximum); }
        throw new NO_IMPLEMENT();
    }

    public boolean _is_a(String repositoryIdentifier) {
        if(hasCorbaObject()) { return getCorbaObject()._is_a(repositoryIdentifier); }
        throw new NO_IMPLEMENT();
    }

    public DomainManager[] _get_domain_managers() {
        if(hasCorbaObject()) { return getCorbaObject()._get_domain_managers(); }
        throw new NO_IMPLEMENT();
    }

    public Object _duplicate() {
        if(hasCorbaObject()) { return getCorbaObject()._duplicate(); }
        throw new NO_IMPLEMENT();
    }

    public Object _get_interface_def() {
        if(hasCorbaObject()) { return getCorbaObject()._get_interface_def(); }
        throw new NO_IMPLEMENT();
    }

    public boolean _is_equivalent(Object other) {
        if(hasCorbaObject()) { return getCorbaObject()._is_equivalent(other); }
        throw new NO_IMPLEMENT();
    }

    public Policy _get_policy(int policy_type) {
        if(hasCorbaObject()) { return getCorbaObject()._get_policy(policy_type); }
        throw new NO_IMPLEMENT();
    }

    public Request _request(String operation) {
        if(hasCorbaObject()) { return getCorbaObject()._request(operation); }
        throw new NO_IMPLEMENT();
    }

    public Object _set_policy_override(Policy[] policies,
                                       SetOverrideType set_add) {
        if(hasCorbaObject()) { return getCorbaObject()._set_policy_override(policies,
                                                                            set_add); }
        throw new NO_IMPLEMENT();
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result) {
        if(hasCorbaObject()) { return getCorbaObject()._create_request(ctx,
                                                                       operation,
                                                                       arg_list,
                                                                       result); }
        throw new NO_IMPLEMENT();
    }

    public Request _create_request(Context ctx,
                                   String operation,
                                   NVList arg_list,
                                   NamedValue result,
                                   ExceptionList exclist,
                                   ContextList ctxlist) {
        if(hasCorbaObject()) { return getCorbaObject()._create_request(ctx,
                                                                       operation,
                                                                       arg_list,
                                                                       result,
                                                                       exclist,
                                                                       ctxlist); }
        throw new NO_IMPLEMENT();
    }

    private boolean hashSet = false;

    private int hashValue;

    protected EventAccessOperations event;

}