package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.*;

import edu.sc.seis.fissuresUtil.cache.BulletproofNetworkAccess;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

/**
 * ChannelProxy.java
 *
 *
 * Created: Thu Jun 27 09:01:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ChannelProxy implements ChannelGrouper{
    public  Channel[] retrieve_grouping(org.omg.CORBA_2_3.ORB orb, ChannelId channelId) {
        Channel[] group;
        try {
            FissuresNamingService fissuresNamingService = new FissuresNamingService(orb);
            NetworkDCOperations[] netRefs = fissuresNamingService.getAllNetworkDC();
            // ChannelId channelId = channel.get_id();
            for(int i = 0; i < netRefs.length; i++) {
                NetworkFinder finder = netRefs[i].a_finder();
                try {
                    NetworkId netId = channelId.network_id;
                    NetworkAccess net = finder.retrieve_by_id(netId);
                    net = new BulletproofNetworkAccess(net, netRefs[i], netId);
                    Channel channel = net.retrieve_channel(channelId);
                    StationId staId = channel.my_site.my_station.get_id();
                    Channel[] channels = net.retrieve_for_station(staId);
                    ChannelGrouperImpl channelGrouperImpl = new ChannelGrouperImpl();
                    group = channelGrouperImpl.retrieve_grouping(channels, channel);
                    if(group.length == 3) return group;
                } catch(Throwable e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return new Channel[0];
        }
        return null;
    }


    public Channel[] retrieve_grouping(edu.sc.seis.fissuresUtil.xml.DataSet dataSet, ChannelId channelId) {

        return null;
        //dataSet

    }


    public ChannelId[] retrieve_grouping(ChannelId[] channelIds, ChannelId channelId) {
        return retrieve_grouping(channelIds, channelId);
    }


}// ChannelProxy
