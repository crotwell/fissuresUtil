package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;
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
import edu.iris.Fissures.network.StationIdUtil;

/**
 * Just a pass thru class for the remote networkAccess, but this will retry if
 * there are errors, up to the specified number. This can help in the case of
 * temporary network/server errors, but may simply waste time in the case of
 * bigger errors.
 */
public class RetryNetworkAccess extends ProxyNetworkAccess {

    public RetryNetworkAccess(NetworkAccess net, int retry) {
        super(net);
        this.retry = retry;
    }

    public ChannelId[] retrieve_grouping(ChannelId id) throws ChannelNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_grouping(id);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Station[] retrieve_stations() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_stations();
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Channel[] retrieve_for_station(StationId id) {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                if(count != 0) {
                    logger.info("before retrieve_for_station after failure "
                            + count + " of " + retry);
                }
                return net.retrieve_for_station(id);
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + count + " of "
                        + retry + " on " + StationIdUtil.toString(id), t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public AuditElement[] get_audit_trail_for_channel(ChannelId id)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.get_audit_trail_for_channel(id);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public TimeCorrection[] retrieve_time_corrections(ChannelId id,
                                                      TimeRange time_range)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_time_corrections(id, time_range);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public NetworkAttr get_attributes() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.get_attributes();
            } catch(SystemException t) {
                lastException = t;
                logger.warn("Caught exception, retrying " + ++count + " of "
                        + retry, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
        }
        throw lastException;
    }

    public ChannelId[][] retrieve_groupings() {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_groupings();
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Instrumentation retrieve_instrumentation(ChannelId id, Time the_time)
            throws ChannelNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_instrumentation(id, the_time);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.get_audit_trail();
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_channel(id);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Channel[] locate_channels(Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.locate_channels(the_area, sampling, orientation);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public ChannelId[] retrieve_all_channels(int seq_max,
                                             ChannelIdIterHolder iter) {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_all_channels(seq_max, iter);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Calibration[] retrieve_calibrations(ChannelId id, TimeRange the_time)
            throws ChannelNotFound, NotImplemented {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_calibrations(id, the_time);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
            throws ChannelNotFound {
        int count = 0;
        SystemException lastException = null;
        while(count < retry || retry == -1) {
            try {
                return net.retrieve_channels_by_code(station_code,
                                                     site_code,
                                                     channel_code);
            } catch(SystemException t) {
                lastException = t;
                logMessage(count, t);
                BulletproofVestFactory.retrySleep(count);
                reset();
            } catch(OutOfMemoryError e) {
                // repackage to get at least a partial stack trace
                throw new RuntimeException("Out of memory", e);
            }
            count++;
        }
        throw lastException;
    }

    private void logMessage(int count, SystemException t) {
        if(retry != -1) {
            logger.debug("Caught exception, retrying " + count + " of " + retry,
                         t);
        } else {
            logger.debug("Caught exception, retrying " + count + " of infinity",
                         t);
        }
    }

    private int retry;

    private static Logger logger = Logger.getLogger(RetryNetworkAccess.class);
}
