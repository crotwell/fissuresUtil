/**
 * JDBCSiteTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfNetwork.MockSite;
import java.sql.SQLException;
import junit.framework.TestCase;

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
}

