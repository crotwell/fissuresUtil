/**
 * JDBCNetworkTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAttr;

public class JDBCNetworkTest extends TestCase{

    public void testDoublePut() throws SQLException, NotFound{
        JDBCNetwork net = new JDBCNetwork();
        NetworkAttr attr = MockNetworkAttr.createNetworkAttr();
        int dbidA = net.put(attr.get_id());
        int dbidB = net.put(attr);
        int gottenId = net.getDBId(attr.get_id());
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }
    
    public void testGetNetworkId() throws SQLException, NotFound{
        JDBCNetwork net = new JDBCNetwork();
        NetworkAttr attr = MockNetworkAttr.createNetworkAttr();
        int dbid = net.put(attr.get_id());
        NetworkId netId = net.getNetworkId(dbid);
        assertEquals(attr.get_id().network_code, netId.network_code);
        assertEquals(attr.get_id().begin_time.date_time, netId.begin_time.date_time);
    }
}

