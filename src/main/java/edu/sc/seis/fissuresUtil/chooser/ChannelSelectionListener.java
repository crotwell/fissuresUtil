package edu.sc.seis.fissuresUtil.chooser;

/**
 * ChannelSelectionListener.java
 *
 * @author Created by Charlie Groves
 */

import java.util.EventListener;

public interface ChannelSelectionListener extends EventListener{
    public void channelSelectionChanged(ChannelSelectionEvent e);
}

