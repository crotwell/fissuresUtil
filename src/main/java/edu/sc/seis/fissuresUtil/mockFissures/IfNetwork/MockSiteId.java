package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;

public class MockSiteId {

    public static SiteId createSiteId() {
        Station sta = MockStation.createStation();
        return new SiteId(sta.get_id().network_id,
                          sta.get_code(),
                          "  ",
                          sta.get_id().begin_time);
    }

    public static SiteId createOtherSiteIdSameStation() {
        Station sta = MockStation.createStation();
        return new SiteId(sta.get_id().network_id,
                          sta.get_code(),
                          "11",
                          sta.get_id().begin_time);
    }

    public static SiteId createOtherSiteId() {
        Station sta = MockStation.createOtherStation();
        return new SiteId(sta.get_id().network_id,
                          sta.get_code(),
                          "00",
                          sta.get_id().begin_time);
    }

    public static SiteId createSiteId(Station sta) {
        return createSiteId(sta, "  ");
    }

    public static SiteId createSiteId(Station sta, String siteCode) {
        return new SiteId(sta.get_id().network_id,
                          sta.get_code(),
                          siteCode,
                          sta.get_id().begin_time);
    }
}