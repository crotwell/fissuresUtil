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
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Nov 9, 2004
 */
public class MockEventFinder implements EventFinder {
    public MockEventFinder(){
        this(MockEventAccessOperations.createEventTimeRange());
    }
    
    public MockEventFinder(EventAccessOperations[] servedEvents){
        events = new MockEventAccess[servedEvents.length];
        for(int i = 0; i < servedEvents.length; i++) {
            events[i] = new MockEventAccess(servedEvents[i]);
        }
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
        return events;
    }

    public EventAccess[] get_by_name(String name) {
        return null;
    }

    public String[] known_catalogs() {
        List catalogs = new ArrayList();
        for(int i = 0; i < events.length; i++) {
            catalogs.add(events[i].getOrigin().catalog);
        }
        return (String[])catalogs.toArray(new String[0]);
    }

    public String[] known_contributors() {
        List contribs = new ArrayList();
        for(int i = 0; i < events.length; i++) {
            contribs.add(events[i].getOrigin().contributor);
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