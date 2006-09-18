package edu.sc.seis.fissuresUtil.rt130;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.bag.OrientationUtil;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class DASChannelCreator {

    public DASChannelCreator(Properties props) throws IOException {
        this(PopulationProperties.getNetworkAttr(props),
             new RT130SamplingFinder(new RT130FileReader()),
             new NCReader(props).getSites());
    }

    public DASChannelCreator(NetworkAttr net, SamplingFinder sf) {
        this(net, sf, new ArrayList(0));
    }

    public DASChannelCreator(NetworkAttr net, SamplingFinder sf, List sites) {
        this.net = net;
        this.sf = sf;
        Iterator it = sites.iterator();
        while(it.hasNext()) {
            add((Site)it.next());
        }
    }

    private void add(Site s) {
        String unitId = getUnitId(s);
        if(!unitIdToSites.containsKey(unitId)) {
            unitIdToSites.put(unitId, new ArrayList());
        }
        List sites = (List)unitIdToSites.get(unitId);
        sites.add(s);
        siteToChannels.put(s, new HashMap());
        allSites.add(s);
    }

    private Site find(String unitIdNumber, MicroSecondDate beginTime) {
        Object siteOrList = unitIdToSites.get(unitIdNumber);
        if(siteOrList instanceof Site) {// Only a site in the map. That means
            // it's a dummy we created
            return (Site)siteOrList;
        }
        List sites = (List)siteOrList;
        Iterator it = sites.iterator();
        while(it.hasNext()) {
            Site s = (Site)it.next();
            if(new MicroSecondTimeRange(s.effective_time).contains(beginTime)) {
                return s;
            }
        }
        throw new RT130FormatError(unitIdNumber
                + " had defined sites for it and data outside of the effective time of those sites");
    }

    public static String getUnitId(Site s) {
        return s.comment.substring(0, s.comment.indexOf('/'));
    }

    public Channel[] create(String unitIdNumber,
                            String fileLoc,
                            MicroSecondTimeRange fileTimeWindow) {
        int sampleRate;
        try {
            sampleRate = sf.find(fileLoc, fileTimeWindow);
        } catch(RT130FormatException e) {
            throw new RT130FormatError(e);
        } catch(IOException e) {
            throw new RT130FormatError(e);
        }
        return create(unitIdNumber,
                      fileTimeWindow.getBeginTime(),
                      new File(fileLoc).getParentFile().getName(),
                      sampleRate);
    }

    public Channel[] create(String unitIdNumber,
                            MicroSecondDate beginTime,
                            String datastream,
                            int sampleRate) {
        // Get site for unitIdNumber and time - create and cache if no real
        Site s;
        if(unitIdToSites.containsKey(unitIdNumber)) {
            s = find(unitIdNumber, beginTime);
        } else {
            s = createSite(unitIdNumber, beginTime);
            unitIdToSites.put(unitIdNumber, s);
            siteToChannels.put(s, new HashMap());
            allSites.add(s);
        }
        // Get channels for site for datastream - create and cache if necessary
        Map dataStreamsToChannels = (Map)siteToChannels.get(s);
        if(dataStreamsToChannels.containsKey(datastream)) {
            return (Channel[])dataStreamsToChannels.get(datastream);
        }
        Channel[] chans = createChannels(s, sampleRate);
        dataStreamsToChannels.put(datastream, chans);
        return chans;
    }

    private Site createSite(String unitIdNumber, MicroSecondDate begin) {
        Time beginTime = begin.getFissuresTime();
        TimeRange effTime = new TimeRange(beginTime, TimeUtils.timeUnknown);
        SiteId siteId = new SiteId(net.get_id(), unitIdNumber, "00", beginTime);
        StationId staId = new StationId(net.get_id(), unitIdNumber, beginTime);
        Location loc = new Location(0,
                                    0,
                                    new QuantityImpl(0, UnitImpl.METER),
                                    new QuantityImpl(0, UnitImpl.METER),
                                    LocationType.GEOGRAPHIC);
        Station station = new StationImpl(staId,
                                          "",
                                          loc,
                                          effTime,
                                          "",
                                          "",
                                          TAG,
                                          net);
        return new SiteImpl(siteId, loc, effTime, station, unitIdNumber
                + "/123 " + TAG + " default");
    }

    private Channel[] createChannels(Site s, int sampleRate) {
        String band = "B";
        if(sampleRate < 10) {
            band = "L";
        }
        Matcher m = instrumentationParser.matcher(s.comment);
        if(!m.matches()) {
            throw new RT130FormatError(SiteIdUtil.toString(s.get_id())
                    + " has a malformed instrumentation specification '"
                    + s.comment + "'");
        }
        Orientation[] orientations = parseOrientations(m.group(2));
        if(orientations.length < 3) {
            System.out.println("GOT " + orientations.length
                    + " orientations from " + m.group(2) + " from " + s.comment);
        }
        int curUnmatched = 1;
        SamplingImpl sampling = new SamplingImpl(sampleRate,
                                                 new TimeInterval(1,
                                                                  UnitImpl.SECOND));
        Channel[] newChannel = new ChannelImpl[orientations.length];
        for(int i = 0; i < orientations.length; i++) {
            String orientationCode;
            if(OrientationUtil.areEqual(UP, orientations[i])) {
                orientationCode = "Z";
            } else if(OrientationUtil.areEqual(EAST, orientations[i])) {
                orientationCode = "E";
            } else if(OrientationUtil.areEqual(NORTH, orientations[i])) {
                orientationCode = "N";
            } else {
                orientationCode = "" + curUnmatched++;
            }
            ChannelId id = new ChannelId(net.get_id(),
                                         s.my_station.get_code(),
                                         s.get_code(),
                                         band + "H" + orientationCode,
                                         s.effective_time.start_time);
            newChannel[i] = new ChannelImpl(id,
                                            "",
                                            orientations[i],
                                            sampling,
                                            s.effective_time,
                                            s);
        }
        return newChannel;
    }

    public static Orientation[] parseOrientations(String orientationString) {
        Matcher m = orientation.matcher(orientationString);
        if(!m.matches()) {
            throw new IllegalArgumentException("The orientation string must be either 'default' or a channel orientation specification");
        }
        if(m.group(1) != null) {
            return new Orientation[] {new Orientation(0, -90),
                                      new Orientation(0, 0),
                                      new Orientation(90, 0)};
        }
        Orientation[] orientations = new Orientation[3];
        for(int i = 0; i < orientations.length; i++) {
            orientations[i] = new Orientation(Integer.parseInt(m.group(3 + i * 2)),
                                              Integer.parseInt(m.group(2 + i * 2)));
        }
        return orientations;
    }

    private static Pattern orientation = Pattern.compile("(default)|\\d/(-?\\d+)/(-?\\d+):\\d/(-?\\d+)/(-?\\d+):\\d/(-?\\d+)/(-?\\d+)");

    private static final Orientation UP = OrientationUtil.getUp(),
            NORTH = OrientationUtil.getNorth(),
            EAST = OrientationUtil.getEast();

    private SamplingFinder sf;

    private Pattern instrumentationParser = Pattern.compile(NCReader.INSTRUMENT_RE);

    private NetworkAttr net;

    private Map unitIdToSites = new HashMap();

    private Map siteToChannels = new HashMap();

    private List allSites = new ArrayList();

    private static final String TAG = "CreatedBy/DASChannelCreator";
}
