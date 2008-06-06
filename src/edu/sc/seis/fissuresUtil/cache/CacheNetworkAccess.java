package edu.sc.seis.fissuresUtil.cache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class CacheNetworkAccess extends ProxyNetworkAccess {

    public CacheNetworkAccess(NetworkAccess net) {
        super(net);
    }
    
    public CacheNetworkAccess(NetworkAccess net, NetworkAttrImpl attr) {
        super(net);
        this.attr = attr;
    }
    
    public static CacheNetworkAccess create(NetworkAttrImpl attr, FissuresNamingService fisName) throws NetworkNotFound {
        VestingNetworkDC netdc = new VestingNetworkDC(attr.getSourceServerDNS(),
                                                      attr.getSourceServerName(),
                                                      fisName);
        VestingNetworkFinder vFinder = (VestingNetworkFinder)netdc.a_finder();
        return vFinder.vest(attr);   
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
        knownSites.clear();
        sensMap.clear();
        super.reset();
    }

    public NetworkAttrImpl get_attributes() {
        synchronized(this) {
            if(attr == null) {
                attr = (NetworkAttrImpl)getNetworkAccess().get_attributes();
                NetworkAttr.intern(attr);
            }
        }
        return attr;
    }

    /**
     * retreives the stations for the network, but uses the cached copy if it
     * has been previously retrieved. The stations are also cleaned of duplicate
     * networkAttr objects to free memory.
     */
    public Station[] retrieve_stations() {
        synchronized(knownStations) {
            if(stations == null) {
                stations = getNetworkAccess().retrieve_stations();
                for(int i = 0; i < stations.length; i++) {
                    stations[i] = Station.intern(stations[i]);
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
                Channel[] chans = getNetworkAccess().retrieve_for_station(id);
                for(int i = 0; i < chans.length; i++) {
                    Channel.intern(chans[i]);
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
        Instrumentation inst = getNetworkAccess().retrieve_instrumentation(id, the_time);
        updateHolder(id, the_time, inst);
        return inst;
    }

    private SensitivityHolder updateHolder(ChannelId id,
                                           Time the_time,
                                           Instrumentation inst) {
        if(inst.the_response.stages.length == 0) {
            throw new InstrumentationInvalid(id,
                                             "Instrumentation has no stages, units cannot be determined.");
        }
        SensitivityHolder holder = extractExistingHolder(id, the_time);
        List sensForChannel = extractSensForChannel(id);
        if(holder == null) {
            holder = new SensitivityHolder(inst);
            sensForChannel.add(holder);
        } else {
            holder.updateHoldings(inst);
        }
        return holder;
    }

    private SensitivityHolder extractExistingHolder(ChannelId id, Time time) {
        MicroSecondDate date = new MicroSecondDate(time);
        List sensForChannel = extractSensForChannel(id);
        for(Iterator iter = sensForChannel.iterator(); iter.hasNext();) {
            SensitivityHolder holder = (SensitivityHolder)iter.next();
            if(holder.range.contains(date)) {
                return holder;
            }
        }
        return null;
    }

    private List extractSensForChannel(ChannelId id) {
        String idString = ChannelIdUtil.toString(id);
        List sensForChannel = (List)sensMap.get(idString);
        if(sensForChannel == null) {
            sensForChannel = new ArrayList();
            sensMap.put(idString, sensForChannel);
        }
        return sensForChannel;
    }

    private SensitivityHolder getFilledHolder(ChannelId id, Time the_time)
            throws ChannelNotFound {
        SensitivityHolder holder = extractExistingHolder(id, the_time);
        if(holder == null) {
            retrieve_instrumentation(id, the_time);
            return extractExistingHolder(id, the_time);
        }
        return holder;
    }

    public Sensitivity retrieve_sensitivity(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return getFilledHolder(id, the_time).sensitivity;
    }

    public Unit retrieve_initial_units(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return getFilledHolder(id, the_time).initialUnits;
    }

    public Unit retrieve_final_units(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return getFilledHolder(id, the_time).finalUnits;
    }

    class SensitivityHolder {

        public SensitivityHolder(Instrumentation inst) {
            updateHoldings(inst);
        }

        private void updateHoldings(Instrumentation inst) {
            this.sensitivity = inst.the_response.the_sensitivity;
            Stage[] stages = inst.the_response.stages;
            this.initialUnits = stages[0].input_units;
            this.finalUnits = stages[stages.length - 1].output_units;
            this.range = new MicroSecondTimeRange(inst.effective_time);
        }

        Sensitivity sensitivity;

        Unit initialUnits, finalUnits;

        MicroSecondTimeRange range;
    }

    private  Map<String, Site> knownSites = Collections.synchronizedMap(new HashMap<String, Site>());
    
    private Map<String, Station> knownStations = Collections.synchronizedMap(new HashMap<String, Station>());

    protected NetworkAttrImpl attr;

    protected Station[] stations;

    private HashMap channelMap = new HashMap();

    private HashMap sensMap = new HashMap();

    private static Logger logger = Logger.getLogger(CacheNetworkAccess.class);
}