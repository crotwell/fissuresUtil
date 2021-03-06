package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class MockSite {

    public static Site createSite() {
        return new SiteImpl(MockSiteId.createSiteId(),
                            MockLocation.create(),
                            MockStation.createStation(),
                            "this is a site.  Isn't it grand?");
    }

    public static Site createOtherSite() {
        return new SiteImpl(MockSiteId.createOtherSiteId(),
                            MockLocation.createBerlin(),
                            MockStation.createOtherStation(),
                            "?dnarg ti t'nsI  .etis a is siht");
    }

    public static Site createOtherSiteSameStation() {
        return new SiteImpl(MockSiteId.createOtherSiteIdSameStation(),
                            MockLocation.create(),
                            MockStation.createStation(),
                            "this is another site.  Isn't it grander?");
    }

    public static Site createSite(Station station) {
        return createSite(station, "00");
    }

    public static Site createSite(Station station, String siteCode) {
        return new SiteImpl(MockSiteId.createSiteId(station, siteCode),
                            station.getLocation(),
                            station,
                            "Mock Site for station "
                                    + StationIdUtil.toStringNoDates(station.get_id()));
    }

    public static Site createSite(Location location) {
        Station station = MockStation.createStation(location);
        return new SiteImpl(MockSiteId.createSiteId(station),
                            station.getLocation(),
                            station,
                            "Mock Site for station "
                                    + StationIdUtil.toStringNoDates(station.get_id()));
    }
}
