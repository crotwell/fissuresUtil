package edu.sc.seis.fissuresUtil.chooser;

import java.util.EventListener;

/**
 * Allows interested parties to know that the list of stations in the
 * ChannelChooser has changed.
 *
 * @author Philip Oliver-Paull
 **/
public interface StationDataListener extends EventListener{

    public void stationDataChanged(StationDataEvent s);

    public void stationDataCleared();

}
