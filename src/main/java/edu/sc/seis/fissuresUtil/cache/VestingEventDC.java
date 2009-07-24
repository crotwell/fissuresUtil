package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class VestingEventDC extends ProxyEventDC {

    public VestingEventDC(String serverName,
                          String serverDNS,
                          FissuresNamingService fns,
                          int numRetries) {
        this(new RetryEventDC(new NSEventDC(serverDNS, serverName, fns),
                              numRetries,
                              new ClassicRetryStrategy()),
             numRetries,
             new ClassicRetryStrategy());
    }

    public VestingEventDC(String serverName,
                          String serverDNS,
                          FissuresNamingService fns,
                          int numRetries,
                          RetryStrategy strat) {
        this(new RetryEventDC(new NSEventDC(serverDNS, serverName, fns),
                              numRetries,
                              strat), numRetries, strat);
    }

    public VestingEventDC(ProxyEventDC dc, int numRetries, RetryStrategy strat) {
        setEventDC(dc);
        this.pdc = dc;
        this.retries = numRetries;
        this.strat = strat;
    }

    public EventChannelFinder a_channel_finder() {
        return getEventDC().a_channel_finder();
    }

    public EventFinder a_finder() {
        return new RetryEventFinder(pdc, retries, strat);
    }

    private int retries;

    private ProxyEventDC pdc;

    private RetryStrategy strat;
}
