package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.chooser.DataSetChannelGrouper;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    
    public static UnitRangeImpl getShaledRange(UnitRangeImpl ampRange, double shift, double scale){
        if(shift == 0 && scale == 1.0){
            return ampRange;
        }
        double range = ampRange.getMaxValue() - ampRange.getMinValue();
        double minValue = ampRange.getMinValue() + range * shift;
        return new UnitRangeImpl(minValue, minValue + range * scale, ampRange.getUnit());
    }
    
    public static final int[] getSeisPoints(LocalSeismogramImpl seis,
                                            MicroSecondTimeRange time){
        long seisBegin = seis.getBeginTime().getMicroSecondTime();
        long seisEnd = seis.getEndTime().getMicroSecondTime();
        int numValues = seis.getNumPoints();
        int[] values = new int[2];
        values[0] =
            (int)Math.floor(linearInterp(seisBegin,
                                         seisEnd,
                                         numValues,
                                         time.getBeginTime().getMicroSecondTime()));
        values[1] =
            (int)Math.ceil(linearInterp(seisBegin,
                                        seisEnd,
                                        numValues,
                                        time.getEndTime().getMicroSecondTime()));
        return values;
    }
    
    public static String[] getSeismogramNames(DataSetSeismogram[] dss){
        String[] names = new String[dss.length];
        for(int i = 0; i < dss.length; i++){
            names[i] = "" + dss[i];
        }
        return names;
    }
    
    public static String getOrientationName(String orientation) {
        
        char ch = orientation.charAt(2);
        if(ch == 'E' || ch == '1' || ch == 'U') return "East";
        else if(ch == 'N' || ch == '2' || ch == 'V') return "North";
        else return "Up";
    }
    
    /**
     * <code>getComponents</code> sorts the passed in seismograms in by their east-west, north-south or z
     * component and finds all available components in their data sets for each component
     * @param dss the seismograms to be componentized
     @return an array sorted by component orientation.  [0][] contains north, [1][] contains east and [2][] contains z
     */
    public static DataSetSeismogram[][] getComponents(DataSetSeismogram[] dss){
        return getComponents(dss, "");
    }
    
    /**
     * <code>getComponents</code> performs the same operation as getComponents, but also adds a suffix to each created
     * datasetseismogram
     * @param suffix the string to be appended
     */
    public static DataSetSeismogram[][] getComponents(DataSetSeismogram[] dss, String suffix){
        List names = new ArrayList();
        List north = new ArrayList();
        List east = new ArrayList();
        List z = new ArrayList();
        for(int i = 0; i < dss.length; i++){
            if(!names.contains(dss[i].getSeismogram().getName())){
                LocalSeismogramImpl seis = dss[i].getSeismogram();
                XMLDataSet dataSet = (XMLDataSet)dss[i].getDataSet();
                ChannelId[] channelGroup = DataSetChannelGrouper.retrieveGrouping(dataSet, seis.getChannelID());
                for(int counter = 0; counter < channelGroup.length; counter++) {
                    LocalSeismogram[] newSeismograms  = DisplayUtils.getSeismogram(channelGroup[counter], dataSet,
                                                                                   new TimeRange(seis.getBeginTime().getFissuresTime(),
                                                                                                 seis.getEndTime().getFissuresTime()));
                    for(int j = 0; j < newSeismograms.length; j++){
                        DataSetSeismogram current = new DataSetSeismogram((LocalSeismogramImpl)newSeismograms[j],
                                                                          dataSet,
                                                                              ((LocalSeismogramImpl)newSeismograms[i]).getName()+ suffix);
                        if(DisplayUtils.getOrientationName(channelGroup[counter].channel_code).equals("North")){
                            north.add(current);
                        }else if(DisplayUtils.getOrientationName(channelGroup[counter].channel_code).equals("East")){
                            east.add(current);
                        }else{
                            z.add(current);
                        }
                        names.add(current.getSeismogram().getName());
                    }
                }
            }
        }
        DataSetSeismogram[][] sortedSeismos = new DataSetSeismogram[3][];
        sortedSeismos[0] = ((DataSetSeismogram[])north.toArray(new DataSetSeismogram[north.size()]));
        sortedSeismos[1] = ((DataSetSeismogram[])east.toArray(new DataSetSeismogram[east.size()]));
        sortedSeismos[2] = ((DataSetSeismogram[])z.toArray(new DataSetSeismogram[z.size()]));
        return sortedSeismos;
    }
    
    public static DataSetSeismogram[] addSuffix(DataSetSeismogram[] dss, String suffix){
        DataSetSeismogram[] suffixedDss = new DataSetSeismogram[dss.length];
        for(int i = 0; i < dss.length; i++){
            suffixedDss[i] = new DataSetSeismogram(dss[i], dss[i].toString() + suffix);
        }
        return suffixedDss;
    }
    
    private static final double linearInterp(long xa, long xb, int y,
                                             long x) {
        if (x == xa) return 0;
        if (x == xb) return y;
        return y*(x-xa)/(double)(xb-xa);
    }
    
    public static final UnitRangeImpl ZERO_RANGE = new UnitRangeImpl(0, 0, UnitImpl.COUNT);
    
    
    public static final Map statCache = new HashMap();
}// DisplayUtils
