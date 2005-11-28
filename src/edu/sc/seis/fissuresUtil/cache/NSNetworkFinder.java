package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;

public class NSNetworkFinder extends ProxyNetworkFinder {

    public NSNetworkFinder(ProxyNetworkDC netDC) {
        super();
        this.netDC = netDC;
    }

    private synchronized ProxyNetworkFinder getFinder() {
        if(finder == null) {
            finder = new RetryNetworkFinder(new SynchronizedNetworkFinder(netDC.a_finder()),
                                            3);
        }
        return finder;
    }

    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        try {
            return getFinder().retrieve_by_id(id);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_by_id(), regetting from nameservice to try again.",
                        e);
            reset();
            return getFinder().retrieve_by_id(id);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        try {
            return getFinder().retrieve_by_code(code);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_by_code(), regetting from nameservice to try again.",
                        e);
            reset();
            return getFinder().retrieve_by_code(code);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        try {
            return getFinder().retrieve_by_name(name);
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_by_name(), regetting from nameservice to try again.",
                        e);
            reset();
            return getFinder().retrieve_by_name(name);
        } // end of try-catch
    }

    public NetworkAccess[] retrieve_all() {
        try {
            return getFinder().retrieve_all();
        } catch(Throwable e) {
            // retry in case regetting from name service helps
            logger.warn("Exception in retrieve_all(), regetting from nameservice to try again.",
                        e);
            reset();
            return getFinder().retrieve_all();
        } // end of try-catch
    }

    /**
     * the networkfinder gets revested in a retry here. this stays in line with
     * what happens in BulletProofVestFactory.vestNetworkFinder. just be aware
     * that any changes there need to be checked here, as well.
     */
    public void reset() {
        netDC.reset();
        finder = null;
    }

    public boolean hasCorbaObject() {
        return getCorbaObject() != null;
    }

    public NetworkFinder getCorbaObject() {
        return getFinder().getCorbaObject();
    }

    private static Logger logger = Logger.getLogger(NSNetworkFinder.class);

    ProxyNetworkDC netDC;

    private ProxyNetworkFinder finder;
}
