package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.iris.Fissures.IfNetwork.NetworkAttr;

public class NetworkAttrComparator implements Comparator<NetworkAttr> {

    NetworkIdComparator idCompare = new NetworkIdComparator();

    public int compare(NetworkAttr o1, NetworkAttr o2) {
        return idCompare.compare(o1.getId(), o2.getId());
    }
}
