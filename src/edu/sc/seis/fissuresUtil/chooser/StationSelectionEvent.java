package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.IfNetwork.Station;



/**
 * Represents a selected station in ChannelChooser.  This is
 * to be fired whenever the selected station changes.
 *
 * @author Philip Oliver-Paull
 **/

public class StationSelectionEvent{

    Object source;
    Station[] selected;

    public StationSelectionEvent(Object source, Station[] selectedStations){
        this.source = source;
        this.selected = selectedStations;
    }

    public Object getSource(){
        return source;
    }

    public Station[] getSelectedStations(){
        return selected;
    }

}
