/**
 * RetryNetworkAccess.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Calibration;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.TimeCorrection;
import edu.iris.Fissures.IfNetwork.ChannelIdIterHolder;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.NotImplemented;
import org.apache.log4j.Logger;

/** Just a pass thru class for the remote networkAccess, but this will retry
 *  if there are errors, up to the specified number. This can help in the
 *  case of temporary network/server errors, but may simply waste time in
 *  the case of bigger errors. */
public class RetryNetworkAccess implements NetworkAccess {

    public RetryNetworkAccess(NetworkAccess net, int retry) {
        this.net = net;
        this.retry = retry;
    }

    public NetworkAccess getNetworkAccess() {
        return net;
    }

    public ChannelId[] retrieve_grouping(ChannelId id) throws ChannelNotFound {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_grouping(id);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Station[] retrieve_stations() {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_stations();
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Channel[] retrieve_for_station(StationId id) {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                if (count != 0) {
                    logger.info("before retrieve_for_station after failure "+count+" of "+retry);
                }
                return net.retrieve_for_station(id);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public AuditElement[] get_audit_trail_for_channel(ChannelId id) throws ChannelNotFound, NotImplemented {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.get_audit_trail_for_channel(id);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public TimeCorrection[] retrieve_time_corrections(ChannelId id, TimeRange time_range) throws ChannelNotFound, NotImplemented {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_time_corrections(id, time_range);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public NetworkAttr get_attributes() {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.get_attributes();
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public ChannelId[][] retrieve_groupings() {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_groupings();
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time) throws ChannelNotFound {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_instrumentation(id, the_time);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public AuditElement[] get_audit_trail() throws NotImplemented {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.get_audit_trail();
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_channel(id);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Channel[] locate_channels(Area the_area, SamplingRange sampling, OrientationRange orientation) {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.locate_channels(the_area, sampling, orientation);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public ChannelId[] retrieve_all_channels(int seq_max, ChannelIdIterHolder iter) {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_all_channels(seq_max, iter);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Calibration[] retrieve_calibrations(ChannelId id, TimeRange the_time) throws ChannelNotFound, NotImplemented {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_calibrations(id, the_time);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public Channel[] retrieve_channels_by_code(String station_code, String site_code, String channel_code) throws ChannelNotFound {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return net.retrieve_channels_by_code(station_code, site_code, channel_code);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count+" of "+retry, t);
            }
            count++;
        }
        throw lastException;
    }

    NetworkAccess net;
    int retry;

    static Logger logger =
        Logger.getLogger(RetryNetworkAccess.class);

}

