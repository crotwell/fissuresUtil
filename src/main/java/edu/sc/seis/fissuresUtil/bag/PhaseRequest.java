package edu.sc.seis.fissuresUtil.bag;

import java.util.List;

import org.apache.log4j.Logger;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.LocationUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

public class PhaseRequest  {

    protected PhaseRequest(String beginPhase, String endPhase, String model)
            throws TauModelException {
        this.beginPhase = beginPhase;
        this.endPhase = endPhase;
            util = TauPUtil.getTauPUtil(model);
    }

    public PhaseRequest(String beginPhase,
                        TimeInterval beginOffest,
                        String endPhase,
                        TimeInterval endOffset,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = beginOffest;
        this.endOffset = endOffset;
    }
    
    public PhaseRequest(String beginPhase,
                        TimeInterval beginOffset,
                        String endPhase,
                        double endOffestRatio,
                        TimeInterval endOffsetMinimum,
                        boolean negateEndOffsetRatio,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = beginOffset;
        this.endOffset = null;
        this.endOffsetRatio = endOffestRatio;
        this.endOffsetRatioMinimum = endOffsetMinimum;
        this.negateEndOffsetRatio = negateEndOffsetRatio;
    }
    
    public PhaseRequest(String beginPhase,
                        double beginOffestRatio,
                        TimeInterval beginOffsetMinimum,
                        boolean negateBeginOffsetRatio,
                        String endPhase,
                        TimeInterval endOffset,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = null;
        this.beginOffsetRatio = beginOffestRatio;
        this.beginOffsetRatioMinimum = beginOffsetMinimum;
        this.negateBeginOffsetRatio = negateBeginOffsetRatio;
        this.endOffset = endOffset;
    }
    
    public PhaseRequest(String beginPhase,
                        double beginOffestRatio,
                        TimeInterval beginOffsetMinimum,
                        boolean negateBeginOffsetRatio,
                        String endPhase,
                        double endOffestRatio,
                        TimeInterval endOffsetMinimum,
                        boolean negateEndOffsetRatio,
                        String model) throws TauModelException {
        this(beginPhase, endPhase, model);
        this.beginOffset = null;
        this.beginOffsetRatio = beginOffestRatio;
        this.beginOffsetRatioMinimum = beginOffsetMinimum;
        this.negateBeginOffsetRatio = negateBeginOffsetRatio;
        this.endOffset = null;
        this.endOffsetRatio = endOffestRatio;
        this.endOffsetRatioMinimum = endOffsetMinimum;
        this.negateEndOffsetRatio = negateEndOffsetRatio;
    }

    public RequestFilter generateRequest(EventAccessOperations event,
                                         Channel channel) throws Exception {
        Origin origin = EventUtil.extractOrigin(event);

        synchronized(this) {
            if(prevRequestFilter != null
                    && LocationUtil.areEqual(origin.getLocation(), prevOriginLoc)
                    && LocationUtil.areEqual(channel.getSite().getLocation(), prevSiteLoc)) {
                // don't need to do any work
                return new RequestFilter(channel.get_id(),
                                         prevRequestFilter.start_time,
                                         prevRequestFilter.end_time);
            }
        }
        double begin = getArrivalTime(beginPhase, channel, origin);
        double end = getArrivalTime(endPhase, channel, origin);
        if(begin == -1 || end == -1) {
            // no arrivals found, return zero length request filters
            return null;
        }
        MicroSecondDate originDate = new MicroSecondDate(origin.getOriginTime());
        TimeInterval bInterval = new TimeInterval(begin, UnitImpl.SECOND);
        TimeInterval eInterval = new TimeInterval(end, UnitImpl.SECOND);
        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        if(beginOffset != null) {
            bInterval = beginOffset;
        } else {
            bInterval = getTimeIntervalFromRatio(bDate,
                                                 eDate,
                                                 beginOffsetRatio,
                                                 beginOffsetRatioMinimum,
                                                 negateBeginOffsetRatio);
        }
        if(endOffset != null) {
            eInterval = endOffset;
        } else {
            eInterval = getTimeIntervalFromRatio(bDate,
                                                 eDate,
                                                 endOffsetRatio,
                                                 endOffsetRatioMinimum,
                                                 negateEndOffsetRatio);
        }
        bDate = bDate.add(bInterval);
        eDate = eDate.add(eInterval);
        synchronized(this) {
            prevOriginLoc = origin.getLocation();
            prevSiteLoc = channel.getSite().getLocation();
            prevRequestFilter = new RequestFilter(channel.get_id(),
                                                  bDate.getFissuresTime(),
                                                  eDate.getFissuresTime());
        }
        logger.debug("Generated request from "
                + bDate
                + " to "
                + eDate
                + " for "
                + StationIdUtil.toStringNoDates(channel.getSite().getStation().get_id()));
        return prevRequestFilter;
    }

    private double getArrivalTime(String phase, Channel chan, Origin origin)
            throws TauModelException {
        if(phase.equals(ORIGIN)) {
            return 0;
        }
        String[] phases = {phase};
        List<Arrival> arrivals = util.calcTravelTimes(chan.getSite().getLocation(),
                                                  origin,
                                                  phases);
        if(arrivals.size() == 0) {
            return -1;
        }
        // round to milliseconds
        return Math.rint(1000 * arrivals.get(0).getTime()) / 1000;
    }

    public static TimeInterval getTimeIntervalFromRatio(MicroSecondDate startPhaseTime,
                                                        MicroSecondDate endPhaseTime,
                                                        double ratio,
                                                        TimeInterval minimumTime,
                                                        boolean negate) {
        TimeInterval interval = new TimeInterval(endPhaseTime.difference(startPhaseTime)
                .multiplyBy(ratio));
        if(interval.lessThan(minimumTime)) {
            return negateIfTrue(minimumTime, negate);
        }
        return negateIfTrue(interval, negate);
    }

    public static TimeInterval negateIfTrue(TimeInterval interval,
                                            boolean negate) {
        if(negate) {
            double value = interval.getValue();
            return new TimeInterval(-value, interval.getUnit());
        }
        return interval;
    }

    private String beginPhase, endPhase;

    private TimeInterval beginOffset, endOffset;

    private double beginOffsetRatio, endOffsetRatio;

    private TimeInterval beginOffsetRatioMinimum, endOffsetRatioMinimum;

    private boolean negateBeginOffsetRatio = false,
            negateEndOffsetRatio = false;

    private TauPUtil util;

    private RequestFilter prevRequestFilter;

    private Location prevOriginLoc, prevSiteLoc;

    private static Logger logger = Logger.getLogger(PhaseRequest.class);

    private static final String ORIGIN = "origin";
}// PhaseRequest
