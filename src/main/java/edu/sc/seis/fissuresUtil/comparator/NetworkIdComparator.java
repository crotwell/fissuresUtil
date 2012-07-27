package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkIdUtil;


public class NetworkIdComparator implements Comparator<NetworkId> {

    public int compare(NetworkId n1, NetworkId n2) {
        int out = n1.network_code.compareTo(n2.network_code);
        if (out == 0 && NetworkIdUtil.isTemporary(n1)) {
            // codes equal, so compare start times if temp nets
           MicroSecondDate n1Start = new MicroSecondDate(n1.begin_time);
           MicroSecondDate n2Start = new MicroSecondDate(n2.begin_time);
           return n1Start.compareTo(n2Start);
        }
        return out;
    }
    
    
}
