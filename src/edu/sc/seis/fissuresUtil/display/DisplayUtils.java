package edu.sc.seis.fissuresUtil.display;

import java.util.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableFilteredSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableIterator;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
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
        DataSet dataSet = seismogram.getDataSet();
        RequestFilter rf = seismogram.getRequestFilter();
        return getComponents(dataSet, rf);
    }

    public static DataSetSeismogram[] getComponents(DataSet dataSet, RequestFilter rf) {
        Set componentSeismograms = new HashSet();
        MicroSecondTimeRange tr = new MicroSecondTimeRange(rf);
        ChannelId chanId = rf.channel_id;
        String[] names = dataSet.getDataSetSeismogramNames();
        for (int i = 0; i < names.length; i++ ) {
            DataSetSeismogram currentSeis = dataSet.getDataSetSeismogram(names[i]);
            RequestFilter currentRF = currentSeis.getRequestFilter();
            MicroSecondTimeRange curTr = new MicroSecondTimeRange(currentRF);
            if(areFriends(chanId,currentRF.channel_id) && tr.equals(curTr)) {
                componentSeismograms.add(currentSeis);
            }
        }

        //If we didn't find three with the same channel id and begin and end
        //times, look for some with the same channel id and overlapping times
        // do this as a separate step in case there are exact matches AND overlaps
        // and we prefer exact match to overlapping
        if(componentSeismograms.size() < 3) {
            for (int i = 0; i < names.length; i++ ) {
                DataSetSeismogram currentSeis = dataSet.getDataSetSeismogram(names[i]);
                RequestFilter currentRF = currentSeis.getRequestFilter();
                MicroSecondTimeRange curTr = new MicroSecondTimeRange(currentRF);
                if(areFriends(chanId,currentRF.channel_id) &&
                   areOverlapping(tr, curTr))
                    componentSeismograms.add(currentSeis);
            }
        }
        DataSetSeismogram[] components = new DataSetSeismogram[componentSeismograms.size()];
        componentSeismograms.toArray(components);
        return components;
    }

    public static boolean areFriends(DataSetSeismogram seis, DataSetSeismogram otherSeis){
        return areFriends(seis.getRequestFilter().channel_id,
                          otherSeis.getRequestFilter().channel_id);
    }

    public static boolean areFriends(ChannelId a, ChannelId b) {
        MicroSecondDate aBeginMSD = new MicroSecondDate(a.begin_time);
        MicroSecondDate bBeginMSD = new MicroSecondDate(b.begin_time);
        return NetworkIdUtil.areEqual(a.network_id, b.network_id) &&
            a.station_code.equals(b.station_code) &&
            a.site_code.equals(b.site_code) &&
            a.channel_code.substring(0, 2).equals(b.channel_code.substring(0, 2)) &&
            aBeginMSD.equals(bBeginMSD);
    }

    public static void applyFilter(NamedFilter filter, DrawableIterator it){
        while(it.hasNext()){
            DrawableSeismogram seis =  (DrawableSeismogram)it.next();
            Iterator filterIt = seis.iterator(DrawableFilteredSeismogram.class);
            boolean found = false;
            DrawableFilteredSeismogram filterSeis = null;
            while(filterIt.hasNext() && !found){
                filterSeis = (DrawableFilteredSeismogram)filterIt.next();
                if(filterSeis.getFilter().equals(filter)){
                    found = true;
                }
            }
            if(!found && filter.getVisibility()){
                seis.add(new DrawableFilteredSeismogram(seis.getParent(),
                                                        seis.getSeismogram(),
                                                        filter));
            }else if(found && !filter.getVisibility()){
                seis.remove(filterSeis);
            }
        }
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
        Map seisTimes = new HashMap();
        for (int i = 0; i < seis.length; i++){
            seisTimes.put(seis[i], new MicroSecondTimeRange(seis[i].getBeginTime(),
                                                            seis[i].getEndTime()));
        }
        List seisList = sortByDate(seisTimes);
        seis = new LocalSeismogramImpl[seisList.size()];
        return (LocalSeismogramImpl[])seisList.toArray(seis);
    }

    public static RequestFilter[] sortByDate(RequestFilter[] rf){
        Map rfTimes = new HashMap();
        for (int i = 0; i < rf.length; i++){
            rfTimes.put(rf[i], new MicroSecondTimeRange(rf[i]));
        }
        List rfList = sortByDate(rfTimes);
        rf = new RequestFilter[rfList.size()];
        return (RequestFilter[])rfList.toArray(rf);
    }

    private static List sortByDate(Map objectTimes){
        List sortedObj = new ArrayList();
        Iterator objIt = objectTimes.keySet().iterator();
        while(objIt.hasNext()){
            Object currentObj = objIt.next();
            MicroSecondDate timeToBeAdded = ((MicroSecondTimeRange)objectTimes.get(currentObj)).getBeginTime();
            ListIterator it = sortedObj.listIterator();
            boolean added = false;
            while(it.hasNext()){
                Object current = it.next();
                MicroSecondDate currentTime = ((MicroSecondTimeRange)objectTimes.get(current)).getBeginTime();
                if(timeToBeAdded.before(currentTime)){
                    it.previous();
                    it.add(currentObj);
                    added = true;
                    break;
                }
            }
            if(!added){
                sortedObj.add(currentObj);
            }
        }
        MicroSecondTimeRange prev = null;
        ListIterator it = sortedObj.listIterator();
        while(it.hasNext()){
            MicroSecondTimeRange cur = (MicroSecondTimeRange)objectTimes.get(it.next());
            if(prev != null && prev.getEndTime().after(cur.getEndTime())){
                it.remove();
            }else{
                prev = cur;
            }
        }
        return sortedObj;
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
        MicroSecondTimeRange oneTr = new MicroSecondTimeRange(one.getBeginTime(),
                                                              one.getEndTime());
        MicroSecondTimeRange twoTr = new MicroSecondTimeRange(two.getBeginTime(),
                                                              two.getEndTime());
        return areOverlapping(oneTr, twoTr);
    }

    public static boolean areOverlapping(MicroSecondTimeRange one,
                                         MicroSecondTimeRange two){
        if((one.getBeginTime().before(two.getEndTime()) &&
                one.getEndTime().after(two.getBeginTime()))||
               (two.getBeginTime().before(one.getEndTime()) &&
                    two.getEndTime().after(one.getBeginTime()))){
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


    public static DataSetSeismogram[][] sortByComponents(DataSetSeismogram[] seismos){
        List north = new ArrayList();
        List east = new ArrayList();
        List z = new ArrayList();
        for (int i = 0; i < seismos.length; i++) {
            if(DisplayUtils.getOrientationName(seismos[i]).equals("North")){
                north.add(seismos[i]);
            }else if(DisplayUtils.getOrientationName(seismos[i]).equals("East")){
                east.add(seismos[i]);
            }else{
                z.add(seismos[i]);
            }
        }
        DataSetSeismogram[][] sortedSeismos = new DataSetSeismogram[3][];
        sortedSeismos[0] = ((DataSetSeismogram[])north.toArray(new DataSetSeismogram[north.size()]));
        sortedSeismos[1] = ((DataSetSeismogram[])east.toArray(new DataSetSeismogram[east.size()]));
        sortedSeismos[2] = ((DataSetSeismogram[])z.toArray(new DataSetSeismogram[z.size()]));
        return sortedSeismos;
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

    public static boolean inInsets(MouseEvent me){
        JComponent comp = (JComponent)me.getComponent();
        Insets insets = comp.getInsets();
        if( me.getX() < insets.left  ||
           me.getX() > comp.getSize().width - insets.right ||
           me.getY() < insets.top ||
           me.getY() > comp.getSize().height - insets.bottom){
            return true;
        }
        return false;
    }

    public static final double linearInterp(long firstPoint, long lastPoint,
                                            int numValues, long currentPoint){
        return
            (currentPoint-firstPoint)/(double)(lastPoint-firstPoint)*(numValues-1);
    }

    public static boolean originIsEqual(EventAccessOperations eventA, EventAccessOperations eventB)
        throws NoPreferredOrigin{
        Origin originA = eventA.get_preferred_origin();
        Origin originB = eventB.get_preferred_origin();

        if (!originA.get_id().equals(originB.get_id())) return false;
        if (!originA.origin_time.date_time.equals(originB.origin_time.date_time)) return false;
        if (originA.magnitudes[0].value != originB.magnitudes[0].value) return false;
        if (originA.my_location.latitude != originB.my_location.latitude) return false;
        if (originA.my_location.longitude != originB.my_location.longitude) return false;
        if (originA.my_location.depth.value != originB.my_location.depth.value) return false;

        return true;
    }

    public static DistAz calculateDistAz(DataSetSeismogram seis) {
        EventAccessOperations event = seis.getDataSet().getEvent();
        ChannelId chanId = seis.getRequestFilter().channel_id;
        Channel channel = seis.getDataSet().getChannel(chanId);
        if(channel != null && event != null){
            Site seisSite = channel.my_site;
            Location seisLoc =  seisSite.my_location;
            Location eventLoc = null;
            try{
                eventLoc = event.get_preferred_origin().my_location;
            }catch(NoPreferredOrigin e){//if no preferred origin, just use the first
                if (event.get_origins().length > 0) {
                    eventLoc = event.get_origins()[0].my_location;
                }
            }
            if (eventLoc == null) { return null; }
            DistAz distAz = new DistAz(seisLoc.latitude, seisLoc.longitude,
                                       eventLoc.latitude, eventLoc.longitude);
            return distAz;
        }
        return null;
    }

    public static QuantityImpl calculateBackAzimuth(DataSetSeismogram seis) {
        DistAz distAz = calculateDistAz(seis);
        if (distAz != null) {
            return new QuantityImpl(distAz.baz, UnitImpl.DEGREE);
        }
        return null;
    }

    public static QuantityImpl calculateAzimuth(DataSetSeismogram seis) {
        DistAz distAz = calculateDistAz(seis);
        if (distAz != null) {
            return new QuantityImpl(distAz.az, UnitImpl.DEGREE);
        }
        return null;
    }

    public static QuantityImpl calculateDistance(DataSetSeismogram seis){
        DistAz distAz = calculateDistAz(seis);
        if (distAz != null) {
            return new QuantityImpl(distAz.delta, UnitImpl.DEGREE);
        }
        return null;
    }
    public static QuantityImpl calculateDistance(Channel channel, EventAccessOperations event){
        if(channel != null && event != null){
            Site seisSite = channel.my_site;
            Location seisLoc =  seisSite.my_location;
            Location eventLoc = null;
            try{
                eventLoc = event.get_preferred_origin().my_location;
            }catch(NoPreferredOrigin e){//if no preferred origin, just use the first
                if (event.get_origins().length > 0) {
                    eventLoc = event.get_origins()[0].my_location;
                }
            }
            if (eventLoc == null) { return null; }
            DistAz distAz = new DistAz(seisLoc.latitude, seisLoc.longitude,
                                       eventLoc.latitude, eventLoc.longitude);
            return new QuantityImpl(distAz.delta, UnitImpl.DEGREE);
        }
        return null;
    }

    public static MicroSecondDate firstBeginDate(RequestFilter[] request){
        MicroSecondDate begin = new MicroSecondDate(request[0].start_time);
        MicroSecondDate tmp;
        for (int i = 0; i < request.length; i++) {
            tmp = new MicroSecondDate(request[i].start_time);
            if (tmp.before(begin)){
                begin = tmp;
            }
        }
        return begin;
    }

    public static MicroSecondDate lastEndDate(RequestFilter[] request){
        MicroSecondDate end = new MicroSecondDate(request[0].end_time);
        MicroSecondDate tmp;
        for (int i = 0; i < request.length; i++) {
            tmp = new MicroSecondDate(request[i].end_time);
            if (tmp.after(end)){
                end = tmp;
            }
        }
        return end;
    }

    public static final String UP = "Up";

    public static final String EAST = "East";

    public static final String NORTH = "North";

    public static final String NORTHEAST = NORTH + "-" + EAST;

    public static final String UPEAST = UP + "-" + EAST;

    public static final String UPNORTH = UP + "-" + NORTH;

    public static Font DEFAULT_FONT = new Font("Serif", Font.PLAIN, 12);

    public static Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);

    public static final Font BORDER_FONT = new Font("Serif", Font.PLAIN, 11);

    public static Font BOLD_FONT = new Font("Serif", Font.BOLD, 12);

    public static Font BIG_BOLD_FONT = new Font("Serif", Font.BOLD, 16);

    public static final Stroke ONE_PIXEL_STROKE = new BasicStroke(1);

    public static final Stroke TWO_PIXEL_STROKE = new BasicStroke(2);

    public static final Stroke THREE_PIXEL_STROKE = new BasicStroke(3);

    public static final UnitRangeImpl ZERO_RANGE = new UnitRangeImpl(0, 0, UnitImpl.COUNT);

    public static final UnitRangeImpl ONE_RANGE = new UnitRangeImpl(-1, 1, UnitImpl.COUNT);

    public static final MicroSecondTimeRange ZERO_TIME = new MicroSecondTimeRange(new MicroSecondDate(0), new MicroSecondDate(0));

    public static final MicroSecondTimeRange ONE_TIME = new MicroSecondTimeRange(new MicroSecondDate(0), new MicroSecondDate(1));

    public static final Rectangle2D EMPTY_RECTANGLE = new Rectangle2D.Float(0,0,0,0);

    public static final Color SHALLOW_DEPTH_EVENT = new Color(243, 33, 78);

    public static final Color MEDIUM_DEPTH_EVENT = new Color(246, 185, 42);

    public static final Color DEEP_DEPTH_EVENT = new Color(245, 249, 27);

    public static final Color STATION = new Color(43, 33, 243);

    public static final Color NO_STATUS_STATION = Color.WHITE;

    public static final Color DOWN_STATION = new Color(183, 183, 183);

    private static final Logger logger = Logger.getLogger(DisplayUtils.class);

}// DisplayUtils

