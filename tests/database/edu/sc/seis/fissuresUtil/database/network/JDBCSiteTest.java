/**
 * JDBCSiteTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfNetwork.MockSite;

public class JDBCSiteTest extends TestCase{
    public void testDoublePut() throws SQLException, NotFound{
        JDBCSite siteTable = new JDBCSite();
        Site site = MockSite.createSite();
        int dbidA = siteTable.put(site);
        int dbidB = siteTable.put(site);
        int gottenId = siteTable.getDBId(site.get_id(), site.my_station);
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    public void testGetAll() throws SQLException{
        JDBCSite siteTable = new JDBCSite();
        Site site = MockSite.createSite();
        Site site2 = MockSite.createOtherSite();
        siteTable.put(site);
        siteTable.put(site2);
        SiteId[] sites = siteTable.getAllSiteIds(site.my_station.get_id());
        assertEquals(1, sites.length);
        assertTrue(SiteIdUtil.areEqual(sites[0], site.get_id()));
        SiteId[] site2s = siteTable.getAllSiteIds(site2.my_station.get_id());
        assertEquals(1, site2s.length);
        assertTrue(SiteIdUtil.areEqual(site2s[0], site2.get_id()));
        assertEquals(1, siteTable.getAllSites(site.get_id().network_id).length);
    }
}

