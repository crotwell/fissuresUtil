package edu.sc.seis.fissuresUtil.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public class CacheNetworkAccess extends ProxyNetworkAccess {

    public CacheNetworkAccess(NetworkAccess net) {
        super(net);
    }

    /**
     * Resetting a CacheNetworkAccess clears the cache and calls reset on the
     * network access it's holding if it is also a ProxyNetworkAccess
     */
    public void reset() {
        attr = null;
        stations = null;
        channelMap.clear();
        knownStations.clear();
        knownTimes.clear();
        knownSites.clear();
        instrumentationMap.clear();
        super.reset();
    }

    public NetworkAttr get_attributes() {
        synchronized(this) {
            if(attr == null) {
                attr = net.get_attributes();
                attr.description = attr.description.intern();
                attr.name = attr.name.intern();
                attr.owner = attr.owner.intern();
                intern(attr.get_id());
            }
        }
        return attr;
    }

    private void intern(NetworkId id) {
        id.begin_time = intern(id.begin_time);
        id.network_code = id.network_code.intern();
    }

    /**
     * retreives the stations for the network, but uses the cached copy if it
     * has been previously retrieved. The stations are also cleaned of duplicate
     * networkAttr objects to free memory.
     */
    public Station[] retrieve_stations() {
        synchronized(knownStations) {
            if(stations == null) {
                stations = net.retrieve_stations();
                for(int i = 0; i < stations.length; i++) {
                    stations[i] = intern(stations[i]);
                }
            }
        }
        return stations;
    }

    /**
     * retreives the channels for the stations, but uses the cached copy if it
     * has been previously retrieved. The channels are also cleaned of duplicate
     * site objects to free memory.
     */
    public Channel[] retrieve_for_station(StationId id) {
        String idStr = StationIdUtil.toString(id);
        synchronized(channelMap) {
            if(!channelMap.containsKey(idStr)) {
                Channel[] chans = net.retrieve_for_station(id);
                for(int i = 0; i < chans.length; i++) {
                    intern(chans[i]);
                }
                if(chans.length == 0) {
                    logger.debug("Got 0 channels for station "
                            + StationIdUtil.toString(id) + " in network "
                            + NetworkIdUtil.toString(get_attributes().get_id()));
                }
                channelMap.put(idStr, chans);
                return chans;
            }
            return (Channel[])channelMap.get(idStr);
        }
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time)
            throws ChannelNotFound {
        MicroSecondDate date = new MicroSecondDate(the_time);
        Instrumentation inst = null;
        String idString = ChannelIdUtil.toString(id);
        List instForChannel = (List)instrumentationMap.get(idString);
        if(instForChannel == null) {
            instForChannel = new ArrayList();
            instrumentationMap.put(idString, instForChannel);
        }
        for(Iterator iter = instForChannel.iterator(); iter.hasNext();) {
            InstHolder instHold = (InstHolder)iter.next();
            if(instHold.range.intersects(date)) {
                inst = instHold.inst;
            }
        }
        if(inst == null) {
            inst = net.retrieve_instrumentation(id, the_time);
            InstHolder instHold = new InstHolder(inst);
            instForChannel.add(instHold);
        }
        return inst;
    }

    class InstHolder {

        InstHolder(Instrumentation inst) {
            this.inst = inst;
            this.range = new MicroSecondTimeRange(inst.effective_time);
        }

        Instrumentation inst;

        MicroSecondTimeRange range;
    }

    private void intern(Channel channel) {
        intern(channel.get_id());
        channel.my_site = intern(channel.my_site);
        intern(channel.effective_time);
        channel.name = channel.name.intern();
    }

    private void intern(ChannelId id) {
        id.channel_code = id.channel_code.intern();
        id.network_id = get_attributes().get_id();
        id.station_code = id.station_code.intern();
        id.site_code = id.site_code.intern();
        id.begin_time = intern(id.begin_time);
    }

    private void intern(SiteId id) {
        id.network_id = get_attributes().get_id();
        id.station_code = id.station_code.intern();
        id.site_code = id.site_code.intern();
        id.begin_time = intern(id.begin_time);
    }

    private void intern(StationId id) {
        id.network_id = get_attributes().get_id();
        id.station_code = id.station_code.intern();
        id.begin_time = intern(id.begin_time);
    }

    private Site intern(Site site) {
        synchronized(knownSites) {
            String id = SiteIdUtil.toString(site.get_id());
            if(!knownSites.containsKey(id)) {
                intern(site.get_id());
                site.comment = site.comment.intern();
                site.my_station = intern(site.my_station);
                site.effective_time = intern(site.effective_time);
                knownSites.put(id, site);
                return site;
            }
            return (Site)knownSites.get(id);
        }
    }

    private TimeRange intern(TimeRange effective_time) {
        effective_time.end_time = intern(effective_time.end_time);
        effective_time.start_time = intern(effective_time.start_time);
        return effective_time;
    }

    public Station intern(Station station) {
        synchronized(knownStations) {
            String id = StationIdUtil.toString(station.get_id());
            if(!knownStations.containsKey(id)) {
                intern(station.get_id());
                station.my_network = get_attributes();
                knownStations.put(id, station);
                return station;
            }
            return (Station)knownStations.get(id);
        }
    }

    private Time intern(Time unknownTime) {
        synchronized(knownTimes) {
            if(!knownTimes.containsKey(unknownTime.date_time)) {
                knownTimes.put(unknownTime.date_time, unknownTime);
                return unknownTime;
            }
            return (Time)knownTimes.get(unknownTime.date_time);
        }
    }

    private Map knownTimes = Collections.synchronizedMap(new HashMap());

    private Map knownStations = Collections.synchronizedMap(new HashMap());

    private Map knownSites = Collections.synchronizedMap(new HashMap());

    private NetworkAttr attr;

    private Station[] stations;

    private HashMap channelMap = new HashMap();

    private HashMap instrumentationMap = new HashMap();

    private static Logger logger = Logger.getLogger(CacheNetworkAccess.class);
}