package edu.sc.seis.fissuresUtil.chooser;

import java.util.EventListener;

/**
 * Allows interested parties to know that the selection in the list of stations
 * in the ChannelChooser has changed.
 *
 * @author Philip Oliver-Paull
 **/
public interface StationSelectionListener extends EventListener{

    public void stationSelectionChanged(StationSelectionEvent s);

}
