package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class NSNetworkFinder extends ProxyNetworkFinder {

    public NSNetworkFinder(ProxyNetworkDC netDC) {
        super();
        this.netDC = netDC;
    }

    public synchronized NetworkFinder getWrappedNetworkFinder() {
        if(nf == null) {
            nf = new SynchronizedNetworkFinder(netDC.a_finder());
        }
        return nf;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        try {
            return super.retrieve_by_id(id);
        } catch(Throwable e) {
            reset();
            return super.retrieve_by_id(id);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        try {
            return super.retrieve_by_code(code);
        } catch(Throwable e) {
            reset();
            return super.retrieve_by_code(code);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        try {
            return super.retrieve_by_name(name);
        } catch(Throwable e) {
            reset();
            return super.retrieve_by_name(name);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_all() {
        try {
            return super.retrieve_all();
        } catch(Throwable e) {
            reset();
            return super.retrieve_all();
        } // end of try-catch
    }

    /**
     * the networkfinder gets revested in a retry here. this stays in line with
     * what happens in BulletProofVestFactory.vestNetworkFinder. just be aware
     * that any changes there need to be checked here, as well.
     */
    public void reset() {
        super.reset();
        netDC.reset();
        nf = null; // so we can create a new SynchronizedNetworkFinder
    }

    public String getServerDNS() {
        return netDC.getServerDNS();
    }

    public String getServerName() {
        return netDC.getServerName();
    }

    ProxyNetworkDC netDC;

}
