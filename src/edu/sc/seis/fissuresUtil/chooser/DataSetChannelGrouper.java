package edu.sc.seis.fissuresUtil.chooser;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.seismogramDC.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * DataSetChannelGrouper.java
 *
 *
 * Created: Fri Sep 13 13:57:25 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class DataSetChannelGrouper {
    public DataSetChannelGrouper (){

    }

    public static ChannelId[] retrieveGrouping(DataSet dataset,
                      ChannelId channelID) {
    String[] paramNames = dataset.getParameterNames();
    LinkedList list = new LinkedList();
    String origChannelIdStr = ChannelIdUtil.toString(channelID);
    origChannelIdStr = origChannelIdStr.substring(0, origChannelIdStr.indexOf(channelID.channel_code)+2);
    String chanGroupString = edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames.CHANNEL +
        origChannelIdStr;
    for (int i=0; i<paramNames.length; i++) {
        if (paramNames[i].startsWith(chanGroupString)) {
        logger.debug("found match parameter channelId "+paramNames[i]);
        list.add(((edu.iris.Fissures.IfNetwork.Channel)dataset.getParameter(paramNames[i])).get_id());
        } // end of if (paramNames[i].startsWith(origChannelIdStr))

    } // end of for (int i=0; i<paramNames[i]; i++)
    ChannelId[] channelIds = (ChannelId[])list.toArray(new ChannelId[0]);

    ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
    ChannelId[] channelGroup =
        channelProxy.retrieve_grouping(channelIds, channelID);
    return channelGroup;
    }

    static Category logger =
    Category.getInstance(DataSetChannelGrouper.class.getName());

}// DataSetChannelGrouper
