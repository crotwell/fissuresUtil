/**
 * ChannelChooserLayer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.layers;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;

public class ChannelChooserLayer extends StationLayer{

    private ChannelChooser chooser;

    public ChannelChooserLayer(ChannelChooser c){
        super();
        setName("Channel Chooser Layer");
        c.addStationDataListener(this);
        c.addStationSelectionListener(this);
        c.addAvailableStationDataListener(this);
        chooser = c;
    }

    public ChannelChooser getChannelChooser(){
        return chooser;
    }

    public void toggleStationSelection(Station station){
        chooser.toggleStationSelected(station);
    }

}

