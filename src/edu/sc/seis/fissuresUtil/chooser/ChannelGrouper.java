package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.*;
import org.omg.CORBA.*;

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
    public Channel[] retrieve_grouping(org.omg.CORBA_2_3.ORB orb, ChannelId channelId);
    // public Channel[] retrieve_grouping(NetworkDC networkDC, ChannelId channelId);
    //public Channel[] retrieve_grouping(Channel[] channels, ChannelId channelId);
}// ChannelGrouper
