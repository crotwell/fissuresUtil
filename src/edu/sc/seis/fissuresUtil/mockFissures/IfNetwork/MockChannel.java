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
                             MockSite.createSite(),
                             VERTICAL);
    }

    public static Channel createNorthChannel() {
        return createChannel(MockChannelId.createNorthChanId(),
                             "North Channel",
                             MockSite.createSite(),
                             NORTH);
    }

    public static Channel createEastChannel() {
        return createChannel(MockChannelId.createEastChanId(),
                             "East Channel",
                             MockSite.createSite(),
                             EAST);
    }

    public static Channel createOtherNetChan() {
        return createChannel(MockChannelId.createOtherNetChanId(),
                             "Other Net Vertical Channel",
                             MockSite.createOtherSite(),
                             VERTICAL);
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
                             s,
                             VERTICAL);
    }

    private static Channel createChannel(ChannelId id,
                                         String info,
                                         Site s,
                                         Orientation o) {
        return new ChannelImpl(id,
                               info,
                               o,
                               new SamplingImpl(20,
                                                new TimeInterval(1.0,
                                                                 UnitImpl.SECOND)),
                               s.effective_time,
                               s);
    }

    public static Channel[] createMotionVector() {
        return createMotionVector(MockStation.createStation());
    }

    public static Channel[] createMotionVector(Station station) {
        Channel[] channels = new Channel[3];
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

    private static final Orientation VERTICAL = new Orientation(0, 90);

    private static final Orientation EAST = new Orientation(90, 0);

    private static final Orientation NORTH = new Orientation(0, 0);

    private static final Orientation[] ORIENTATIONS = {VERTICAL, NORTH, EAST};
}
