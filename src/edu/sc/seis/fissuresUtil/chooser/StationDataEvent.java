package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Station;

/**
 * Represents the stations in the ChannelChooser.  This is
 * to be fired whenever stations are added or removed.
 *
 * @author Philip Oliver-Paull
 **/
public class StationDataEvent{
	Object source;
	Station[] stations;
	
	public StationDataEvent(Object source, Station[] stations){
		this.source = source;
		this.stations = stations;
	}
	
	public Object getSource(){
		return source;
	}
	
	public Station[] getStations(){
		return stations;
	}

}
