package edu.sc.seis.fissuresUtil.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.StationIdUtil;

/**
 * @author groves Created on Dec 1, 2004
 */
public class FilterNetworkAccess extends CacheNetworkAccess {

    private Pattern[] patterns;

    public FilterNetworkAccess(NetworkAccess na, Pattern[] patterns) {
        super(na);
        this.patterns = patterns;
    }

    public static String getStationString(StationId s) {
        return StationIdUtil.toStringNoDates(s);
    }

    public Station[] retrieve_stations() {
        Station[] stations = super.retrieve_stations();
        List<Station> acceptableStations = new ArrayList<Station>();
        for(int i = 0; i < stations.length; i++) {
            String netAndStaCode = getStationString(stations[i].get_id());
            for(int j = 0; j < patterns.length; j++) {
                if(patterns[j].matcher(netAndStaCode).matches()) {
                    acceptableStations.add(stations[i]);
                    break;
                }
            }
        }
        return (Station[])acceptableStations.toArray(new Station[0]);
    }
}