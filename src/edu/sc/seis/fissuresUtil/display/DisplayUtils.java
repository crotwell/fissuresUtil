package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
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
    public static String[] getSeismogramNames(ChannelId channelId, DataSet dataset, TimeRange timeRange) {
	Channel channel = ((XMLDataSet)dataset).getChannel(channelId);
	SeismogramAttr[] attrs = ((XMLDataSet)dataset).getSeismogramAttrs();
	MicroSecondDate startDate = new MicroSecondDate(timeRange.start_time);
	MicroSecondDate endDate = new MicroSecondDate(timeRange.end_time);
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < attrs.length; counter++) {
	    if(ChannelIdUtil.toString(channelId).equals(ChannelIdUtil.toString(((SeismogramAttrImpl)attrs[counter]).getChannelID()))){
		if(((((SeismogramAttrImpl)attrs[counter]).getBeginTime().equals(startDate) ||
		    ((SeismogramAttrImpl)attrs[counter]).getBeginTime().before(startDate))) &&
		   (((SeismogramAttrImpl)attrs[counter]).getEndTime().equals(endDate) ||
		    ((SeismogramAttrImpl)attrs[counter]).getEndTime().after(endDate))){
			arrayList.add(((SeismogramAttrImpl)attrs[counter]).getName());
		    //return ((SeismogramAttrImpl)attrs[counter]).getName();
		}
	    }
	}
	String[] rtnValues = new String[arrayList.size()];
	rtnValues = (String[]) arrayList.toArray(rtnValues);														
	return rtnValues;
    }    
	
	public static LocalSeismogram[] getSeismogram(ChannelId channelId, DataSet dataset, TimeRange timeRange) {
		String[] seisNames = DisplayUtils.getSeismogramNames(channelId, dataset, timeRange);
		LocalSeismogram[] localSeismograms = new LocalSeismogram[seisNames.length];
		for(int counter = 0 ; counter < seisNames.length; counter++) {
				localSeismograms[counter] = ((XMLDataSet)dataset).getSeismogram(seisNames[counter]);
		}
		return localSeismograms;
    }
	
	 public static String getSeismogramName(ChannelId channelId, DataSet dataset, TimeRange timeRange) {
        Channel channel = ((XMLDataSet)dataset).getChannel(channelId);
        SeismogramAttr[] attrs = ((XMLDataSet)dataset).getSeismogramAttrs();
        MicroSecondDate startDate = new MicroSecondDate(timeRange.start_time);
        MicroSecondDate endDate = new MicroSecondDate(timeRange.end_time);
        for(int counter = 0; counter < attrs.length; counter++) {
            if(ChannelIdUtil.toString(channelId).equals(ChannelIdUtil.toString(((SeismogramAttrImpl)attrs[counter]).getChannelID()))){
                if(((((SeismogramAttrImpl)attrs[counter]).getBeginTime().equals(startDate) ||
		    ((SeismogramAttrImpl)attrs[counter]).getBeginTime().before(startDate))) &&
		   (((SeismogramAttrImpl)attrs[counter]).getEndTime().equals(endDate) ||
		    ((SeismogramAttrImpl)attrs[counter]).getEndTime().after(endDate))){
                    return ((SeismogramAttrImpl)attrs[counter]).getName();
                }
            }
        }
        return null;
    }    

}// DisplayUtils
