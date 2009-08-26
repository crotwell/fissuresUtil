package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class VestingEventDC extends ProxyEventDC {

    public VestingEventDC(String serverName,
                          String serverDNS,
                          FissuresNamingService fns,
                          int numRetries) {
        this(serverName, serverDNS, fns, new ClassicRetryStrategy(numRetries));
    }

    public VestingEventDC(String serverName,
                          String serverDNS,
                          FissuresNamingService fns,
                          RetryStrategy strat) {
        this(new RetryEventDC(new NSEventDC(serverDNS, serverName, fns),
                              strat), strat);
    }

    public VestingEventDC(ProxyEventDC dc, RetryStrategy strat) {
        setEventDC(dc);
        this.pdc = dc;
        this.strat = strat;
    }

    public EventChannelFinder a_channel_finder() {
        return getEventDC().a_channel_finder();
    }

    public EventFinder a_finder() {
        return new RetryEventFinder(pdc, strat);
    }

    private ProxyEventDC pdc;

    private RetryStrategy strat;
}
