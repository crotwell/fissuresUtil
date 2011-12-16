/**
 * NetworkDataEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.NetworkAccess;

public class NetworkDataEvent
{
    Object source;
    NetworkAccess network;

    public NetworkDataEvent(Object source, NetworkAccess network){
        this.source = source;
        this.network = network;
    }

    public Object getSource(){
        return source;
    }

    public NetworkAccess getNetwork(){
        return network;
    }
}

