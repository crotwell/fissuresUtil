package edu.sc.seis.fissuresUtil.stationxml;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.network.StationImpl;


public class StationChannelBundle {

    public StationChannelBundle(StationImpl station, List<ChannelSensitivityBundle> chanList) {
        this.station = station;
        this.chanList = chanList;
    }
    
    public StationChannelBundle(StationImpl station) {
        this( station, new ArrayList<ChannelSensitivityBundle>());
    }
    
    public void setChanList(List<ChannelSensitivityBundle> chanList) {
        this.chanList = chanList;
    }


    public StationImpl getStation() {
        return station;
    }
    
    public List<ChannelSensitivityBundle> getChanList() {
        return chanList;
    }

    StationImpl station;
    List<ChannelSensitivityBundle> chanList;
}
