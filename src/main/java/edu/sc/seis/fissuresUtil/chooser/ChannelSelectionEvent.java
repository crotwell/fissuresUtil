package edu.sc.seis.fissuresUtil.chooser;

/**
 * ChannelSelectionEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.iris.Fissures.IfNetwork.Channel;

public class ChannelSelectionEvent{
    public ChannelSelectionEvent(Channel[] channels){
        this.channels = channels;
    }

    public Channel[] getSelectedChannels(){ return channels; }

    private Channel[] channels;
}

