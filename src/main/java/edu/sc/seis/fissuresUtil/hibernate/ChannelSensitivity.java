package edu.sc.seis.fissuresUtil.hibernate;

import edu.iris.Fissures.network.ChannelImpl;


public class ChannelSensitivity {
    
    public ChannelSensitivity() {}

    public ChannelSensitivity(ChannelImpl channel, float overallGain, float frequency, String inputUnits) {
        super();
        this.channel = channel;
        this.overallGain = overallGain;
        this.frequency = frequency;
        this.inputUnits = inputUnits;
    }
    
    public ChannelImpl getChannel() {
        return channel;
    }

    public float getOverallGain() {
        return overallGain;
    }
    
    public float getFrequency() {
        return frequency;
    }
    
    public String getInputUnits() {
        return inputUnits;
    }
    
    protected void setChannel(ChannelImpl channel) {
        this.channel = channel;
    }

    
    protected void setOverallGain(float overallGain) {
        this.overallGain = overallGain;
    }

    
    protected void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    
    protected void setInputUnits(String inputUnits) {
        this.inputUnits = inputUnits;
    }

    
    public int getDbid() {
        return dbid;
    }

    
    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    ChannelImpl channel;
    float overallGain, frequency;
    String inputUnits;
    int dbid;
}
