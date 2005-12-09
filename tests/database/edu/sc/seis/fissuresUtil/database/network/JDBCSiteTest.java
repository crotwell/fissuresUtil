/**
 * JDBCSiteTest.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockSite;

public class JDBCSiteTest extends JDBCTearDown {

    public void setUp() throws SQLException {
        siteTable = new JDBCSite();
        site = MockSite.createSite();
    }

    public void testDoublePut() throws SQLException, NotFound {
        int dbidA = siteTable.put(site);
        int dbidB = siteTable.put(site);
        int gottenId = siteTable.getDBId(site.get_id(), site.my_station);
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    public void testGetByCode() throws SQLException, NotFound {
        int dbidA = siteTable.put(site);
        int[] possibleStations = siteTable.getStationTable()
                .getDBIds(site.get_id().network_id, site.my_station.get_code());
        int[] dbids = siteTable.getDBIds(possibleStations, site.get_code());
        assertEquals(1, dbids.length);
        assertEquals(dbids[0], dbidA);
    }

    private JDBCSite siteTable;

    private Site site;
}