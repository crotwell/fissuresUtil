/**
 * JDBCStationTEst.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class JDBCStationTest extends TestCase{
    public JDBCStationTest() throws SQLException{ stationTable = new JDBCStation(); }

    public void testDoublePut() throws SQLException, NotFound{
        Station sta = MockStation.createStation();
        int dbidA = stationTable.put(sta.get_id());
        int dbidB = stationTable.put(sta);
        int gottenId = stationTable.getDBId(sta.get_id());
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    public void testGetAll() throws SQLException{
        Station sta = MockStation.createStation();
        Station sta2 = MockStation.createOtherStation();
        stationTable.put(sta);
        stationTable.put(sta2);
        StationId[] stas = stationTable.getAllStationIds(sta.get_id().network_id);
        assertEquals(1, stas.length);
        assertTrue(StationIdUtil.areEqual(stas[0], sta.get_id()));
        StationId[] sta2s = stationTable.getAllStationIds(sta2.get_id().network_id);
        assertEquals(1, sta2s.length);
        assertTrue(StationIdUtil.areEqual(sta2s[0], sta2.get_id()));
    }

    private JDBCStation stationTable;
}

