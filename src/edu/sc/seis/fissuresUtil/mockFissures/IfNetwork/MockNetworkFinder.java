/*
 * Created on Jul 19, 2004
 */
package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkFinder;

/**
 * @author Charlie Groves
 */
public class MockNetworkFinder extends ProxyNetworkFinder {

    private NetworkAccess[] nets = new NetworkAccess[] {MockNetworkAccess.createNetworkAccess(),
                                                        MockNetworkAccess.createOtherNetworkAccess(),
                                                        MockNetworkAccess.createManySplendoredNetworkAccess()};

    public NetworkAccess retrieve_by_id(NetworkId arg0) throws NetworkNotFound {
        for(int i = 0; i < nets.length; i++) {
            NetworkId curId = nets[i].get_attributes().get_id();
            if(NetworkIdUtil.areEqual(arg0, curId)) { return nets[i]; }
        }
        throw new NetworkNotFound(getExceptionString());
    }

    public NetworkAccess[] retrieve_by_code(String arg0) throws NetworkNotFound {
        for(int i = 0; i < nets.length; i++) {
            String curCode = nets[i].get_attributes().get_code();
            if(curCode.equals(arg0)) { return new NetworkAccess[] {nets[i]}; }
        }
        throw new NetworkNotFound(getExceptionString());
    }

    public NetworkAccess[] retrieve_by_name(String arg0) throws NetworkNotFound {
        for(int i = 0; i < nets.length; i++) {
            String curName = nets[i].get_attributes().name;
            if(curName.equals(arg0)) { return new NetworkAccess[] {nets[i]}; }
        }
        throw new NetworkNotFound(getExceptionString());
    }

    private String getExceptionString() {
        String exceptionString = "I only have " + nets.length + " networks: ";
        for(int i = 0; i < nets.length; i++) {
            NetworkId curId = nets[i].get_attributes().get_id();
            exceptionString += NetworkIdUtil.toString(curId) + " ";
        }
        return exceptionString;
    }

    public NetworkAccess[] retrieve_all() {
        return nets;
    }
}