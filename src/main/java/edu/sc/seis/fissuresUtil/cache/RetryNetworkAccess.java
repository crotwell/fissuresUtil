package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

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
import edu.iris.Fissures.network.ChannelIdUtil;

/**
 * Just a pass thru class for the remote networkAccess, but this will retry if
 * there are errors, up to the specified number. This can help in the case of
 * temporary network/server errors, but may simply waste time in the case of
 * bigger errors.
 */
public class RetryNetworkAccess extends ProxyNetworkAccess {

    public RetryNetworkAccess(NetworkAccess net,
                              int retry){
        this(net, new ClassicRetryStrategy(retry));
    }

    public RetryNetworkAccess(NetworkAccess net,
                              RetryStrategy handler) {
        super(net);
        this.handler = handler;
    }

    public NetworkAttr get_attributes() {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().get_attributes();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                NetworkAttr result = getNetworkAccess().get_attributes();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId id)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().get_audit_trail_for_channel(id);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                AuditElement[] result = getNetworkAccess().get_audit_trail_for_channel(id);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().get_audit_trail();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                AuditElement[] result = getNetworkAccess().get_audit_trail();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Channel[] locate_channels(Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().locate_channels(the_area, sampling, orientation);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Channel[] result = getNetworkAccess().locate_channels(the_area,
                                                       sampling,
                                                       orientation);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public ChannelId[] retrieve_all_channels(int seq_max,
                                             ChannelIdIterHolder iter) {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_all_channels(seq_max, iter);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                ChannelId[] result = getNetworkAccess().retrieve_all_channels(seq_max, iter);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Calibration[] retrieve_calibrations(ChannelId id, TimeRange the_time)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_calibrations(id, the_time);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Calibration[] result = getNetworkAccess().retrieve_calibrations(id, the_time);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_channel(id);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Channel result = getNetworkAccess().retrieve_channel(id);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
            throws ChannelNotFound {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_channels_by_code(station_code,
                                                 site_code,
                                                 channel_code);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Channel[] result = getNetworkAccess().retrieve_channels_by_code(station_code,
                                                                 site_code,
                                                                 channel_code);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Channel[] retrieve_for_station(StationId p1) {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_for_station(p1);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Channel[] result = getNetworkAccess().retrieve_for_station(p1);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public ChannelId[] retrieve_grouping(ChannelId id) throws ChannelNotFound {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_grouping(id);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                ChannelId[] result = getNetworkAccess().retrieve_grouping(id);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public ChannelId[][] retrieve_groupings() {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_groupings();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                ChannelId[][] result = getNetworkAccess().retrieve_groupings();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time)
            throws ChannelNotFound {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_instrumentation(id, the_time);
        } catch(UNKNOWN t) {
            latest = t;
            logger.warn("UNKNOWN on retrieve_instrumentation("+ChannelIdUtil.toString(id)+", "+the_time.date_time);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Instrumentation result = getNetworkAccess().retrieve_instrumentation(id,
                                                                      the_time);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public Station[] retrieve_stations() {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_stations();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                Station[] result = getNetworkAccess().retrieve_stations();
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId id,
                                                      TimeRange time_range)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException latest;
        try {
            return getNetworkAccess().retrieve_time_corrections(id, time_range);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                TimeCorrection[] result = getNetworkAccess().retrieve_time_corrections(id,
                                                                        time_range);
                handler.serverRecovered(this);
                return result;
            } catch(SystemException t) {
                latest = t;
            } catch(OutOfMemoryError e) {
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw latest;
    }

    private boolean shouldRetry(int count, SystemException t) {
        return handler.shouldRetry(t, this, count);
    }

    private RetryStrategy handler;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RetryNetworkAccess.class);
}
