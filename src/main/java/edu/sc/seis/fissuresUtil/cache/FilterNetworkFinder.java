package edu.sc.seis.fissuresUtil.cache;

import java.util.regex.Pattern;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

/**
 * @author groves Created on Dec 2, 2004
 */
public class FilterNetworkFinder extends ProxyNetworkFinder {

    public FilterNetworkFinder(NetworkFinder nf, Pattern[] patterns) {
        super(nf);
        this.patterns = patterns;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        return make(getWrappedNetworkFinder().retrieve_by_id(id));
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return wrap(getWrappedNetworkFinder().retrieve_by_code(code));
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return wrap(getWrappedNetworkFinder().retrieve_by_name(name));
    }

    public NetworkAccess[] retrieve_all() {
        return wrap(getWrappedNetworkFinder().retrieve_all());
    }

    private FilterNetworkAccess[] wrap(NetworkAccess[] nas) {
        FilterNetworkAccess[] filters = new FilterNetworkAccess[nas.length];
        for(int i = 0; i < filters.length; i++) {
            filters[i] = make(nas[i]);
        }
        return filters;
    }

    private FilterNetworkAccess make(NetworkAccess na) {
        return new FilterNetworkAccess(na, patterns);
    }

    // needed to compile under java11?
    public org.omg.CORBA.InterfaceDef _get_interface() {
      throw new RuntimeException("should never be called");
    }
    public org.omg.CORBA.Object _get_component() {
      throw new RuntimeException("should never be called");
    }

    private Pattern[] patterns;

    private ProxyNetworkDC myCreator;
}
