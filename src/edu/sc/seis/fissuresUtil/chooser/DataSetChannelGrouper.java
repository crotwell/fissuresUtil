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
        logger.debug("looking for "+chanGroupString);
        for (int i=0; i<paramNames.length; i++) {
            if (paramNames[i].startsWith(chanGroupString)) {
                logger.debug("found match parameter channelId "+paramNames[i]);
                list.add(((edu.iris.Fissures.IfNetwork.Channel)dataset.getParameter(paramNames[i])).get_id());
            } else {
                logger.debug("no found match parameter channelId "+paramNames[i]);
            }
        } // end of for (int i=0; i<paramNames[i]; i++)
        ChannelId[] channelIds = (ChannelId[])list.toArray(new ChannelId[0]);
        
        ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
        ChannelId[] channelGroup =
            channelProxy.retrieve_grouping(channelIds, channelID);
        return channelGroup;
    }
    
    /** Trys to find the 2 other seismograms that match the given dataset seismogram.
     */
    public static DataSetSeismogram[] retrieveSeismogramGrouping(DataSet dataset,
                                                                 DataSetSeismogram dss) {
        ChannelId channelId = dss.getRequestFilter().channel_id;
        ChannelId[] channelGroup =
            DataSetChannelGrouper.retrieveGrouping(dataset,
                                                   channelId);
        System.out.println("Channel Group has "+channelGroup.length);
        DataSetSeismogram[] chGrpSeismograms = new DataSetSeismogram[3];
        String[] allSeisNames = dataset.getDataSetSeismogramNames();
        for(int counter = 0; counter < channelGroup.length; counter++) {
            for (int i=0; i < allSeisNames.length; i++) {
                DataSetSeismogram allSeis =
                    dataset.getDataSetSeismogram(allSeisNames[i]);
                System.out.println("Checking "+ChannelIdUtil.toStringNoDates(allSeis.getRequestFilter().channel_id));
                if (ChannelIdUtil.areEqual(channelGroup[counter], allSeis.getRequestFilter().channel_id) &&
                    dss.getBeginTime().date_time.equals(allSeis.getBeginTime().date_time) &&
                    dss.getEndTime().date_time.equals(allSeis.getEndTime().date_time)) {
                    // found a match
                    chGrpSeismograms[counter] = allSeis;
                    break;
                }
            }
        }
        return chGrpSeismograms;
    }
    
    static Category logger =
        Category.getInstance(DataSetChannelGrouper.class.getName());
    
}// DataSetChannelGrouper
