
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.network.StationIdUtil;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class CacheNetworkAccess implements NetworkAccess {

    public CacheNetworkAccess(NetworkAccess net) {
        if (net == null) {
            throw new NullPointerException("network is null");
        } // end of if (net == null)

        this.net = net;
    }

        //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/get_attributes:1.0
    //
    /***/

    public NetworkAttr
    get_attributes() {
    if (attr == null) {
        attr = net.get_attributes();
    }
    return attr;
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_stations:1.0
    //
    /***/

    public Station[]
    retrieve_stations() {
    if (stations == null) {
        stations = net.retrieve_stations();
    }
    return stations;
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_for_station:1.0
    //
    /***/

    public Channel[]
    retrieve_for_station(StationId id) {
    String idStr = StationIdUtil.toString(id);
    if ( ! channelMap.containsKey(idStr)) {
        channelMap.put(idStr, net.retrieve_for_station(id));
    }
    return (Channel[])channelMap.get(idStr);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_grouping:1.0
    //
    /***/

    public ChannelId[]
    retrieve_grouping(ChannelId id)
        throws ChannelNotFound {
    return net.retrieve_grouping(id);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_groupings:1.0
    //
    /***/

    public ChannelId[][]
    retrieve_groupings() {
    return net.retrieve_groupings();
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_channel:1.0
    //
    /***/

    public Channel
    retrieve_channel(ChannelId id)
        throws ChannelNotFound {
    return net.retrieve_channel(id);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_channels_by_code:1.0
    //
    /***/

    public Channel[]
    retrieve_channels_by_code(String station_code,
                              String site_code,
                              String channel_code)
        throws ChannelNotFound {
    return net.retrieve_channels_by_code(station_code, site_code, channel_code);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/locate_channels:1.0
    //
    /***/

    public Channel[]
    locate_channels(edu.iris.Fissures.Area the_area,
                    SamplingRange sampling,
                    OrientationRange orientation) {
    return net.locate_channels(the_area, sampling, orientation);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_instrumentation:1.0
    //
    /***/

    public Instrumentation
    retrieve_instrumentation(ChannelId id,
                             edu.iris.Fissures.Time the_time)
        throws ChannelNotFound {
    return net.retrieve_instrumentation(id, the_time);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_calibrations:1.0
    //
    /***/

    public Calibration[]
    retrieve_calibrations(ChannelId id,
                          edu.iris.Fissures.TimeRange the_time)
        throws ChannelNotFound,
               edu.iris.Fissures.NotImplemented {
    return net.retrieve_calibrations(id, the_time);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_time_corrections:1.0
    //
    /***/

    public TimeCorrection[]
    retrieve_time_corrections(ChannelId id,
                              edu.iris.Fissures.TimeRange time_range)
        throws ChannelNotFound,
               edu.iris.Fissures.NotImplemented {
    return net.retrieve_time_corrections(id, time_range);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/retrieve_all_channels:1.0
    //
    /***/

    public ChannelId[]
    retrieve_all_channels(int seq_max,
                          ChannelIdIterHolder iter) {
    return net.retrieve_all_channels(seq_max, iter);
    }

    //
    // IDL:iris.edu/Fissures/IfNetwork/NetworkAccess/get_audit_trail_for_channel:1.0
    //
    /***/

    public edu.iris.Fissures.AuditElement[]
    get_audit_trail_for_channel(ChannelId id)
        throws ChannelNotFound,
               edu.iris.Fissures.NotImplemented {
    return net.get_audit_trail_for_channel(id);
    }

    //
    // IDL:iris.edu/Fissures/AuditSystemAccess/get_audit_trail:1.0
    //
    /***/

    public AuditElement[]
    get_audit_trail()
        throws NotImplemented {
    return net.get_audit_trail();
    }

    NetworkAccess net;
    NetworkAttr attr;
    Station[] stations;
    HashMap channelMap = new HashMap();

    static Logger logger = Logger.getLogger(CacheNetworkAccess.class);
}
