package edu.sc.seis.fissuresUtil.cache;

import java.util.regex.Pattern;
import org.omg.CORBA.NO_IMPLEMENT;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;
import edu.iris.Fissures.IfNetwork.NetworkExplorer;
import edu.iris.Fissures.IfNetwork.NetworkFinder;

/**
 * @author groves Created on Dec 1, 2004
 */
public class FilterNetworkDC extends AbstractProxyNetworkDC {

    public FilterNetworkDC(NetworkDCOperations wrappedDC, Pattern[] patterns) {
        super(wrappedDC);
        this.patterns = patterns;
    }

    public NetworkExplorer a_explorer() {
        throw new NO_IMPLEMENT();
    }

    public NetworkFinder a_finder() {
        return new FilterNetworkFinder(getWrappedDC().a_finder(), this, patterns);
    }

    private Pattern[] patterns;
}