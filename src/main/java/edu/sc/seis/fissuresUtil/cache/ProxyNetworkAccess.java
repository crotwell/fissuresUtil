package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.NO_IMPLEMENT;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.Calibration;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.ConcreteNetwork;
import edu.iris.Fissures.IfNetwork.ConcreteNetworkAccess;
import edu.iris.Fissures.IfNetwork.ConcreteNetworkAccessOperations;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.SiteNotFound;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfNetwork.StationNotFound;
import edu.iris.Fissures.IfNetwork.TimeCorrection;
import edu.iris.Fissures.IfNetwork.VirtualNetworkAccess;

public abstract class ProxyNetworkAccess implements NetworkAccess, ConcreteNetworkAccessOperations, CorbaServerWrapper {

    public ProxyNetworkAccess(NetworkAccess net) {
        setNetworkAccess(net);
    }

    /**
     * If this ProxyNetworkAccess is holding onto a ProxyNetworkAccess, it calls
     * reset on that network access. Otherwise it just falls through.
     */
    public void reset() {
        if(getNetworkAccess() instanceof ProxyNetworkAccess) {
            ((ProxyNetworkAccess)getNetworkAccess()).reset();
        } else if(getNetworkAccess() instanceof ConcreteNetworkAccess) {
            // must be real corba object, so discard to allow orb to reclaim/reopen sockets after garbage collection
            ((ConcreteNetworkAccess)net)._release();
            setNetworkAccess(null);
        } else if(getNetworkAccess() instanceof VirtualNetworkAccess) {
            // must be real corba object, so discard to allow orb to reclaim/reopen sockets after garbage collection
            ((VirtualNetworkAccess)net)._release();
            setNetworkAccess(null);
        }
    }

    public NetworkAccess getCorbaObject() {
        if(getNetworkAccess() instanceof ProxyNetworkAccess) {
            return ((ProxyNetworkAccess)getNetworkAccess()).getCorbaObject();
        }
        return getNetworkAccess();
    }

    public String getServerDNS() {
        if(getNetworkAccess() instanceof ProxyNetworkAccess) {
            return ((ProxyNetworkAccess)getNetworkAccess()).getServerDNS();
        }
        return getNetworkAccess().getClass().getName()+":Unknown";
    }

    public String getServerName() {
        if(getNetworkAccess() instanceof ProxyNetworkAccess) {
            return ((ProxyNetworkAccess)getNetworkAccess()).getServerName();
        }
        return null;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }

    
    public String getServerType(){
        return NETACCESS_TYPE;
    }

    protected void setNetworkAccess(NetworkAccess na) {
        net = na;
    }

    public NetworkAttr get_attributes() {
        return getNetworkAccess().get_attributes();
    }

    public Station[] retrieve_stations() {
        return getNetworkAccess().retrieve_stations();
    }

    public Channel[] retrieve_for_station(StationId p1) {
        return getNetworkAccess().retrieve_for_station(p1);
    }

    public ChannelId[] retrieve_grouping(ChannelId id) throws ChannelNotFound {
        return getNetworkAccess().retrieve_grouping(id);
    }

    public ChannelId[][] retrieve_groupings() {
        return getNetworkAccess().retrieve_groupings();
    }

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        return getNetworkAccess().retrieve_channel(id);
    }

    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
            throws ChannelNotFound {
        return getNetworkAccess().retrieve_channels_by_code(station_code,
                                             site_code,
                                             channel_code);
    }

    public Channel[] locate_channels(Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        return getNetworkAccess().locate_channels(the_area, sampling, orientation);
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return getNetworkAccess().retrieve_instrumentation(id, the_time);
    }

    public Sensitivity retrieve_sensitivity(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return retrieve_instrumentation(id, the_time).the_response.the_sensitivity;
    }

    public Unit retrieve_initial_units(ChannelId id, Time the_time)
            throws ChannelNotFound {
        return retrieve_instrumentation(id, the_time).the_response.stages[0].input_units;
    }

    public Unit retrieve_final_units(ChannelId id, Time the_time)
            throws ChannelNotFound {
        Stage[] stages = retrieve_instrumentation(id, the_time).the_response.stages;
        return stages[stages.length - 1].output_units;
    }

    public Calibration[] retrieve_calibrations(ChannelId id, TimeRange the_time)
            throws ChannelNotFound, NotImplemented {
        return getNetworkAccess().retrieve_calibrations(id, the_time);
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId id,
                                                      TimeRange time_range)
            throws ChannelNotFound, NotImplemented {
        return getNetworkAccess().retrieve_time_corrections(id, time_range);
    }

    public ChannelId[] retrieve_all_channels(int seq_max,
                                             ChannelIdIterHolder iter) {
        return getNetworkAccess().retrieve_all_channels(seq_max, iter);
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId id)
            throws ChannelNotFound, NotImplemented {
        return getNetworkAccess().get_audit_trail_for_channel(id);
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        return getNetworkAccess().get_audit_trail();
    }

    public NetworkAccess getNetworkAccess() {
        return net;
    }
    
    public ConcreteNetwork a_writeable() {
        throw new NO_IMPLEMENT();
    }

    public AuditElement[] get_audit_trail_for_site(SiteId id) throws SiteNotFound, NotImplemented {
        throw new NotImplemented();
    }

    public AuditElement[] get_audit_trail_for_station(StationId id) throws StationNotFound, NotImplemented {
        throw new NotImplemented();
    }

    private NetworkAccess net;
}
