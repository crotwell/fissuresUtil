/**
 * JDBCStationTEst.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class JDBCStationTest extends JDBCTearDown {

    public void setUp() throws SQLException {
        stationTable = new JDBCStation();
    }
    
    public void tearDown() throws SQLException{
        super.tearDown();
        JDBCStation.emptyCache();
    }

    public void testDoublePut() throws SQLException, NotFound {
        Station sta = MockStation.createStation();
        int dbidA = stationTable.put(sta.get_id());
        int dbidB = stationTable.put(sta);
        int gottenId = stationTable.getDBId(sta.get_id());
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    public void testDoublePutNoCache() throws SQLException, NotFound {
        Station sta = MockStation.createStation();
        int dbidA = stationTable.put(sta.get_id());
        stationTable.emptyCache();
        int dbidB = stationTable.put(sta);
        stationTable.emptyCache();
        int gottenId = stationTable.getDBId(sta.get_id());
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    public void testGetAll() throws SQLException {
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

    public void testGetPut() throws SQLException, NotFound {
        Station sta = MockStation.createStation();
        int dbid = stationTable.put(sta);
        stationTable.emptyCache();
        Station outSta = stationTable.get(dbid);
        assertEquals("name", sta.name, outSta.name);
        assertEquals("comment", sta.comment, outSta.comment);
        assertEquals("net name", sta.my_network.name, outSta.my_network.name);
    }

    public void testRestartedStation() throws SQLException, NotFound {
        Station sta = MockStation.createStation();
        Station resta = MockStation.createRestartedStation();
        int dbid = stationTable.put(sta);
        int restaDbid = stationTable.put(resta);
        assertTrue(dbid != restaDbid);
        assertEquals(dbid, stationTable.getDBId(sta.get_id()));
        assertEquals(restaDbid, stationTable.getDBId(resta.get_id()));
    }

    public void testGetAllOfCodeOfNet() throws SQLException, NotFound {
        Station sta = MockStation.createStation();
        Station resta = MockStation.createRestartedStation();
        int dbid = stationTable.put(sta);
        int restartDbid = stationTable.put(resta);
        int[] dbids = stationTable.getDBIds(sta.get_id().network_id,
                                            sta.get_code());
        assertEquals(2, dbids.length);
        boolean foundDbid = false;
        boolean foundRestartDbid = false;
        for(int i = 0; i < dbids.length; i++) {
            if(dbids[i] == dbid) {
                foundDbid = true;
            }
            if(dbids[i] == restartDbid) {
                foundRestartDbid = true;
            }
        }
        assertTrue(foundDbid);
        assertTrue(foundRestartDbid);
    }

    private JDBCStation stationTable;
}