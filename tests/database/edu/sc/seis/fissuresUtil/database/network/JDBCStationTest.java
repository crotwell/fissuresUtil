/**
 * JDBCStationTEst.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfNetwork.MockStation;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCStationTest extends TestCase{

    public void testDoublePut() throws SQLException, NotFound{
        JDBCStation stationTable = new JDBCStation();
        Station sta = MockStation.createStation();
        int dbidA = stationTable.put(sta.get_id());
        int dbidB = stationTable.put(sta);
        int gottenId = stationTable.getDBId(sta.get_id());
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }
}

