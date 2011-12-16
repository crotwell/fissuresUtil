package edu.sc.seis.fissuresUtil.chooser;

import java.util.EventListener;

/**
 * AvailableStationDataListener.java
 *
 * @author Created by Omnicore CodeGuide
 */
public interface AvailableStationDataListener extends EventListener{
	public void stationAvailabiltyChanged(AvailableStationDataEvent e);
}

