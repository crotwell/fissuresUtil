package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.util.ArrayList;
import java.util.List;
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
        SeismogramAttr[] attrs = ((XMLDataSet)dataset).getSeismogramAttrs();
        MicroSecondDate startDate = new MicroSecondDate(timeRange.start_time);
        MicroSecondDate endDate = new MicroSecondDate(timeRange.end_time);
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < attrs.length; counter++) {
            SeismogramAttrImpl atrib = (SeismogramAttrImpl)attrs[counter];
            if(ChannelIdUtil.areEqual(channelId,atrib.getChannelID())){
                if((atrib.getBeginTime().equals(startDate) ||
                    atrib.getBeginTime().before(startDate)) &&
                       (atrib.getEndTime().equals(endDate) ||
                        atrib.getEndTime().after(endDate))){
                    arrayList.add(atrib.getName());

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

    public static DataSetSeismogram[] getComponents(DataSetSeismogram seismogram){
        List componentSeismograms = new ArrayList();
        RequestFilter rf = seismogram.getRequestFilter();
        MicroSecondDate startDate = new MicroSecondDate(rf.start_time);
        MicroSecondDate endDate = new MicroSecondDate(rf.end_time);
        ChannelId chanId = rf.channel_id;
        DataSet dataSet = seismogram.getDataSet();
        String[] names = dataSet.getDataSetSeismogramNames();
        for (int i = 0; i < names.length; i++ ) {
            System.out.println("Attempting to match on " + names[i]);
            DataSetSeismogram currentSeis = dataSet.getDataSetSeismogram(names[i]);
            RequestFilter currentRF = currentSeis.getRequestFilter();
            MicroSecondDate currentBegin = new MicroSecondDate(currentRF.start_time);
            MicroSecondDate currentEnd = new MicroSecondDate(currentRF.end_time);
            System.out.println("ID: " + ChannelIdUtil.toString(currentRF.channel_id) +
                               "\nSITE CODE: " + currentRF.channel_id.site_code + " NETWORK ID: " +
                               currentRF.channel_id.network_id + " CHANNEL CODE: " + currentRF.channel_id.channel_code +
                               " STATION CODE: " + currentRF.channel_id.station_code);
            if(areFriends(chanId,currentRF.channel_id)){
                System.out.println("the channel ids are equal");
                if((currentBegin.equals(startDate) ||
                    currentBegin.before(startDate)) &&
                       (currentEnd.equals(endDate) ||
                        currentBegin.after(endDate))){
                    System.out.println("Found matching component");
                    componentSeismograms.add(currentSeis);

                }
            }
        }
        DataSetSeismogram[] components = new DataSetSeismogram[componentSeismograms.size()];
        componentSeismograms.toArray(components);
        return components;
    }

    public static boolean areFriends(ChannelId a, ChannelId b) {
        MicroSecondDate aBeginMSD = new MicroSecondDate(a.begin_time);
        MicroSecondDate bBeginMSD = new MicroSecondDate(b.begin_time);
        return NetworkIdUtil.areEqual(a.network_id, b.network_id) &&
            a.station_code.equals(b.station_code) &&
            a.site_code.equals(b.site_code) &&
            aBeginMSD.equals(bBeginMSD);
    }

    public static String getSeismogramName(ChannelId channelId, DataSet dataset, TimeRange timeRange) {
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

    /** Calculates the indexes within the seismogram data points,
     correspoding to the begin and end time of the given range.
     The amplitude of the
     seismogram is not important for this calculation.
     */
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

    public static String getOrientationName(DataSetSeismogram dss){
        return getOrientationName(dss.getRequestFilter().channel_id.channel_code);
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
        List names = new ArrayList();
        List north = new ArrayList();
        List east = new ArrayList();
        List z = new ArrayList();
        for(int i = 0; i < dss.length; i++){
            if(!names.contains(dss[i].getName())){
                DataSetSeismogram[] newSeismograms = DisplayUtils.getComponents(dss[i]);
                for(int j = 0; j < newSeismograms.length; j++){
                    DataSetSeismogram current = newSeismograms[i];
                    if(DisplayUtils.getOrientationName(current).equals("North")){
                        north.add(current);
                    }else if(DisplayUtils.getOrientationName(current).equals("East")){
                        east.add(current);
                    }else{
                        z.add(current);
                    }
                    names.add(current.getName());
                }
            }
        }

        DataSetSeismogram[][] sortedSeismos = new DataSetSeismogram[3][];
        sortedSeismos[0] = ((DataSetSeismogram[])north.toArray(new DataSetSeismogram[north.size()]));
        sortedSeismos[1] = ((DataSetSeismogram[])east.toArray(new DataSetSeismogram[east.size()]));
        sortedSeismos[2] = ((DataSetSeismogram[])z.toArray(new DataSetSeismogram[z.size()]));
        return sortedSeismos;
    }

    public static boolean allNull(Object[] array){
        boolean allNull = true;
        for (int i = 0; i < array.length && allNull; i++ ) {
            if(array[i] != null){
                allNull = false;
            }
        }
        return allNull;
    }

    private static final double linearInterp(long xa, long xb, int y,
                                             long x) {
        if (x == xa) return 0;
        if (x == xb) return y;
        return y*(x-xa)/(double)(xb-xa);
    }

    public static final UnitRangeImpl ZERO_RANGE = new UnitRangeImpl(0, 0, UnitImpl.COUNT);

    public static final UnitRangeImpl ONE_RANGE = new UnitRangeImpl(1, 1, UnitImpl.COUNT);

    public static final MicroSecondTimeRange ONE_TIME = new MicroSecondTimeRange(new MicroSecondDate(), new MicroSecondDate(1));

}// DisplayUtils
