package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;

public class MockChannel {
    
    public static ChannelImpl createChannel() {
        return createChannel(MockChannelId.createVerticalChanId(),
                             "Vertical Channel",
                             MockSite.createSite(),
                             VERTICAL);
    }

    public static ChannelImpl createNorthChannel() {
        return createChannel(MockChannelId.createNorthChanId(),
                             "North Channel",
                             MockSite.createSite(),
                             NORTH);
    }

    public static ChannelImpl createEastChannel() {
        return createChannel(MockChannelId.createEastChanId(),
                             "East Channel",
                             MockSite.createSite(),
                             EAST);
    }

    public static ChannelImpl createOtherSiteSameStationChan() {
        return createChannel(MockChannelId.createOtherSiteSameStationChanId(),
                             "Other Site Same Station Vertical Channel",
                             MockSite.createOtherSiteSameStation(),
                             VERTICAL);
    }

    public static ChannelImpl createOtherNetChan() {
        return createChannel(MockChannelId.createOtherNetChanId(),
                             "Other Net Vertical Channel",
                             MockSite.createOtherSite(),
                             VERTICAL);
    }

    public static ChannelImpl[] createChannelsAtLocs(Location[] locs) {
        ChannelImpl[] chans = new ChannelImpl[locs.length];
        for(int i = 0; i < chans.length; i++) {
            chans[i] = createChannel(locs[i]);
        }
        return chans;
    }

    public static ChannelImpl createChannel(Location location) {
        return createChannel( MockSite.createSite(location));
    }

    public static ChannelImpl createChannel(Site site) {
        return createChannel(MockChannelId.createChanId("BHZ", site),
                             "fake chan",
                             site,
                             VERTICAL);
    }
    
    public static ChannelImpl createChannel(Station station) {
        Site s = MockSite.createSite(station);
        return createChannel(MockChannelId.createChanId("BHZ", s),
                             "fake chan",
                             s,
                             VERTICAL);
    }

    private static ChannelImpl createChannel(ChannelId id,
                                         String info,
                                         Site s,
                                         Orientation o) {
        return new ChannelImpl(id,
                               info,
                               o,
                               new SamplingImpl(20,
                                                new TimeInterval(1.0,
                                                                 UnitImpl.SECOND)),
                               s.getEffectiveTime(),
                               s);
    }

    public static ChannelImpl[] createMotionVector() {
        return createMotionVector(MockStation.createStation());
    }

    public static ChannelImpl[] createMotionVector(Station station) {
        ChannelImpl[] channels = new ChannelImpl[3];
        Site s = MockSite.createSite(station);
        String[] codes = {"BHZ", "BHN", "BHE"};
        for(int i = 0; i < codes.length; i++) {
            channels[i] = createChannel(MockChannelId.createChanId(codes[i], s),
                                        "Motion Vector Channel " + codes[i],
                                        s,
                                        ORIENTATIONS[i]);
        }
        return channels;
    }

    public static ChannelGroup createGroup() {
        return new ChannelGroup( createMotionVector());
    }
    
    private static final Orientation VERTICAL = new Orientation(0, 90);

    private static final Orientation EAST = new Orientation(90, 0);

    private static final Orientation NORTH = new Orientation(0, 0);

    private static final Orientation[] ORIENTATIONS = {VERTICAL, NORTH, EAST};

}
