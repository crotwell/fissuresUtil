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
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

/**
 * Wrapper for NetworkFinders that pass all of the interesting real
 * NetworkFinder calls down to the wrapped finder. For all of the CORBA system
 * type calls that we're required to implement since this must be a
 * networkfinder and not a NetworkFinderOperations we just throw a NO_IMPLEMENT
 * if there isn't a real NetworkFinder somehwere in the stack of Proxies. if
 * there is one, we just use it to return things like _non_existent() Bearing
 * that in mind, if there isn't a real corba object at the base, please don't
 * try to send an instance of this class over the wire
 * 
 * @author groves Created on Dec 2, 2004
 */
public abstract class ProxyNetworkFinder implements NetworkFinder, CorbaServerWrapper{

    public ProxyNetworkFinder() {
        this(null);
    }

    public ProxyNetworkFinder(NetworkFinder nf) {
        this.nf = nf;
    }

    /**
     * If this ProxyNetworkFinder is holding onto a ProxyNetworkFinder, it calls
     * reset on that network finder. Otherwise it just falls through.
     */
    public void reset() {
        if(nf instanceof ProxyNetworkFinder) {
            ((ProxyNetworkFinder)nf).reset();
        } else {
            // must be real corba object, so discard to allow orb to reclaim/reopen sockets after garbage collection
            nf = null;
        }
    }

    public boolean hasCorbaObject() {
        return getCorbaObject() != null;
    }

    public NetworkFinder getCorbaObject() {
        if(nf instanceof ProxyNetworkFinder) {
            return ((ProxyNetworkFinder)nf).getCorbaObject();
        } else if(!(nf instanceof ProxyNetworkFinder)) { return nf; }
        return null;
    }
    
    public String getServerName() {
        if(nf instanceof ProxyNetworkFinder) {
            return ((ProxyNetworkFinder)nf).getServerName();
        }
        return null;
    }

    public String getServerDNS() {
        if(nf instanceof ProxyNetworkFinder) {
            return ((ProxyNetworkFinder)nf).getServerDNS();
        }
        return null;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }


    public String getServerType() {
        return NETFINDER_TYPE;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        return nf.retrieve_by_id(id);
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return nf.retrieve_by_code(code);
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return nf.retrieve_by_name(name);
    }

    public NetworkAccess[] retrieve_all() {
        return nf.retrieve_all();
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

    protected NetworkFinder nf;
}