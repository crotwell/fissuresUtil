package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;

public abstract class ProxyNetworkAccess implements NetworkAccess{
    public ProxyNetworkAccess(NetworkAccess net){ this.net = net; }

    /**
     * If this RetryNetworkAccess is holding onto a ProxyNetworkAccess, it calls
     * reset on that network access.  Otherwise it just falls through.
     */
    public void reset(){
        if(net instanceof ProxyNetworkAccess){
            ((ProxyNetworkAccess)net).reset();
        }
    }

    public NetworkAccess getNetworkAccess(){
        if(net instanceof ProxyNetworkAccess){
            return ((ProxyNetworkAccess)net).getNetworkAccess();
        }else{
            return net;
        }
    }
    public NetworkAttr get_attributes() { return net.get_attributes(); }

    public Station[] retrieve_stations() { return net.retrieve_stations(); }

    public Channel[] retrieve_for_station(StationId p1) {
        return net.retrieve_for_station(p1);
    }

    public ChannelId[]  retrieve_grouping(ChannelId id)throws ChannelNotFound {
        return net.retrieve_grouping(id);
    }

    public ChannelId[][] retrieve_groupings(){return net.retrieve_groupings();}

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        return net.retrieve_channel(id);
    }

    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
        throws ChannelNotFound {
        return net.retrieve_channels_by_code(station_code, site_code, channel_code);
    }

    public Channel[] locate_channels(Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        return net.locate_channels(the_area, sampling, orientation);
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time)
        throws ChannelNotFound {
        return net.retrieve_instrumentation(id, the_time);
    }

    public Calibration[]retrieve_calibrations(ChannelId id, TimeRange the_time)
        throws ChannelNotFound,NotImplemented {
        return net.retrieve_calibrations(id, the_time);
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId id,
                                                      TimeRange time_range)
        throws ChannelNotFound, NotImplemented {
        return net.retrieve_time_corrections(id, time_range);
    }

    public ChannelId[]retrieve_all_channels(int seq_max, ChannelIdIterHolder iter) {
        return net.retrieve_all_channels(seq_max, iter);
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId id)
        throws ChannelNotFound, NotImplemented {
        return net.get_audit_trail_for_channel(id);
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        return net.get_audit_trail();
    }

    protected NetworkAccess net;
}

