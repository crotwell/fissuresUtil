/**
 * JDBCChannelTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStationId;

public class JDBCChannelTest extends TestCase{
    

    public void testPutGet() throws SQLException, NotFound{
        JDBCChannel chanTable = new JDBCChannel();
        Channel chan = MockChannel.createChannel();
        int dbid = chanTable.put(chan);
        chanTable.getSiteTable().getStationTable().emptyCache();
        Channel out = chanTable.get(dbid);
        assertEquals(chan.sampling_info, out.sampling_info);
        assertTrue(ChannelIdUtil.areEqual(chan.get_id(),
                                          out.get_id()));
        assertEquals("site lat", chan.my_site.my_location.latitude,
                     out.my_site.my_location.latitude, 0.0001f);
        assertEquals("site lon", chan.my_site.my_location.longitude,
                     out.my_site.my_location.longitude, 0.0001f);
        assertEquals("station lat", chan.my_site.my_station.my_location.latitude,
                     out.my_site.my_station.my_location.latitude, 0.0001f);
        assertEquals("station lon", chan.my_site.my_station.my_location.longitude,
                     out.my_site.my_station.my_location.longitude, 0.0001f);
        assertEquals("station Name", chan.my_site.my_station.name,
                     out.my_site.my_station.name);
        assertEquals("network name", chan.my_site.my_station.my_network.name,
                     out.my_site.my_station.my_network.name);
    }
    
    public void testDoublePut() throws SQLException, NotFound{
        JDBCChannel chanTable = new JDBCChannel();
        Channel chan = MockChannel.createChannel();
        int dbidA = chanTable.put(chan);
        int dbidB = chanTable.put(chan);
        int gottenId = chanTable.getDBId(chan.get_id(), chan.my_site);
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
        assertEquals(chan.sampling_info, chanTable.get(dbidA).sampling_info);
        assertTrue(ChannelIdUtil.areEqual(chan.get_id(),
                                          chanTable.get(dbidA).get_id()));
    }


    public void testGetByChannelId() throws SQLException, NotFound{
        JDBCChannel chanTable = new JDBCChannel();
        Channel chan = MockChannel.createChannel();
        int dbidA = chanTable.put(chan);
        int dbidB = chanTable.getDBId(chan.get_id());
        int gottenId = chanTable.getDBId(chan.get_id(), chan.my_site);
        assertEquals(dbidA, dbidB);
        assertEquals(dbidB, gottenId);
    }

    
    public void testGetAll() throws SQLException, NotFound{
        JDBCChannel chanTable = new JDBCChannel();
        Channel chan = MockChannel.createChannel();
        Channel other = MockChannel.createNorthChannel();
        Channel otherNet = MockChannel.createOtherNetChan();
        chanTable.put(chan);
        chanTable.put(other);
        chanTable.put(otherNet);
        Channel[] chans = chanTable.getAllChannels(MockStationId.createStationId());
        assertEquals(2, chans.length);
        Channel[] otherNetChans = chanTable.getAllChannels(MockStationId.createOtherStationId());
        assertEquals(1, otherNetChans.length);
        assertTrue(ChannelIdUtil.areEqual(otherNet.get_id(), otherNetChans[0].get_id()));
    }
}

