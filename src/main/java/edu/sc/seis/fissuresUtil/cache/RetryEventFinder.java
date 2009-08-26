package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.SystemException;
import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;

public class RetryEventFinder extends ProxyEventFinder implements EventFinder {

    public RetryEventFinder(ProxyEventDC edc, RetryStrategy strat) {
        super(new NSEventFinder(edc));
        this.handler = strat;
    }

    public EventChannelFinder a_channel_finder() {
        int count = 0;
        SystemException latest;
        try {
            return ef.a_channel_finder();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventChannelFinder result = ef.a_channel_finder();
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

    public EventFactory a_factory() {
        int count = 0;
        SystemException latest;
        try {
            return ef.a_factory();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventFactory result = ef.a_factory();
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

    public EventFinder a_finder() {
        int count = 0;
        SystemException latest;
        try {
            return ef.a_finder();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventFinder result = ef.a_finder();
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

    public String[] catalogs_from(String contrib) {
        int count = 0;
        SystemException latest;
        try {
            return ef.catalogs_from(contrib);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                String[] result = ef.catalogs_from(contrib);
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

    public EventAccess[] get_by_name(String name) {
        int count = 0;
        SystemException latest;
        try {
            return ef.get_by_name(name);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventAccess[] result = ef.get_by_name(name);
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

    public String[] known_catalogs() {
        int count = 0;
        SystemException latest;
        try {
            return ef.known_catalogs();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                String[] result = ef.known_catalogs();
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

    public String[] known_contributors() {
        int count = 0;
        SystemException latest;
        try {
            return ef.known_contributors();
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                String[] result = ef.known_contributors();
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

    public EventAccess[] query_events(Area the_area,
                                      Quantity min_depth,
                                      Quantity max_depth,
                                      TimeRange time_range,
                                      String[] search_types,
                                      float min_magnitude,
                                      float max_magnitude,
                                      String[] catalogs,
                                      String[] contributors,
                                      int seq_max,
                                      EventSeqIterHolder iter) {
        int count = 0;
        SystemException latest;
        try {
            return ef.query_events(the_area,
                                   min_depth,
                                   max_depth,
                                   time_range,
                                   search_types,
                                   min_magnitude,
                                   max_magnitude,
                                   catalogs,
                                   contributors,
                                   seq_max,
                                   iter);
        } catch(SystemException t) {
            latest = t;
        } catch(OutOfMemoryError e) {
            throw new RuntimeException("Out of memory", e);
        }
        while(shouldRetry(count++, latest)) {
            try {
                EventAccess[] result = ef.query_events(the_area,
                                                       min_depth,
                                                       max_depth,
                                                       time_range,
                                                       search_types,
                                                       min_magnitude,
                                                       max_magnitude,
                                                       catalogs,
                                                       contributors,
                                                       seq_max,
                                                       iter);
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
}
