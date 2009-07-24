package edu.sc.seis.fissuresUtil.cache;

import org.omg.CORBA.TRANSIENT;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.OrientationRange;
import edu.iris.Fissures.IfNetwork.SamplingRange;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.NetworkAttrImpl;

/**
 * A NSNetworkAccess allows for the NetworkAccess reference inside of it to go
 * stale by resetting the NetworkAccess to a fresh value from the netDC passed
 * in in its constructor. A NSNetworkDC is probably a good choice for the type
 * of netDC to pass in since it will also allow the NetworkDC itself to go stale
 * and be refreshed from the naming service.
 */
public class NSNetworkAccess extends ProxyNetworkAccess {

    /**
     * A ProxyNetworkAccess is not allowed as the NetworkAccess for this network
     * access since calling reset on this will reset the network access from the
     * netDC and the behaviour will change back to whatever is provided by the
     * NetworkAccess returned by the netDC.
     */
    public NSNetworkAccess(NetworkId id, VestingNetworkFinder vnf)
            throws NetworkNotFound {
        this(getAccess(id, vnf), id, vnf);
    }

    public NSNetworkAccess(SynchronizedNetworkAccess na,
                           NetworkId id,
                           VestingNetworkFinder vnf) {
        super(na);
        this.id = id;
        this.vnf = vnf;
    }

    /**
     * Refreshes the network from the network dc
     */
    public void reset() {
        try {
            super.reset();
            vnf.reset();
            setNetworkAccess( getAccess(id, vnf));
        } catch(NetworkNotFound e) {
            TRANSIENT t = new TRANSIENT("Unable to find the network to reset it");
            t.initCause(e);
            throw t;
        }
    }

    private static SynchronizedNetworkAccess getAccess(NetworkId id,
                                                       VestingNetworkFinder vnf)
            throws NetworkNotFound {
        return new SynchronizedNetworkAccess(((ProxyNetworkAccess)vnf.retrieve_by_id(id)).getCorbaObject());
    }
    
    protected NetworkAttrImpl setSource(NetworkAttr attr) {
        NetworkAttrImpl impl = (NetworkAttrImpl)attr;
        impl.setSourceServerDNS(getServerDNS());
        impl.setSourceServerName(getServerName());
        return impl;
    }

    @Override
    public NetworkAttr get_attributes() {
        return setSource(super.get_attributes());
    }

    @Override
    public Channel[] locate_channels(Area the_area,
                                     SamplingRange sampling,
                                     OrientationRange orientation) {
        Channel[] chans = super.locate_channels(the_area, sampling, orientation);
        for(int i = 0; i < chans.length; i++) {
            setSource(chans[i].getSite().getStation().getNetworkAttr());
        }
        return chans;
    }

    @Override
    public Channel retrieve_channel(ChannelId id) throws ChannelNotFound {
        Channel chan =  super.retrieve_channel(id);
        setSource(chan.getSite().getStation().getNetworkAttr());
        return chan;
    }

    @Override
    public Channel[] retrieve_channels_by_code(String station_code,
                                               String site_code,
                                               String channel_code)
            throws ChannelNotFound {
        Channel[] chans =  super.retrieve_channels_by_code(station_code, site_code, channel_code);
        for(int i = 0; i < chans.length; i++) {
            setSource(chans[i].getSite().getStation().getNetworkAttr());
        }
        return chans;
    }

    @Override
    public Channel[] retrieve_for_station(StationId p1) {
        Channel[] chans =  super.retrieve_for_station(p1);
        for(int i = 0; i < chans.length; i++) {
            setSource(chans[i].getSite().getStation().getNetworkAttr());
        }
        return chans;
    }

    @Override
    public Station[] retrieve_stations() {
        Station[] sta = super.retrieve_stations();
        for(int i = 0; i < sta.length; i++) {
            setSource(sta[i].getNetworkAttr());
        }
        return sta;
    }

    private VestingNetworkFinder vnf;

    private NetworkId id;

    public String getServerDNS() {
        return vnf.getServerDNS();
    }

    public String getServerName() {
        return vnf.getServerName();
    }
}