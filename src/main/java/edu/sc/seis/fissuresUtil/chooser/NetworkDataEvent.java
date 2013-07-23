/**
 * NetworkDataEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.network.NetworkAttrImpl;

public class NetworkDataEvent
{
    Object source;
    NetworkFromSource netSource;
    
    public NetworkDataEvent(Object source, NetworkFromSource networkSource) {
        this.source = source;
        this.netSource = networkSource;
    }

    public Object getSource(){
        return source;
    }

    public NetworkAttrImpl getNetwork(){
        return netSource.getNetAttr();
    }

    public ChannelChooserSource getNetworkSource() {
        return netSource.getSource();
    }
    
    public NetworkFromSource getNetworkFromSource() {
        return netSource;
    }
}

