package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class TimeSensitiveMockEventFinder extends MockEventFinder {

    public TimeSensitiveMockEventFinder() {
        super(new EventAccessOperations[0]);
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
        MicroSecondTimeRange tr = new MicroSecondTimeRange(time_range);
        return wrap(MockEventAccessOperations.createEvents(tr, 3, 6));
    }
}