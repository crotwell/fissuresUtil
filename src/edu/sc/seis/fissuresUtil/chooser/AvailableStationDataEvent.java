package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Station;

/**
 * AvailableStationDataEvent.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class AvailableStationDataEvent{
	private Object source;
	private Station station;
	private boolean isUp;

	public AvailableStationDataEvent(Object source, Station station, boolean isUp){
		this.source = source;
		this.station = station;
		this.isUp = isUp;
	}

	public Object getSource(){
		return source;
	}

	public Station getStation(){
		return station;
	}

	public boolean stationIsUp(){
		return isUp;
	}
}

