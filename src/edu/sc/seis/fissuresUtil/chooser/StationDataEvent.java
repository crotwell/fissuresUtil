package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Station;

/**
 * Represents the stations in the ChannelChooser.  This is
 * to be fired whenever stations are added or removed.
 *
 * @author Philip Oliver-Paull
 **/
public class StationDataEvent{
    Station[] stations;

    public StationDataEvent(Station[] stations){
        this.stations = stations;
    }

    public Station[] getStations(){
        return stations;
    }
}
