/**
 * MyNetworkAccess.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Calibration;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfNetwork.TimeCorrection;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.StationIdUtil;

public class MockNetworkAccess implements NetworkAccess {

    public static NetworkAccess createNetworkAccess() {
        return new MockNetworkAccess();
    }

    public static NetworkAccess createOtherNetworkAccess() {
        return new MockNetworkAccess(MockNetworkAttr.createOtherNetworkAttr(),
                                     MockStation.createOtherStation(),
                                     new Channel[] {MockChannel.createOtherNetChan()});
    }

    public static NetworkAccess createManySplendoredNetworkAccess() {
        Station[] stations = MockStation.createMultiSplendoredStations();
        Channel[][] channels = new Channel[stations.length][];
        for(int i = 0; i < stations.length; i++) {
            channels[i] = MockChannel.createMotionVector(stations[i]);
        }
        return new MockNetworkAccess(MockNetworkAttr.createMultiSplendoredAttr(),
                                     stations,
                                     channels);
    }

    private NetworkAttr attributes;

    public MockNetworkAccess(NetworkAttr attributes,
                             Station station,
                             Channel[] channels) {
        this(attributes, new Station[] {station}, make2DArray(channels));
    }

    private static Channel[][] make2DArray(Channel[] channels) {
        Channel[][] channels2d = new Channel[1][];
        channels2d[0] = channels;
        return channels2d;
    }

    public MockNetworkAccess() {
        this(MockNetworkAttr.createNetworkAttr(),
             MockStation.createStation(),
             new Channel[] {MockChannel.createChannel(),
                            MockChannel.createNorthChannel(),
                            MockChannel.createEastChannel()});
    }

    private MockNetworkAccess(NetworkAttr attributes,
                              Station station[],
                              Channel[][] channels) {
        this.attributes = attributes;
        this.stations = station;
        this.channels = channels;
    }

    public Station[] retrieve_stations() {
        return stations;
    }

    public Instrumentation retrieve_instrumentation(ChannelId p1, Time p2)
            throws ChannelNotFound {
        return null;
    }

    public Channel[] retrieve_channels_by_code(String p1, String p2, String p3)
            throws ChannelNotFound {
        return null;
    }

    public Channel retrieve_channel(ChannelId p1) throws ChannelNotFound {
        for(int j = 0; j < channels.length; j++) {
            for(int i = 0; i < channels[j].length; i++) {
                if(ChannelIdUtil.areEqual(channels[j][i].get_id(), p1)) {
                    return channels[j][i];
                }
            }
        }
        throw new ChannelNotFound();
    }

    public Channel[] retrieve_for_station(StationId p1) {
        for(int i = 0; i < stations.length; i++) {
            if(StationIdUtil.areEqual(p1, stations[i].get_id())) {
                return channels[i];
            }
        }
        return new Channel[] {};
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        return null;
    }

    public Calibration[] retrieve_calibrations(ChannelId p1, TimeRange p2)
            throws ChannelNotFound, NotImplemented {
        return null;
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId p1)
            throws ChannelNotFound, NotImplemented {
        return null;
    }

    public ChannelId[] retrieve_grouping(ChannelId p1) throws ChannelNotFound {
        return null;
    }

    public ChannelId[] retrieve_all_channels(int p1, ChannelIdIterHolder p2) {
        return null;
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId p1, TimeRange p2)
            throws ChannelNotFound, NotImplemented {
        return null;
    }

    public Channel[] locate_channels(Area p1,
                                     SamplingRange p2,
                                     OrientationRange p3) {
        return null;
    }

    public ChannelId[][] retrieve_groupings() {
        return null;
    }

    public NetworkAttr get_attributes() {
        return attributes;
    }

    private Station[] stations;

    private Channel[][] channels;
}