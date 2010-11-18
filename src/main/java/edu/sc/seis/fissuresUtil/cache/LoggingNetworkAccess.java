package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Calibration;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfNetwork.TimeCorrection;


public class LoggingNetworkAccess extends ProxyNetworkAccess {

    public LoggingNetworkAccess(NetworkAccess net) {
        super(net);
        logger.info("new LoggingNetworkAccess(NetworkAccess net)");
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        logger.info("get_audit_trail()");
        return super.get_audit_trail();
    }

    public NetworkAttr get_attributes() {
        logger.info("get_attributes()");
        return super.get_attributes();
    }

    public Station[] retrieve_stations() {
        logger.info("retrieve_stations()");
        return super.retrieve_stations();
    }

    public Channel[] retrieve_for_station(StationId id) {
        logger.info("retrieve_for_station(StationId id)");
        return super.retrieve_for_station( id);
    }

    public ChannelId[] retrieve_grouping(ChannelId id) throws ChannelNotFound {
        logger.info("retrieve_grouping(ChannelId id)");
        return super.retrieve_grouping( id);
    }

    public ChannelId[][] retrieve_groupings() {
        logger.info("retrieve_groupings()");
        return super.retrieve_groupings();
    }

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        logger.info("retrieve_channel(ChannelId id)");
        return super.retrieve_channel( id);
    }

    public Channel[] retrieve_channels_by_code(String station_code, String site_code, String channel_code)
            throws ChannelNotFound {
        logger.info("retrieve_channels_by_code(String station_code, String site_code, String channel_code)");
        return super.retrieve_channels_by_code( station_code,  site_code,  channel_code);
    }

    public Channel[] locate_channels(Area the_area, SamplingRange sampling, OrientationRange orientation) {
        logger.info("locate_channels(Area the_area, SamplingRange sampling, OrientationRange orientation) {");
        return super.locate_channels( the_area,  sampling,  orientation);
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time) throws ChannelNotFound {
        logger.info("retrieve_instrumentation(ChannelId id, Time the_time)");
        return super.retrieve_instrumentation( id,  the_time);
    }

    public Calibration[] retrieve_calibrations(ChannelId id, TimeRange the_time) throws ChannelNotFound, NotImplemented {
        logger.info("retrieve_calibrations(ChannelId id, TimeRange the_time)");
        return super.retrieve_calibrations( id,  the_time);
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId id, TimeRange time_range) throws ChannelNotFound,
            NotImplemented {
        logger.info("retrieve_time_corrections(ChannelId id, TimeRange time_range)");
        return super.retrieve_time_corrections( id,  time_range);
    }

    public ChannelId[] retrieve_all_channels(int seq_max, ChannelIdIterHolder iter) {
        logger.info("retrieve_all_channels(int seq_max, ChannelIdIterHolder iter)");
        return super.retrieve_all_channels( seq_max,  iter);
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId id) throws ChannelNotFound, NotImplemented {
        logger.info("get_audit_trail_for_channel(ChannelId id)");
        return super.get_audit_trail_for_channel(id);
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LoggingNetworkAccess.class);
}
