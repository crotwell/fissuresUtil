package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.network.SiteImpl;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class MockSite{
    public static Site createSite(){
        return new SiteImpl(MockSiteId.createSiteId(), MockLocation.SIMPLE,
                            MockStation.createStation(),
                            "this is a site.  Isn't it grand?");
    }

    public static Site createOtherSite(){
        return new SiteImpl(MockSiteId.createOtherSiteId(), MockLocation.BERLIN,
                            MockStation.createOtherStation(),
                            "?dnarg ti t'nsI  .etis a is siht");
    }
}
