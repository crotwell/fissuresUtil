package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.COMM_FAILURE;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkFinder;


public class GoodOrEvilNetworkFinder extends MockNetworkFinder {
    
    public GoodOrEvilNetworkFinder(boolean evil) {
        this.evil = evil;
    }
    
    public NetworkAccess retrieve_by_id(NetworkId id) throws NetworkNotFound {
        System.out.println("retrieve_by_id called.  evil = " + evil);
        if (evil) {
            throw new COMM_FAILURE();
        } else {
            return super.retrieve_by_id(id);
        }
    }

    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        System.out.println("retrieve_by_code called.  evil = " + evil);
        if (evil) {
            throw new COMM_FAILURE();
        } else {
            return super.retrieve_by_code(code);
        }
    }

    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        System.out.println("retrieve_by_name called.  evil = " + evil);
        if (evil) {
            throw new COMM_FAILURE();
        } else {
            return super.retrieve_by_name(name);
        }
    }
    
    private boolean evil;
    
}
