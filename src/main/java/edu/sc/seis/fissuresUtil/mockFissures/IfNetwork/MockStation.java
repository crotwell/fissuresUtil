package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class MockStation {

    public static StationImpl createStation() {
        return new StationImpl(MockStationId.createStationId(),
                               "Test Station",
                               MockLocation.create(),
                               "Joe",
                               "this is a test",
                               "still, a test",
                               MockNetworkAttr.createNetworkAttr());
    }

    public static StationImpl createRestartedStation() {
        return new StationImpl(MockStationId.createRestartedStationId(),
                               "Test Station",
                               MockLocation.create(),
                               "Joe",
                               "this is a test",
                               "still, a test",
                               MockNetworkAttr.createNetworkAttr());
    }

    public static StationImpl createOtherStation() {
        return new StationImpl(MockStationId.createOtherStationId(),
                               "Noitats tset",
                               MockLocation.createBerlin(),
                               "Frank",
                               "tset a si siht",
                               "tset a ,llits",
                               MockNetworkAttr.createOtherNetworkAttr());
    }

    public static StationImpl[] createMultiSplendoredStations() {
        return createMultiSplendoredStations(4, 5);
    }
    
    public static StationImpl createCloseStation(Station in) {
        Location loc = new Location(in.getLocation().latitude+.01f, in.getLocation().longitude+.01f, in.getLocation().elevation, in.getLocation().depth, in.getLocation().type);
        return new StationImpl(MockStationId.createCloseStationId(),
                               "Close Station",
                               loc,
                               "Close to you",
                               "is this too close",
                               "still, just a close a test",
                               MockNetworkAttr.createNetworkAttr());
    }

    public static StationImpl[] createMultiSplendoredStations(int rows, int columns) {
        StationImpl[] stations = new StationImpl[rows * columns];
        Location[] locations = MockLocation.create(rows, columns);
        for(int i = 0; i < stations.length; i++) {
            stations[i] = new StationImpl(MockStationId.createMultiSplendoredId("MS"
                                                  + i),
                                          "Multi" + i,
                                          locations[i],
                                          "Charlie",
                                          "Grid of Stations",
                                          "Many Station Group",
                                          MockNetworkAttr.createMultiSplendoredAttr());
        }
        return stations;
    }

    public static StationImpl createStation(Location location) {
        return new StationImpl(MockStationId.createMultiSplendoredId(location.latitude
                                       + "" + location.longitude),
                               "Test Station",
                               location,
                               "Joe",
                               "this is a test",
                               "still, a test",
                               MockNetworkAttr.createMultiSplendoredAttr());
    }
}