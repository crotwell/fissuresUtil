package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;


public class InstrumentationInvalid extends RuntimeException {

    private ChannelId id;
    
    public ChannelId getChannelId() {
        return id;
    }
    
    public String toString() {
        return ChannelIdUtil.toString(getChannelId())+" "+super.toString();
    }
    
    public InstrumentationInvalid(ChannelId id) {
        super();
        this.id = id;
    }

    public InstrumentationInvalid(ChannelId id, String message) {
        super(message);
        this.id = id;
    }

    public InstrumentationInvalid(ChannelId id, String message, Throwable cause) {
        super(message, cause);
        this.id = id;
    }

    public InstrumentationInvalid(ChannelId id, Throwable cause) {
        super(cause);
        this.id = id;
    }
}
