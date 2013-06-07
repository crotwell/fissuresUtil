package edu.sc.seis.fissuresUtil.cache;

public interface CorbaServerWrapper  {
    
    public String getFullName();

    public String getServerName();

    public String getServerDNS();

    public String getServerType();

    public void reset();

    public static final String NETDC_TYPE = "NetworkDC";

    public static final String NETACCESS_TYPE = "NetworkAccess";

    public static final String NETFINDER_TYPE = "NetworkFinder";

    public static final String EVENTDC_TYPE = "EventDC";

    public static final String EVENTFINDER_TYPE = "EventFinder";
    
    public static final String EVENTACCESS_TYPE = "EventAccess";

    public static final String SEISDC_TYPE = "SeismogramDC";

    public static final String PLOTTABLEDC_TYPE = "PlottableDC";
}
