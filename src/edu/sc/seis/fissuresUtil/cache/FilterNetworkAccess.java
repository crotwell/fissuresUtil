package edu.sc.seis.fissuresUtil.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;

/**
 * @author groves Created on Dec 1, 2004
 */
public class FilterNetworkAccess extends ProxyNetworkAccess {

    private Pattern[] patterns;

    public FilterNetworkAccess(NetworkAccess na, Pattern[] patterns) {
        super(na);
        this.patterns = patterns;
    }

    public Station[] retrieve_stations() {
        Station[] stations = net.retrieve_stations();
        List acceptableStations = new ArrayList();
        for(int i = 0; i < stations.length; i++) {
            String netAndStaCode = stations[i].my_network.get_code() + "."
                    + stations[i].get_code();
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