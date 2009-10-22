package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;


public class VestingEventFinder extends ProxyEventFinder {

    public VestingEventFinder(ProxyEventFinder edc, RetryStrategy strat) {
        super(edc);
        this.handler = strat;
    }

    @Override
    public EventAccess[] get_by_name(String name) {
        return BulletproofVestFactory.vestEventAccess(super.get_by_name(name));
    }

    @Override
    public EventAccess[] query_events(Area theArea,
                                      Quantity minDepth,
                                      Quantity maxDepth,
                                      TimeRange timeRange,
                                      String[] searchTypes,
                                      float minMagnitude,
                                      float maxMagnitude,
                                      String[] catalogs,
                                      String[] contributors,
                                      int seqMax,
                                      EventSeqIterHolder iter) {
        return BulletproofVestFactory.vestEventAccess(super.query_events(theArea,
                                  minDepth,
                                  maxDepth,
                                  timeRange,
                                  searchTypes,
                                  minMagnitude,
                                  maxMagnitude,
                                  catalogs,
                                  contributors,
                                  seqMax,
                                  iter));
    }
    
    private RetryStrategy handler;
}
