/*
 * Created on Jul 19, 2004
 */
package edu.sc.seis.fissuresUtil.chooser;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.cache.RetryNetworkDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkDC;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkId;

/**
 * @author Charlie Groves
 */
public class ChannelChooserTest extends TestCase {
    public ChannelChooserTest() throws NetworkNotFound {
        BasicConfigurator.configure();
        MockNetworkDC mockDC = new MockNetworkDC();
        ProxyNetworkDC[] dcs = new ProxyNetworkDC[] { new RetryNetworkDC(
                mockDC, 3) };
        cc = new ChannelChooser(dcs);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        NetworkAccess threeCompNet = mockDC.a_finder().retrieve_by_id(MockNetworkId.createNetworkID());
        threeCompStation = threeCompNet.retrieve_stations()[0];
        NetworkAccess vertNet = mockDC.a_finder().retrieve_by_id(MockNetworkId.createOtherNetworkID());
        vertOnlyStation = vertNet.retrieve_stations()[0];
    }

    public void testSingleStationVerticalChannelSelection() {
        cc.orientationList.setSelectedIndex(ChannelChooser.VERTICAL_ONLY);
        cc.select(vertOnlyStation);
        assertEquals(1, cc.getSelectedChannels().length);
    }

    public void testDualStationVerticalChannelSelection() {
        cc.orientationList.setSelectedIndex(ChannelChooser.VERTICAL_ONLY);
        cc.select(threeCompStation);
        cc.select(vertOnlyStation);
        assertEquals(2, cc.getSelectedChannels().length);
    }

    public void testHorizontalChannelSelectionWithData() {
        cc.orientationList.setSelectedIndex(ChannelChooser.HORIZONTAL_ONLY);
        cc.select(threeCompStation);
        assertEquals(2, cc.getSelectedChannels().length);
    }

    public void testHorizontalChannelSelectionWithNoData() {
        cc.orientationList.setSelectedIndex(ChannelChooser.HORIZONTAL_ONLY);
        cc.select(vertOnlyStation);
        assertEquals(0, cc.getSelectedChannels().length);
    }

    public void testThreeComponentSelection() {
        cc.select(threeCompStation);
        cc.orientationList.setSelectedIndex(ChannelChooser.THREE_COMPONENT);
        assertEquals(3, cc.getSelectedChannels().length);
    }

    public void testThreeComponentSelectionWithNoData() {
        cc.select(vertOnlyStation);
        cc.orientationList.setSelectedIndex(ChannelChooser.THREE_COMPONENT);
        assertEquals(0, cc.getSelectedChannels().length);
    }

    public void testBestSelection() {
        cc.select(vertOnlyStation);
        cc.orientationList.setSelectedIndex(ChannelChooser.BEST_CHANNELS);
        assertEquals(1, cc.getSelectedChannels().length);
    }
    
    
    public void tearDown() {
        cc.deselect(threeCompStation);
        cc.deselect(vertOnlyStation);
    }

    private ChannelChooser cc;
    
    private Station threeCompStation;
    private Station vertOnlyStation;

    private static Logger logger = Logger.getLogger(ChannelChooserTest.class);
}