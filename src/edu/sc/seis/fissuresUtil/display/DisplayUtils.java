package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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

    public static DataSetSeismogram[] getComponents(DataSetSeismogram seismogram){
        List componentSeismograms = new ArrayList();
        RequestFilter rf = seismogram.getRequestFilter();
        MicroSecondDate startDate = new MicroSecondDate(rf.start_time);
        MicroSecondDate endDate = new MicroSecondDate(rf.end_time);
        ChannelId chanId = rf.channel_id;
        DataSet dataSet = seismogram.getDataSet();
        String[] names = dataSet.getDataSetSeismogramNames();
        for (int i = 0; i < names.length; i++ ) {
            DataSetSeismogram currentSeis = dataSet.getDataSetSeismogram(names[i]);
            RequestFilter currentRF = currentSeis.getRequestFilter();
            MicroSecondDate currentBegin = new MicroSecondDate(currentRF.start_time);
            MicroSecondDate currentEnd = new MicroSecondDate(currentRF.end_time);
            if(areFriends(chanId,currentRF.channel_id)){
                if((currentBegin.equals(startDate) ||
                        currentBegin.before(startDate)) &&
                       (currentEnd.equals(endDate) ||
                            currentBegin.after(endDate))){
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
        //System.out.println("SeisBegin " + seisBegin + " TimeBegin: " + time.getBeginTime().getMicroSecondTime() + " Seis end: " + seisEnd + " Time end: " + time.getEndTime().getMicroSecondTime());
        values[0] =
            (int)(linearInterp(seisBegin,
                               seisEnd,
                               numValues,
                               time.getBeginTime().getMicroSecondTime()));
        values[1] =
            (int)(linearInterp(seisBegin,
                               seisEnd,
                               numValues,
                               time.getEndTime().getMicroSecondTime()));
        return values;
    }

    /** Calculates the indexes within the seismogram data points,
     correspoding to the begin and end time of the given range.
     The amplitude of the
     seismogram is not important for this calculation.
     */
    public static final int[] getPoints(SeismogramIterator it,
                                        MicroSecondTimeRange time){
        long seisBegin= it.getSeisTime().getBeginTime().getMicroSecondTime();
        long seisEnd = it.getSeisTime().getEndTime().getMicroSecondTime();
        int numValues = it.getNumPoints();
        int[] values = new int[2];
        //System.out.println("SeisBegin " + seisBegin + " TimeBegin: " + time.getBeginTime().getMicroSecondTime() + " Seis end: " + seisEnd + " Time end: " + time.getEndTime().getMicroSecondTime());
        values[0] =
            (int)(linearInterp(seisBegin,
                               seisEnd,
                               numValues,
                               time.getBeginTime().getMicroSecondTime()));
        values[1] =
            (int)(linearInterp(seisBegin,
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

    /**
     * Sorts the passed array of seismograms by begin time. If a seismogram is
     * completely enveloped by another seismogram in terms of time, it is not
     * returned
     *
     * @returns the seismograms in order of begin time
     */
    public static LocalSeismogramImpl[] sortByDate(LocalSeismogramImpl[] seis){
        List sortedSeis = new ArrayList();
        for(int i = 0; i < seis.length; i++){
            MicroSecondDate timeToBeAdded = seis[i].getBeginTime();
            ListIterator it = sortedSeis.listIterator();
            boolean added = false;
            while(it.hasNext()){
                LocalSeismogramImpl current = (LocalSeismogramImpl)it.next();
                MicroSecondDate currentTime = current.getBeginTime();
                if(timeToBeAdded.before(currentTime)){
                    it.previous();
                    it.add(seis[i]);
                    added = true;
                    break;
                }
            }
            if(!added){
                sortedSeis.add(seis[i]);
            }
        }
        LocalSeismogramImpl prev = null;
        ListIterator it = sortedSeis.listIterator();
        while(it.hasNext()){
            LocalSeismogramImpl cur = (LocalSeismogramImpl)it.next();
            if(prev != null && prev.getEndTime().after(cur.getEndTime())){
                it.remove();
            }else{
                prev = cur;
            }
        }
        return (LocalSeismogramImpl[])sortedSeis.toArray(new LocalSeismogramImpl[sortedSeis.size()]);
    }

    /**
     *@returns A time range encompassing the earliest begin time of the passed
     * in seismograms to the latest end time
     */
    public static MicroSecondTimeRange getFullTime(LocalSeismogramImpl[] seis){
        if(seis.length == 0){
            return ZERO_TIME;
        }
        MicroSecondDate beginTime = sortByDate(seis)[0].getBeginTime();
        MicroSecondDate endTime = new MicroSecondDate(0);
        for(int i = 0; i < seis.length; i++){
            if(seis[i].getEndTime().after(endTime)){
                endTime = seis[i].getEndTime();
            }
        }
        return new MicroSecondTimeRange(beginTime, endTime);
    }

    public static boolean areOverlapping(LocalSeismogramImpl one,
                                         LocalSeismogramImpl two){
        MicroSecondDate[] oneTimes = { one.getBeginTime(), one.getEndTime()};
        MicroSecondDate[] twoTimes = { two.getBeginTime(), two.getEndTime()};
        if((oneTimes[0].before(twoTimes[1]) && oneTimes[1].after(twoTimes[0]))||
               (twoTimes[0].before(oneTimes[1]) && twoTimes[1].after(oneTimes[0]))){
            return true;
        }
        return false;
    }

    public static boolean areContiguous(LocalSeismogramImpl one,
                                        LocalSeismogramImpl two){
        if(!areOverlapping(one, two)){
            if(one.getEndTime().before(two.getBeginTime())){
                return areContiguous(one.getEndTime(),
                                     two.getBeginTime(),
                                     one.getSampling().getPeriod());
            }else{
                return areContiguous(two.getEndTime(),
                                     one.getBeginTime(),
                                     one.getSampling().getPeriod());
            }
        }
        return false;
    }

    private static boolean areContiguous(MicroSecondDate one,
                                         MicroSecondDate two,
                                         TimeInterval interval){
        TimeInterval doubleInterval = (TimeInterval)interval.multiplyBy(2.0);
        if(one.add(doubleInterval).after(two)){
            return true;
        }
        return false;
    }

    public static String getOrientationName(DataSetSeismogram dss){
        return getOrientationName(dss.getRequestFilter().channel_id.channel_code);
    }

    public static String getOrientationName(String orientation) {

        char ch = orientation.charAt(2);
        if(ch == 'E' || ch == '1' || ch == 'U') return EAST;
        else if(ch == 'N' || ch == '2' || ch == 'V') return NORTH;
        else return UP;
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
                    DataSetSeismogram current = newSeismograms[j];
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
        for (int i = 0; i < array.length; i++ ) {
            if(array[i] != null){
                return false;
            }
        }
        return true;
    }

    public static final double linearInterp(long firstPoint, long lastPoint,
                                            int numValues, long currentPoint){
        return
            (currentPoint-firstPoint)/(double)(lastPoint-firstPoint)*(numValues-1);
    }

    public static final String UP = "Up";

    public static final String EAST = "East";

    public static final String NORTH = "North";

    public static final String NORTHEAST = NORTH + "-" + EAST;

    public static final String UPEAST = UP + "-" + EAST;

    public static final String UPNORTH = UP + "-" + NORTH;

    public static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);

    public static Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);

    public static final Font BORDER_FONT = new Font("Arial", Font.PLAIN, 10);

    public static Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);

    public static final Stroke ONE_PIXEL_STROKE = new BasicStroke(1);

    public static final Stroke TWO_PIXEL_STROKE = new BasicStroke(2);

    public static final Stroke THREE_PIXEL_STROKE = new BasicStroke(3);

    public static final UnitRangeImpl ZERO_RANGE = new UnitRangeImpl(0, 0, UnitImpl.COUNT);

    public static final UnitRangeImpl ONE_RANGE = new UnitRangeImpl(-1, 1, UnitImpl.COUNT);

    public static final MicroSecondTimeRange ZERO_TIME = new MicroSecondTimeRange(new MicroSecondDate(0), new MicroSecondDate(0));

    public static final MicroSecondTimeRange ONE_TIME = new MicroSecondTimeRange(new MicroSecondDate(0), new MicroSecondDate(1));

}// DisplayUtils
