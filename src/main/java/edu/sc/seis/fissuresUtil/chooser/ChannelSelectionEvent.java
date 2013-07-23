package edu.sc.seis.fissuresUtil.chooser;

/**
 * ChannelSelectionEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.iris.Fissures.network.ChannelImpl;

public class ChannelSelectionEvent{
    public ChannelSelectionEvent(ChannelImpl[] channels){
        this.channels = channels;
    }

    public ChannelImpl[] getSelectedChannels(){ return channels; }

    private ChannelImpl[] channels;
}

