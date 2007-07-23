package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventFinderOperations;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;

public class NSEventFinder extends ProxyEventFinder {

    public NSEventFinder(ProxyEventDC edc) {
        this.edc = edc;
    }

    private EventFinder getFinder() {
        if(finder.get() == null) {
            finder.set(edc.a_finder());
        }
        return (EventFinder)finder.get();
    }

    public void reset() {
        edc.reset();
        if(finder.get() != null) {
            ((EventFinder)finder.get())._release();
        }
        finder.set(null);
    }

    public EventChannelFinder a_channel_finder() {
        try {
            return getFinder().a_channel_finder();
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().a_channel_finder();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public EventFactory a_factory() {
        try {
            return getFinder().a_factory();
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().a_factory();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public EventFinder a_finder() {
        try {
            return getFinder().a_finder();
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().a_finder();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String[] catalogs_from(String contrib) {
        try {
            return getFinder().catalogs_from(contrib);
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().catalogs_from(contrib);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public EventAccess[] get_by_name(String name) {
        try {
            return getFinder().get_by_name(name);
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().get_by_name(name);
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String[] known_catalogs() {
        try {
            return getFinder().known_catalogs();
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().known_catalogs();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    public String[] known_contributors() {
        try {
            return getFinder().known_contributors();
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().known_contributors();
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
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
        try {
            return getFinder().query_events(the_area,
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
        } catch(Throwable e) {
            reset();
            try {
                return getFinder().query_events(the_area,
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
            } catch(RuntimeException ee) {
                reset();
                throw ee;
            }
        } // end of try-catch
    }

    private ThreadLocal finder; // holds an EventFinder per thread

    private ProxyEventDC edc;
}
