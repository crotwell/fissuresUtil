package edu.sc.seis.fissuresUtil.mockFissures.IfNetwork;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;

public class MockChannel {

    public static Channel createChannel() {
        return createChannel(MockChannelId.createVerticalChanId(),
                             "Vertical Channel",
                             MockSite.createSite());
    }

    public static Channel createNorthChannel() {
        return createChannel(MockChannelId.createNorthChanId(),
                             "North Channel",
                             MockSite.createSite());
    }

    public static Channel createEastChannel() {
        return createChannel(MockChannelId.createEastChanId(),
                             "East Channel",
                             MockSite.createSite());
    }

    public static Channel createOtherNetChan() {
        return createChannel(MockChannelId.createOtherNetChanId(),
                             "Other Net Vertical Channel",
                             MockSite.createOtherSite());
    }

    public static Channel[] createChannelsAtLocs(Location[] locs) {
        Channel[] chans = new Channel[locs.length];
        for(int i = 0; i < chans.length; i++) {
            chans[i] = createChannel(locs[i]);
        }
        return chans;
    }

    private static Channel createChannel(Location location) {
        Site s = MockSite.createSite(location);
        return createChannel(MockChannelId.createChanId("BHZ", s),
                             location.toString(),
                             s);
    }

    private static Channel createChannel(ChannelId id, String info, Site s) {
        return new ChannelImpl(id,
                               info,
                               new Orientation(0, 0),
                               new SamplingImpl(0,
                                                new TimeInterval(20.0,
                                                                 UnitImpl.SECOND)),
                               s.effective_time,
                               s);
    }

    public static Channel[] createMotionVector(Station station) {
        Channel[] channels = new Channel[3];
        Site s = MockSite.createSite(station);
        String[] codes = {"BHZ", "BHN", "BHE"};
        for(int i = 0; i < codes.length; i++) {
            channels[i] = createChannel(MockChannelId.createChanId(codes[i], s),
                                        "Motion Vector Channel " + codes[i],
                                        s);
        }
        return channels;
    }
}
