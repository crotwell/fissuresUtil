package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;

/**
 * ChannelGrouper.java
 *
 *
 * Created: Thu Jun 27 08:51:26 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface ChannelGrouper {
    /**
     * Given a channel and an array of Channels, this method returns the ChannelGroup
     * as an array of Channels corresponding to channel.
     **/
    public Channel[] retrieve_grouping(org.omg.CORBA_2_3.ORB orb, ChannelId channelId);

    /**
     * Given a channelId and an array of ChannelIds, this method returns the ChannelGroup
     * as an array of ChannelIds corresponding to channelId.
     */
    public ChannelId[] retrieve_grouping( ChannelId[] channelIds, ChannelId channelId);
    // public Channel[] retrieve_grouping(NetworkDC networkDC, ChannelId channelId);
    //public Channel[] retrieve_grouping(Channel[] channels, ChannelId channelId);
}// ChannelGrouper
