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

    public FilterNetworkFinder(NetworkFinder nf, ProxyNetworkDC myCreator,
            Pattern[] patterns) {
        super(nf);
        this.myCreator = myCreator;
        this.patterns = patterns;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        return make(nf.retrieve_by_id(id));
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return wrap(nf.retrieve_by_code(code));
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        return wrap(nf.retrieve_by_name(name));
    }

    public NetworkAccess[] retrieve_all() {
        return wrap(nf.retrieve_all());
    }

    private FilterNetworkAccess[] wrap(NetworkAccess[] nas) {
        FilterNetworkAccess[] filters = new FilterNetworkAccess[nas.length];
        for(int i = 0; i < filters.length; i++) {
            filters[i] = make(nas[i]);
        }
        return filters;
    }

    private FilterNetworkAccess make(NetworkAccess na) {
        NetworkAccess vested = BulletproofVestFactory.vestNetworkAccess(na,
                                                                        myCreator);
        return new FilterNetworkAccess(vested, patterns);
    }

    private Pattern[] patterns;

    private ProxyNetworkDC myCreator;
}