/**
 * DataCenterUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database;
// formally in
//package edu.sc.seis.anhinga.seismogramDC;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Category;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfRealTimeCollector.DataChunk;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesType;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * DataCenterUtil.java
 *
 *
 * Created: Tue Dec  4 10:40:27 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class DataCenterUtil {
    public DataCenterUtil (){

    }

    /** Create LocalSeismograms from a List of DataChunks. The DataChunks
    are assumed all to come from the same channel, and to be in time order.
    */
    public static RequestFilter[] makeRequestFilter(float tolerance,
                                                      List chunks) {
        List splitList = splitGaps(tolerance, chunks);
        Iterator it = splitList.iterator();
        LinkedList seisList = new LinkedList();
        while (it.hasNext()) {
            seisList.add(getRequestFilter((List)it.next()));
        } // end of while (it.hasNext())
        logger.debug("original chunks="+chunks.size()+"  rf size="+seisList.size());
        RequestFilter[] tmpRF = new RequestFilter[0];
        return (RequestFilter[])seisList.toArray(tmpRF);
    }

    /** Create LocalSeismograms from a List of DataChunks. The DataChunks
    are assumed all to come from the same channel, and to be in time order.
    */
    public static LocalSeismogram[] makeSeismograms(float tolerance,
                            List chunks) {
    List splitList = splitGaps(tolerance, chunks);
    Iterator it = splitList.iterator();
    LinkedList seisList = new LinkedList();
    while (it.hasNext()) {
        seisList.add(getSeismogram((List)it.next()));
    } // end of while (it.hasNext())
    LocalSeismogram[] tmpSeis = new LocalSeismogram[0];
    return (LocalSeismogram[])seisList.toArray(tmpSeis);
    }

    /** Analyzes the List of dataChunks and creates a list of lists where
    each sublist has datachunks without any gaps exceeding the tolerance.

    @param tolerance The maximum time shift away from a sample that is
    not considered a time tear, expressed as a percentage of the
    sample period.
    @param chunks a List of DataChunks to be analyzed for gaps.
    @returns a List consisting of Lists that each contain contiguous in
        time data chunks.
    */
    public static List splitGaps(float tolerance, List chunks) {
    List out = new LinkedList();
    List currList = new LinkedList();
    Iterator it = chunks.iterator();
    DataChunk currChunk = null;
    DataChunk prevChunk = currChunk;
    MicroSecondDate curr, prev;

    //initialize
    if (it.hasNext()) {
         currChunk = (DataChunk)it.next();
         curr = new MicroSecondDate(currChunk.begin_time);
         currList.add(currChunk);
    } else {
        // no data???
        return out;
    } // end of else

    if ( ! it.hasNext()) {
        // only one chunk???
        out.add(chunks);
        return out;
    }

    // must be at least 2 data chunks, so can set prev and curr not null.
    while (it.hasNext()) {
        prevChunk = currChunk;
        prev = new MicroSecondDate(prevChunk.end_time);
        currChunk = (DataChunk)it.next();
        curr = new MicroSecondDate(currChunk.begin_time);

        TimeInterval gap =
        (TimeInterval)curr.subtract(prev);
        SamplingImpl samps = (SamplingImpl)getSampling(prevChunk);
        TimeInterval period = samps.getPeriod();
        // end time is last sample, not time of first sample of next chunk
        gap = gap.subtract(period);
        QuantityImpl ratio = gap.divideBy(period); // should be dimensionless

        if (Math.abs(ratio.getValue()) > tolerance) {
        logger.warn("Gap found in seismogram, ratio="+ratio+" > "+tolerance+" "+prevChunk.end_time.date_time+" "+currChunk.begin_time.date_time+" "+
samps+" gap="+gap+" period="+period);
        // start a new list
        out.add(currList);
        currList = new LinkedList();
        }
        currList.add(currChunk);
    }
    // add last list to out
    out.add(currList);
    return out;
    }

    public static SamplingImpl getSampling(DataChunk chunk) {
    int npts = 0;
    if   (chunk.data.discriminator()
          == TimeSeriesType.TYPE_SHORT ) {
        npts = chunk.data.sht_values().length;
    } else if (chunk.data.discriminator()
           == TimeSeriesType.TYPE_LONG ) {
        npts = chunk.data.int_values().length;
    } else if (chunk.data.discriminator()
           == TimeSeriesType.TYPE_FLOAT ) {
        npts = chunk.data.flt_values().length;
    } else if (chunk.data.discriminator()
           == TimeSeriesType.TYPE_DOUBLE ) {
        npts = chunk.data.dbl_values().length;
    } else if (chunk.data.discriminator()
           == TimeSeriesType.TYPE_ENCODED ) {
        EncodedData[] encoded = chunk.data.encoded_values();
        for (int i=0; i<encoded.length; i++) {
        npts += encoded[i].num_points;
        }
    } else {
        throw new RuntimeException("Unknown data type! "+
                    chunk.data.discriminator().value());
    } // end of else

    MicroSecondDate b = new MicroSecondDate(chunk.begin_time);
    MicroSecondDate e = new MicroSecondDate(chunk.end_time);
    TimeInterval t = e.subtract(b);
    SamplingImpl samps =
        new SamplingImpl(npts-1, t);
    return samps;
    }

    /** Concatenates DataChunks into a single LocalSeismogram. It is assumed
    that there are no gaps, and that the chunks are in the correct order.
    The sampling is calculated from the begin time of the first data chunk,
    the end time of the last chunk, and the total number of points, and
    thus may be slightly different from the actual sampling of any
    particular chunk. It is also assumed that all of the chunks have the
    same TimeSeriesType, ie short[], int[], float[], double[] or encoded[].

    @returns a local seismogram unless the List is empty, in which
    case it returns null.
    */
    public static LocalSeismogramImpl getSeismogram(List chunks) {
    ArrayList eData = new ArrayList();
    int numPoints = 0;
    DataChunk currChunk = null;
    MicroSecondDate begin = null;
    MicroSecondDate end;
    ChannelId channelId = null;

    Iterator it = chunks.iterator();

    //check for no data
    if ( ! it.hasNext()) {
        // no data???
        return null;
    } // end of else

    while (it.hasNext()) {
        currChunk = (DataChunk)it.next();
        if (begin == null) {
        begin = new MicroSecondDate(currChunk.begin_time);
        channelId = currChunk.channel;
        } // end of if (begin == null)

        if   (currChunk.data.discriminator()
          == TimeSeriesType.TYPE_SHORT ) {
        numPoints += currChunk.data.sht_values().length;
        eData.add(currChunk.data.sht_values());
        } else if (currChunk.data.discriminator()
               == TimeSeriesType.TYPE_LONG ) {
        numPoints += currChunk.data.int_values().length;
        eData.add(currChunk.data.int_values());
        } else if (currChunk.data.discriminator()
               == TimeSeriesType.TYPE_FLOAT ) {
        numPoints += currChunk.data.flt_values().length;
        eData.add(currChunk.data.flt_values());
        } else if (currChunk.data.discriminator()
               == TimeSeriesType.TYPE_DOUBLE ) {
        numPoints += currChunk.data.dbl_values().length;
        eData.add(currChunk.data.dbl_values());
        } else if (currChunk.data.discriminator()
               == TimeSeriesType.TYPE_ENCODED ) {
        EncodedData[] encoded = currChunk.data.encoded_values();
        for (int i=0; i<encoded.length; i++) {
            numPoints += encoded[i].num_points;
            eData.add(encoded[i]);
        }
        } else {
        throw new RuntimeException("Unknown data type! "+
                      currChunk.data.discriminator().value());
        } // end of else
    }
    end = new MicroSecondDate(currChunk.end_time);

    Sampling samps =
        new SamplingImpl(numPoints-1,
                 end.subtract(begin));

    TimeSeriesDataSel sel = new TimeSeriesDataSel();
    if   (currChunk.data.discriminator()
          == TimeSeriesType.TYPE_SHORT ) {
        short[] whole = new short[numPoints];
        int pos = 0;
        Iterator dataIt = eData.iterator();
        while (dataIt.hasNext()) {
        short[] segment = (short[])dataIt.next();
        System.arraycopy(segment, 0, whole, pos, segment.length);
        pos += segment.length;
        } // end of while (dataIt.hasNext())
        sel.sht_values(whole);
    } else if (currChunk.data.discriminator()
               == TimeSeriesType.TYPE_LONG ) {
        int[] whole = new int[numPoints];
        int pos = 0;
        Iterator dataIt = eData.iterator();
        while (dataIt.hasNext()) {
        int[] segment = (int[])dataIt.next();
        System.arraycopy(segment, 0, whole, pos, segment.length);
        pos += segment.length;
        } // end of while (dataIt.hasNext())
        sel.int_values(whole);
    } else if (currChunk.data.discriminator()
           == TimeSeriesType.TYPE_FLOAT ) {
        float[] whole = new float[numPoints];
        int pos = 0;
        Iterator dataIt = eData.iterator();
        while (dataIt.hasNext()) {
        float[] segment = (float[])dataIt.next();
        System.arraycopy(segment, 0, whole, pos, segment.length);
        pos += segment.length;
        } // end of while (dataIt.hasNext())
        sel.flt_values(whole);
    } else if (currChunk.data.discriminator()
           == TimeSeriesType.TYPE_DOUBLE ) {
        double[] whole = new double[numPoints];
        int pos = 0;
        Iterator dataIt = eData.iterator();
        while (dataIt.hasNext()) {
        double[] segment = (double[])dataIt.next();
        System.arraycopy(segment, 0, whole, pos, segment.length);
        pos += segment.length;
        } // end of while (dataIt.hasNext())
        sel.dbl_values(whole);
    } else if (currChunk.data.discriminator()
           == TimeSeriesType.TYPE_ENCODED ) {
        EncodedData[] whole =
        (EncodedData[])eData.toArray(new EncodedData[eData.size()]);
        sel.encoded_values(whole);
    } else {
        throw new RuntimeException("Unknown data type! "+
                       currChunk.data.discriminator().value());
    } // end of else


    LocalSeismogramImpl ls = new LocalSeismogramImpl(ChannelIdUtil.toString(channelId)+":"+begin.getFissuresTime().date_time+"/"+numPoints+"/"+(new Date()).getTime(),
                             begin.getFissuresTime(),
                             numPoints,
                             samps,
                             UnitImpl.COUNT,
                             channelId,
                             sel);
    return ls;
    }

    /** Concatenates DataChunks into a single RequestFilter. It is assumed
    that there are no gaps, and that the chunks are in the correct order.

    @returns a RequestFilter unless the List is empty, in which
    case it returns null.
    */
    public static RequestFilter getRequestFilter(List chunks) {
        ArrayList eData = new ArrayList();
        int numPoints = 0;
        DataChunk currChunk = null;
        MicroSecondDate begin = null;
        MicroSecondDate end;
        ChannelId channelId = null;

        Iterator it = chunks.iterator();

        //check for no data
        if ( ! it.hasNext()) {
            // no data???
            return null;
        } // end of else

        while (it.hasNext()) {
            currChunk = (DataChunk)it.next();
            if (begin == null) {
                begin = new MicroSecondDate(currChunk.begin_time);
                channelId = currChunk.channel;
            } // end of if (begin == null)

            if  (currChunk.data.discriminator()
                 == TimeSeriesType.TYPE_SHORT ) {
                numPoints += currChunk.data.sht_values().length;
                eData.add(currChunk.data.sht_values());
            } else if (currChunk.data.discriminator()
                       == TimeSeriesType.TYPE_LONG ) {
                numPoints += currChunk.data.int_values().length;
                eData.add(currChunk.data.int_values());
            } else if (currChunk.data.discriminator()
                       == TimeSeriesType.TYPE_FLOAT ) {
                numPoints += currChunk.data.flt_values().length;
                eData.add(currChunk.data.flt_values());
            } else if (currChunk.data.discriminator()
                       == TimeSeriesType.TYPE_DOUBLE ) {
                numPoints += currChunk.data.dbl_values().length;
                eData.add(currChunk.data.dbl_values());
            } else if (currChunk.data.discriminator()
                       == TimeSeriesType.TYPE_ENCODED ) {
                EncodedData[] encoded = currChunk.data.encoded_values();
                for (int i=0; i<encoded.length; i++) {
                    numPoints += encoded[i].num_points;
                    eData.add(encoded[i]);
                }
            } else {
                throw new RuntimeException("Unknown data type! "+
                                           currChunk.data.discriminator().value());
            } // end of else
        }
        end = new MicroSecondDate(currChunk.end_time);

        RequestFilter rf = new RequestFilter(channelId,
                                             begin.getFissuresTime(),
                                             end.getFissuresTime());
        return rf;
    }

    static Category logger =
        Category.getInstance(DataCenterUtil.class.getName());

}// DataCenterUtil
