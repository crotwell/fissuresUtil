package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.seismogramDC.*;

/**
 * DisplayUtils.java
 *
 *
 * Created: Thu Jul 18 09:29:21 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class DisplayUtils {
    public static String getSeismogramName(ChannelId channelId, DataSet dataset, TimeRange timeRange) {
	Channel channel = ((XMLDataSet)dataset).getChannel(channelId);
	SeismogramAttr[] attrs = ((XMLDataSet)dataset).getSeismogramAttrs();
	MicroSecondDate startDate = new MicroSecondDate(timeRange.start_time);
	MicroSecondDate endDate = new MicroSecondDate(timeRange.end_time);
	for(int counter = 0; counter < attrs.length; counter++) {
	    if(ChannelIdUtil.toString(channelId).equals(ChannelIdUtil.toString(((SeismogramAttrImpl)attrs[counter]).getChannelID()))){
		if((((SeismogramAttrImpl)attrs[counter]).getBeginTime().equals(startDate) ||
		    ((SeismogramAttrImpl)attrs[counter]).getBeginTime().before(startDate))){
		    return ((SeismogramAttrImpl)attrs[counter]).getName();
		}
	    }
	}
	return null;
    }    
}// DisplayUtils
