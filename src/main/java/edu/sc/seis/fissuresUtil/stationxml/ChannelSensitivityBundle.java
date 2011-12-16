package edu.sc.seis.fissuresUtil.stationxml;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;


public class ChannelSensitivityBundle {
    
    public ChannelSensitivityBundle(ChannelImpl chan, QuantityImpl sensitivity) {
        super();
        this.chan = chan;
        this.sensitivity = sensitivity;
    }

    public ChannelImpl getChan() {
        return chan;
    }
    
    public QuantityImpl getSensitivity() {
        return sensitivity;
    }
    
    private ChannelImpl chan;
    private QuantityImpl sensitivity;
}
