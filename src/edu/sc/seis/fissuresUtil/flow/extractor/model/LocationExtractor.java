package edu.sc.seis.fissuresUtil.flow.extractor.model;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

public class LocationExtractor {

    public Location extract(Object o) {
        if(o instanceof Location) {
            return (Location)o;
        } else if(o instanceof EventAccessOperations) {
            return EventUtil.extractOrigin((EventAccessOperations)o).my_location;
        } else if(o instanceof Station) {
            return ((Station)o).getLocation();
        } else if(o instanceof Channel) {
            return ((Channel)o).getSite().getLocation();
        }
        return null;
    }
}
