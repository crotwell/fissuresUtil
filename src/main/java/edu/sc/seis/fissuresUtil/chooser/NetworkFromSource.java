package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.network.NetworkAttrImpl;

public class NetworkFromSource {

    public NetworkAttrImpl getNetAttr() {
        return netAttr;
    }

    public ChannelChooserSource getSource() {
        return source;
    }

    public NetworkFromSource(NetworkAttrImpl netAttr, ChannelChooserSource source) {
        super();
        this.netAttr = netAttr;
        this.source = source;
    }

    public String toString() {
        return netAttr.toString();
    }
    
    NetworkAttrImpl netAttr;

    ChannelChooserSource source;
}
