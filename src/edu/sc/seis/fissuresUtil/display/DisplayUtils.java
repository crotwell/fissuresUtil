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
	rtnValues = (String[]) arrayList.toArray(rtnValues);												return rtnValues;
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


    public static UnitRangeImpl getShaledRange(UnitRangeImpl ampRange, double shift, double scale){
	if(shift == 0 && scale == 1.0){
	    return ampRange;
	}
	double range = ampRange.getMaxValue() - ampRange.getMinValue();
	double minValue = ampRange.getMinValue() + range * shift;
	return new UnitRangeImpl(minValue, minValue + range * scale, ampRange.getUnit());
    }

    public static final int[] getSeisPoints(LocalSeismogramImpl seis, MicroSecondTimeRange time){
	long seisBegin = seis.getBeginTime().getMicroSecondTime();
	long seisEnd = seis.getEndTime().getMicroSecondTime();
	int numValues = seis.getNumPoints();
	int[] values = new int[2];
	values[0] = linearInterp(seisBegin,
				 seisEnd,
				 numValues, 
				 time.getBeginTime().getMicroSecondTime());
        values[1] = linearInterp(seisBegin,
				 seisEnd,
				 numValues, 
				 time.getEndTime().getMicroSecondTime());
	return values;
    }

    private static final int linearInterp(long xa, long xb, int y,
					  long x) {
        if (x == xa) return 0;
        if (x == xb) return y;
        double result = y*(x-xa)/(double)(xb-xa);
	if(result < 0){
	    return 0;
	}
	if(result >= y){
	    return y - 1;
	}
	return (int)Math.round(result);
    }
    
    public static final UnitRangeImpl ZERO_RANGE = new UnitRangeImpl(0, 0, UnitImpl.COUNT);

    public static final Map statCache = new HashMap();
}// DisplayUtils
