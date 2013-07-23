package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.StationImpl;



/**
 * Represents a selected station in ChannelChooser.  This is
 * to be fired whenever the selected station changes.
 *
 * @author Philip Oliver-Paull
 **/

public class StationSelectionEvent{

    Object source;
    StationImpl[] selected;

    public StationSelectionEvent(Object source, StationImpl[] selectedStations){
        this.source = source;
        this.selected = selectedStations;
    }

    public Object getSource(){
        return source;
    }

    public StationImpl[] getSelectedStations(){
        return selected;
    }
    
    public StationImpl[] getSelectedStations(MicroSecondDate date) {
        return ChannelChooser.getStationsThatExistOnDate(date, selected);
    }

}
