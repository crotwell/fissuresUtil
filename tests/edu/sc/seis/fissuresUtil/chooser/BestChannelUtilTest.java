/*
 * Created on Jul 20, 2004
 */
package edu.sc.seis.fissuresUtil.chooser;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAccess;

/**
 * @author Charlie Groves
 */
public class BestChannelUtilTest extends TestCase {
    public void testGetHorizontalChannels() {
        NetworkAccess na = MockNetworkAccess.createNetworkAccess();
        Station[] stations = na.retrieve_stations();
        for (int i = 0; i < stations.length; i++) {
            Station station = stations[i];
            Channel[] channels = na.retrieve_for_station(station.get_id());
            assertEquals(2,
                    BestChannelUtil.getHorizontalChannels(channels, "B").length);
        }
    }
    
    static{
        BasicConfigurator.configure();
    }
}
