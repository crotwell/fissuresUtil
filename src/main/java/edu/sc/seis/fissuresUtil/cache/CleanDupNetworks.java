package edu.sc.seis.fissuresUtil.cache;

import java.util.LinkedList;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.network.NetworkIdUtil;


public class CleanDupNetworks extends ProxyNetworkFinder {

    public CleanDupNetworks(ProxyNetworkFinder nf) {
        super(nf);
    }
    
    NetworkAccess[] filter(NetworkAccess[] nets) {
        LinkedList<CacheNetworkAccess> out = new LinkedList<CacheNetworkAccess>();
        for (int i = 0; i < nets.length; i++) {
            boolean isNew = true;
            for (NetworkAccess prevNet : out) {
                if (NetworkIdUtil.areEqual(nets[i].get_attributes().get_id(), prevNet.get_attributes().get_id())) {
                    // same as already inserted net
                    MicroSecondDate netBegin = new MicroSecondDate(nets[i].get_attributes().getBeginTime());
                    MicroSecondDate prevNetBegin = new MicroSecondDate(prevNet.get_attributes().getBeginTime());
                    if (netBegin.before(prevNetBegin)) {
                        // use net with earlier begin instead
                        out.remove(prevNet);
                    } else {
                        isNew=false;
                    }
                    break;
                } 
            }
            if (isNew) {
                // comes from vested so should be ok to cast
                out.add((CacheNetworkAccess)nets[i]);
            }
        }
        return out.toArray(new CacheNetworkAccess[0]);
    }

    @Override
    public NetworkAccess[] retrieve_all() {        
        return filter(super.retrieve_all());
    }

    @Override
    public NetworkAccess[] retrieve_by_code(String code) throws NetworkNotFound {
        return filter(super.retrieve_by_code(code));
    }

    @Override
    public NetworkAccess[] retrieve_by_name(String name) throws NetworkNotFound {
        // TODO Auto-generated method stub
        return filter(super.retrieve_by_name(name));
    }
    
    
}
