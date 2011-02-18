package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAccessSeqHolder;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIter;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;

/**
 * @author groves Created on Nov 9, 2004
 */
public class MockEventFinder implements EventFinder {

    public MockEventFinder() {
        this(MockEventAccessOperations.createEventTimeRange());
    }

    public MockEventFinder(EventAccessOperations[] servedEvents) {
        events = wrap(servedEvents);
    }

    public static MockEventAccess[] wrap(EventAccessOperations[] evs) {
        MockEventAccess[] events = new MockEventAccess[evs.length];
        for(int i = 0; i < evs.length; i++) {
            events[i] = new MockEventAccess(evs[i]);
        }
        return events;
    }

    private MockEventAccess[] events;

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
        return prepareReturn(events, iter, seq_max);
    }

    private EventAccess[] prepareReturn(final MockEventAccess[] queryEvents,
                                        final EventSeqIterHolder iter,
                                        final int seq_max) {
        if(seq_max < queryEvents.length) {
            EventAccess[] initialEvents = new EventAccess[seq_max];
            System.arraycopy(queryEvents, 0, initialEvents, 0, seq_max);
            iter.value = new EventSeqIter() {

                int curPosition = seq_max;

                public int how_many_remain() {
                    return queryEvents.length - curPosition;
                }

                public boolean next_n(int how_many, EventAccessSeqHolder seq) {
                    int numReturned = how_many;
                    if(how_many > how_many_remain()) {
                        numReturned = how_many_remain();
                    }
                    seq.value = new EventAccess[numReturned];
                    System.arraycopy(queryEvents,
                                     curPosition,
                                     seq.value,
                                     0,
                                     numReturned);
                    curPosition += numReturned;
                    return numReturned <= how_many;
                }

                public void destroy() {
                // TODO Auto-generated method stub
                }

                public boolean _is_a(String repositoryIdentifier) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public boolean _is_equivalent(Object other) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public boolean _non_existent() {
                    // TODO Auto-generated method stub
                    return false;
                }

                public int _hash(int maximum) {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public Object _duplicate() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public void _release() {
                // TODO Auto-generated method stub
                }

                public Object _get_interface_def() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Request _request(String operation) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Request _create_request(Context ctx,
                                               String operation,
                                               NVList arg_list,
                                               NamedValue result) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Request _create_request(Context ctx,
                                               String operation,
                                               NVList arg_list,
                                               NamedValue result,
                                               ExceptionList exclist,
                                               ContextList ctxlist) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Policy _get_policy(int policy_type) {
                    // TODO Auto-generated method stub
                    return null;
                }

                public DomainManager[] _get_domain_managers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                public Object _set_policy_override(Policy[] policies,
                                                   SetOverrideType set_add) {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
            return initialEvents;
        }
        return queryEvents;
    }

    public EventAccess[] get_by_name(String name) {
        return null;
    }

    public String[] known_catalogs() {
        List catalogs = new ArrayList();
        for(int i = 0; i < events.length; i++) {
            catalogs.add(events[i].getOrigin().getCatalog());
        }
        return (String[])catalogs.toArray(new String[0]);
    }

    public String[] known_contributors() {
        List contribs = new ArrayList();
        for(int i = 0; i < events.length; i++) {
            contribs.add(events[i].getOrigin().getContributor());
        }
        return (String[])contribs.toArray(new String[0]);
    }

    public String[] catalogs_from(String contrib) {
        return null;
    }

    public EventFactory a_factory() {
        return null;
    }

    public EventFinder a_finder() {
        return null;
    }

    public EventChannelFinder a_channel_finder() {
        return null;
    }

    public void _release() {}

    public boolean _non_existent() {
        return false;
    }

    public int _hash(int arg0) {
        return 0;
    }

    public boolean _is_a(String arg0) {
        return false;
    }

    public DomainManager[] _get_domain_managers() {
        return null;
    }

    public Object _duplicate() {
        return null;
    }

    public Object _get_interface_def() {
        return null;
    }

    public boolean _is_equivalent(Object arg0) {
        return false;
    }

    public Policy _get_policy(int arg0) {
        return null;
    }

    public Request _request(String arg0) {
        return null;
    }

    public Object _set_policy_override(Policy[] arg0, SetOverrideType arg1) {
        return null;
    }

    public Request _create_request(Context arg0,
                                   String arg1,
                                   NVList arg2,
                                   NamedValue arg3) {
        return null;
    }

    public Request _create_request(Context arg0,
                                   String arg1,
                                   NVList arg2,
                                   NamedValue arg3,
                                   ExceptionList arg4,
                                   ContextList arg5) {
        return null;
    }
}