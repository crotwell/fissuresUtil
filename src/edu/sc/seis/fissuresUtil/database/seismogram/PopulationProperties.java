package edu.sc.seis.fissuresUtil.database.seismogram;

import java.util.HashMap;
import java.util.Properties;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;

public class PopulationProperties {

    public static NetworkAttrImpl getNetworkAttr(String netString, Properties props) {
        if(netString.equals("XX") || netString.trim().length() == 0) {
            // sac network header is unknown, so set to be value from props
            netString = props.getProperty(NETWORK_REMAP);
        }
        // remap if props have a network remap, otherwise use netString as is
        netString=props.getProperty(NETWORK_REMAP+"."+netString, netString);
        NetworkAttrImpl netAttr;
        if(nets.containsKey(netString)) {
            netAttr = (NetworkAttrImpl)nets.get(netString);
        } else {
            TimeRange netEffectiveTime = new TimeRange(new Time(props.getProperty(NET + netString + BEGIN,
                                                                                  TimeUtils.timeUnknown.date_time), -1),
                                                       new Time(props.getProperty(NET + netString + END,
                                                                                  TimeUtils.timeUnknown.date_time), -1));
            NetworkId netid = new NetworkId(netString.substring(0, 2).toUpperCase(), netEffectiveTime.start_time);
            netAttr = new NetworkAttrImpl(netid,
                                          props.getProperty(NET + netid.network_code + NAME, netString),
                                          props.getProperty(NET + netid.network_code + DESCRIPTION, ""),
                                          props.getProperty(NET + netid.network_code + OWNER, ""),
                                          netEffectiveTime);
            nets.put(netString, netAttr);
        }
        return netAttr;
    }

    public static Channel fix(Channel chan, Properties props) {
        String netString = NetworkIdUtil.toStringNoDates(chan.get_id().network_id);
        NetworkAttrImpl netAttr = getNetworkAttr(netString, props);
        // in case of remap of network code
        netString = NetworkIdUtil.toStringNoDates(netAttr.get_id());
        StationImpl station;
        String stationString = netString + "." + chan.my_site.my_station.get_code();
        if(stations.containsKey(stationString)) {
            station = (StationImpl)stations.get(stationString);
        } else {
            String staPrefix = NET + stationString;
            System.out.println("begin prop name: "+staPrefix + BEGIN+"  end: "+staPrefix + END);
            TimeRange staEffectiveTime = new TimeRange(new Time(props.getProperty(staPrefix + BEGIN,
                                                                                  TimeUtils.timeUnknown.date_time),
                                                                -1),
                                                       new Time(props.getProperty(staPrefix + END,
                                                                                  TimeUtils.timeUnknown.date_time),
                                                                -1));
            StationId stationId = new StationId(netAttr.get_id(),
                                                chan.my_site.my_station.get_code(),
                                                staEffectiveTime.start_time);
            station = new StationImpl(stationId,
                                      chan.my_site.my_station.name,
                                      chan.my_site.my_station.my_location,
                                      chan.my_site.my_station.operator,
                                      chan.my_site.my_station.description,
                                      chan.my_site.my_station.comment,
                                      netAttr);
            stations.put(stationString, station);
        }
        Channel out;
        String channelString = stationString + "." + chan.get_id().site_code + "." + chan.get_code();
        if(channels.containsKey(channelString)) {
            return (ChannelImpl)channels.get(channelString);
        } else {
            // sac knows nothing about channel/site start times, so use station
            // start time from props
            SiteId siteId = new SiteId(netAttr.get_id(),
                                       station.get_code(),
                                       chan.my_site.get_code(),
                                       station.effective_time.start_time);
            Site site = new SiteImpl(siteId,
                                     chan.my_site.my_location,
                                     station.effective_time,
                                     station,
                                     chan.my_site.comment);
            ChannelId chanId = new ChannelId(netAttr.get_id(),
                                             station.get_code(),
                                             siteId.site_code,
                                             chan.get_code(),
                                             station.effective_time.start_time);
            out = new ChannelImpl(chanId,
                                  chan.name,
                                  chan.an_orientation,
                                  chan.sampling_info,
                                  station.effective_time,
                                  site);
            channels.put(channelString, out);
            return out;
        }
    }

    static HashMap nets = new HashMap();

    static HashMap stations = new HashMap();

    static HashMap channels = new HashMap();

    public static final String NET = "network.";

    public static final String NETWORK_REMAP = NET + "remap";

    public static final String BEGIN = ".beginTime";

    public static final String END = ".endTime";

    public static final String NAME = ".name";

    public static final String DESCRIPTION = ".description";

    public static final String OWNER = ".owner";
}
