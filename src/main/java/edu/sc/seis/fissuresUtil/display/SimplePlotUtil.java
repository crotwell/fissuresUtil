package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.log4j.Category;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;

/**
 * SimplePlotUtil.java Created: Thu Jul 8 11:22:02 1999
 * 
 * @author Philip Crotwell, Charlie Groves
 * @version $Id: SimplePlotUtil.java 20217 2009-01-28 20:58:30Z crotwell $
 */
public class SimplePlotUtil {

    /**
     * Creates a plottable with all the data from the seismogram that falls
     * inside of the time range at samplesPerDay. Each pixel in the plottable is
     * of 1/pixelsPerDay days long. Two points are returned for each pixel. The
     * first value is the min value over the time covered in the seismogram, and
     * the second value is the max. The seismogram points in a plottable pixel
     * consist of the first point at or after the start time of the pixel to the
     * last point before the start time of the next pixel.
     */
    public static Plottable makePlottable(LocalSeismogramImpl seis,
                                          int pixelsPerDay)
            throws CodecException {
        MicroSecondTimeRange correctedSeisRange = correctTimeRangeForPixelData(seis,
                                                                               pixelsPerDay);
        int startPoint = getPoint(seis, correctedSeisRange.getBeginTime());
        int endPoint = getPoint(seis, correctedSeisRange.getEndTime());
        IntRange seisPixelRange = getDayPixelRange(seis,
                                                   pixelsPerDay,
                                                   seis.getBeginTime());
        int numPixels = seisPixelRange.getDifference();
        // check to see if numPixels doesn't go over
        MicroSecondDate rangeEnd = correctedSeisRange.getBeginTime()
                .add(new TimeInterval(getPixelPeriod(pixelsPerDay).multiplyBy(numPixels)));
        boolean corrected = false;
        if(rangeEnd.after(correctedSeisRange.getEndTime())) {
            numPixels--;
            corrected = true;
        }
        // end check and correction
        int startPixel = seisPixelRange.getMin();
        int[][] pixels = new int[2][numPixels * 2];
        int pixelPoint = startPixel < 0 ? 0 : startPoint;
        MicroSecondDate pixelEndTime = correctedSeisRange.getBeginTime();
        TimeInterval pixelPeriod = getPixelPeriod(pixelsPerDay);
        if(corrected) {}
        for(int i = 0; i < numPixels; i++) {
            pixelEndTime = pixelEndTime.add(pixelPeriod);
            int pos = 2 * i;
            int nextPos = pos + 1;
            pixels[0][pos] = startPixel + i;
            pixels[0][nextPos] = pixels[0][pos];
            int nextPixelPoint = getPixel(startPoint,
                                          endPoint,
                                          correctedSeisRange.getBeginTime(),
                                          correctedSeisRange.getEndTime(),
                                          pixelEndTime);
            QuantityImpl min = seis.getMinValue(pixelPoint, nextPixelPoint);
            pixels[1][pos] = (int)min.getValue();
            QuantityImpl max = seis.getMaxValue(pixelPoint, nextPixelPoint);
            pixels[1][nextPos] = (int)max.getValue();
            if(corrected && (i < 2 || i >= numPixels - 2)) {
                logger.debug(pixels[0][pos] + ": min " + min.value + " max "
                        + max.value);
            }
            pixelPoint = nextPixelPoint;
        }
        return new Plottable(pixels[0], pixels[1]);
    }

    public static Plottable getEmptyPlottable() {
        int[] empty = new int[0];
        return new Plottable(empty, empty);
    }

    public static void debugExtraPixel(MicroSecondTimeRange correctedSeisRange,
                                       MicroSecondDate rangeEnd,
                                       LocalSeismogramImpl seis,
                                       int startPoint,
                                       int endPoint,
                                       int numPixels,
                                       IntRange seisPixelRange,
                                       int startPixel,
                                       TimeInterval pixelPeriod) {
        logger.warn("corrected for freak extra pixel!");
        logger.debug("correctedSeisRange: " + correctedSeisRange);
        logger.debug("end of range would have been " + rangeEnd
                + " without correction");
        logger.debug("seis.num_points: " + seis.num_points);
        logger.debug("startPoint: " + startPoint);
        logger.debug("endPoint: " + endPoint);
        logger.debug("seisPixelRange: " + seisPixelRange);
        logger.debug("numPixels after correction: " + numPixels);
        logger.debug("startPixel: " + startPixel);
        logger.debug("pixelPeriod: " + pixelPeriod);
    }

    public static TimeInterval getPixelPeriod(int pixelsPerDay) {
        double pixelPeriod = 1.0 / (double)pixelsPerDay;
        return new TimeInterval(pixelPeriod, UnitImpl.DAY);
    }

    public static MicroSecondDate getBeginningOfDay(MicroSecondDate date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new MicroSecondDate(cal.getTime());
    }

    public static MicroSecondTimeRange getDay(MicroSecondDate date) {
        return new MicroSecondTimeRange(getBeginningOfDay(date), ONE_DAY);
    }

    public static MicroSecondDate getPixelBeginTime(MicroSecondTimeRange day,
                                                    int pixel,
                                                    int pixelsPerDay) {
        TimeInterval pixelPeriod = getPixelPeriod(pixelsPerDay);
        return day.getBeginTime()
                .add(new TimeInterval(pixelPeriod.multiplyBy(pixel)));
    }

    /*
     * gets the time range that makes up one pixel of plottable data that either
     * surrounds the given date or is directly after the given date
     */
    public static MicroSecondTimeRange getPixelTimeRange(MicroSecondDate point,
                                                         int pixelsPerDay,
                                                         boolean after) {
        TimeInterval pixelPeriod = getPixelPeriod(pixelsPerDay);
        MicroSecondTimeRange day = getDay(point);
        int pixel = getPixel(pixelsPerDay, day, point);
        if(after) {
            pixel++;
        }
        MicroSecondDate pixelBegin = getPixelBeginTime(day, pixel, pixelsPerDay);
        return new MicroSecondTimeRange(pixelBegin, pixelPeriod);
    }

    /*
     * Gets the pixel range of the seismogram from the point of view of the
     * beginning (midnight) of the day of the begin time of the seismogram. This
     * is to say that if you have an two-hour-long seismogram starting at noon
     * on a day with a resolution of 12 pixels per day, the range returned would
     * be 6 to 7.
     */
    public static IntRange getDayPixelRange(LocalSeismogram seis,
                                            int pixelsPerDay) {
        return getDayPixelRange(seis,
                                pixelsPerDay,
                                getBeginningOfDay(new MicroSecondDate(seis.begin_time)));
    }

    /*
     * Same as above, except day can start at any time. The pixel time
     * boundaries are still dependent upon midnight of the seismogram start
     * time.
     */
    public static IntRange getDayPixelRange(LocalSeismogram seis,
                                            int pixelsPerDay,
                                            MicroSecondDate startOfDay) {
        MicroSecondTimeRange seisTR = new MicroSecondTimeRange((LocalSeismogramImpl)seis);
        MicroSecondTimeRange dayTR = new MicroSecondTimeRange(startOfDay,
                                                              ONE_DAY);
        int startPixel = getPixel(pixelsPerDay, dayTR, seisTR.getBeginTime());
        if(getPixelTimeRange(seisTR.getBeginTime(), pixelsPerDay, false).getBeginTime()
                .before(seisTR.getBeginTime())) {
            // we don't want pixels with partial data
            startPixel++;
        }
        int endPixel = getPixel(pixelsPerDay, dayTR, seisTR.getEndTime());
        if(endPixel < startPixel) {
            // yes, this pretty much means the difference of the pixel range
            // will be 0
            endPixel = startPixel;
        }
        return new IntRange(startPixel, endPixel);
    }

    public static boolean canMakeAtLeastOnePixel(LocalSeismogram seis,
                                                 int pixelsPerDay) {
        IntRange pixelRange = getDayPixelRange(seis, pixelsPerDay);
        return pixelRange.getMax() > pixelRange.getMin();
    }

    public static MicroSecondTimeRange correctTimeRangeForPixelData(LocalSeismogram seis,
                                                                    int pixelsPerDay) {
        IntRange pixelRange = getDayPixelRange(seis, pixelsPerDay);
        MicroSecondTimeRange day = getDay(new MicroSecondDate(seis.begin_time));
        MicroSecondDate start = getPixelBeginTime(day,
                                                  pixelRange.getMin(),
                                                  pixelsPerDay);
        MicroSecondDate end = getPixelBeginTime(day,
                                                pixelRange.getMax(),
                                                pixelsPerDay);
        return new MicroSecondTimeRange(start, end);
    }

    public static int[][] compressXvalues(LocalSeismogram seismogram,
                                          MicroSecondTimeRange timeRange,
                                          Dimension size) throws CodecException {
        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
        int width = size.width;
        int[][] out = new int[2][];
        if(seis.getEndTime().before(timeRange.getBeginTime())
                || seis.getBeginTime().after(timeRange.getEndTime())) {
            out[0] = new int[0];
            out[1] = new int[0];
            logger.info("The end time is before the beginTime in simple seismogram");
            return out;
        }
        MicroSecondDate tMin = timeRange.getBeginTime();
        MicroSecondDate tMax = timeRange.getEndTime();
        int seisStartIndex = getPoint(seis, tMin);
        int seisEndIndex = getPoint(seis, tMax);
        if(seisStartIndex < 0) {
            seisStartIndex = 0;
        }
        if(seisEndIndex >= seis.getNumPoints()) {
            seisEndIndex = seis.getNumPoints() - 1;
        }
        MicroSecondDate tempdate = getValue(seis.getNumPoints(),
                                            seis.getBeginTime(),
                                            seis.getEndTime(),
                                            seisStartIndex);
        int pixelStartIndex = getPixel(width, timeRange, tempdate);
        tempdate = getValue(seis.getNumPoints(),
                            seis.getBeginTime(),
                            seis.getEndTime(),
                            seisEndIndex);
        int pixelEndIndex = getPixel(width, timeRange, tempdate);
        int pixels = seisEndIndex - seisStartIndex + 1;
        out[0] = new int[2 * pixels];
        out[1] = new int[out[0].length];
        int tempYvalues[] = new int[out[0].length];
        int seisIndex = seisStartIndex;
        int numAdded = 0;
        int xvalue = Math.round((float)(linearInterp(seisStartIndex,
                                                     pixelStartIndex,
                                                     seisEndIndex,
                                                     pixelEndIndex,
                                                     seisIndex)));
        int tempValue = 0;
        seisIndex++;
        int j = 0;
        while(seisIndex <= seisEndIndex) {
            tempValue = Math.round((float)(linearInterp(seisStartIndex,
                                                        pixelStartIndex,
                                                        seisEndIndex,
                                                        pixelEndIndex,
                                                        seisIndex)));
            tempYvalues[j++] = (int)seis.getValueAt(seisIndex).getValue();
            if(tempValue != xvalue) {
                out[0][numAdded] = xvalue;
                out[0][numAdded + 1] = xvalue;
                out[1][numAdded] = getMinValue(tempYvalues, 0, j - 1);
                out[1][numAdded + 1] = getMaxValue(tempYvalues, 0, j - 1);
                j = 0;
                xvalue = tempValue;
                numAdded = numAdded + 2;
            }
            seisIndex++;
        }
        int temp[][] = new int[2][numAdded];
        System.arraycopy(out[0], 0, temp[0], 0, numAdded);
        System.arraycopy(out[1], 0, temp[1], 0, numAdded);
        return temp;
    }

    private static int getMinValue(int[] yValues, int startIndex, int endIndex) {
        int minValue = java.lang.Integer.MAX_VALUE;
        for(int i = startIndex; i <= endIndex; i++) {
            if(yValues[i] < minValue)
                minValue = yValues[i];
        }
        return minValue;
    }

    private static int getMaxValue(int[] yValues, int startIndex, int endIndex) {
        int maxValue = java.lang.Integer.MIN_VALUE;
        for(int i = startIndex; i <= endIndex; i++) {
            if(yValues[i] > maxValue)
                maxValue = yValues[i];
        }
        return maxValue;
    }

    /**
     * solves the equation (yb-ya)/(xb-xa) = (y-ya)/(x-xa) for y given x. Useful
     * for finding the pixel for a value given the dimension of the area and the
     * range of values it is supposed to cover. Note, this does not check for xa ==
     * xb, in which case a divide by zero would occur.
     */
    public static final double linearInterp(double xa,
                                            double ya,
                                            double xb,
                                            double yb,
                                            double x) {
        if(x == xa) {
            return ya;
        }
        if(x == xb) {
            return yb;
        }
        return (yb - ya) * (x - xa) / (xb - xa) + ya;
    }

    public static final int getPixel(int totalPixels,
                                     MicroSecondTimeRange tr,
                                     MicroSecondDate value) {
        return getPixel(totalPixels, tr.getBeginTime(), tr.getEndTime(), value);
    }

    public static final int getPoint(LocalSeismogramImpl seis,
                                     MicroSecondDate time) {
        return getPixel(seis.getNumPoints(),
                        seis.getBeginTime(),
                        seis.getEndTime(),
                        time);
    }

    public static final int getPixel(int totalPixels,
                                     MicroSecondDate begin,
                                     MicroSecondDate end,
                                     MicroSecondDate value) {
        return getPixel(0, totalPixels, begin, end, value);
    }

    public static final int getPixel(int startPixel,
                                     int endPixel,
                                     MicroSecondDate begin,
                                     MicroSecondDate end,
                                     MicroSecondDate value) {
        return (int)linearInterp(begin.getMicroSecondTime(),
                                 startPixel,
                                 end.getMicroSecondTime(),
                                 endPixel,
                                 value.getMicroSecondTime());
    }

    public static final MicroSecondDate getValue(int totalPixels,
                                                 MicroSecondDate begin,
                                                 MicroSecondDate end,
                                                 int pixel) {
        return getValue(0, totalPixels, begin, end, pixel);
    }

    public static final MicroSecondDate getValue(int startPixel,
                                                 int endPixel,
                                                 MicroSecondDate begin,
                                                 MicroSecondDate end,
                                                 int pixel) {
        double value = linearInterp(startPixel,
                                    0,
                                    endPixel,
                                    end.getMicroSecondTime()
                                            - begin.getMicroSecondTime(),
                                    pixel);
        return new MicroSecondDate(begin.getMicroSecondTime() + (long)value);
    }

    public static final int getPixel(int totalPixels,
                                     UnitRangeImpl range,
                                     QuantityImpl value) {
        QuantityImpl converted = value.convertTo(range.getUnit());
        return getPixel(totalPixels, range, converted.getValue());
    }

    public static final int getPixel(int totalPixels,
                                     UnitRangeImpl range,
                                     double value) {
        return (int)linearInterp(range.getMinValue(),
                                 0,
                                 range.getMaxValue(),
                                 totalPixels,
                                 value);
    }

    public static final QuantityImpl getValue(int totalPixels,
                                              UnitRangeImpl range,
                                              int pixel) {
        double value = linearInterp(0,
                                    range.getMinValue(),
                                    totalPixels,
                                    range.getMaxValue(),
                                    pixel);
        return new QuantityImpl(value, range.getUnit());
    }

    public static final MicroSecondDate getTimeForIndex(int index,
                                                        MicroSecondDate beginTime,
                                                        SamplingImpl sampling) {
        TimeInterval width = sampling.getPeriod();
        width = (TimeInterval)width.multiplyBy(index);
        return beginTime.add(width);
    }

    public static int[] createRandomDataBits(int length) {
        int[] dataBits = new int[length];
        double tmpDouble;
        for(int i = 0; i < dataBits.length; i++) {
            tmpDouble = Math.random() * 2.0 - 1.0;
            // tmpDouble = .4 + Math.random()*.1;
            // this makes the values a little more likely to be close
            // to the center, making it slightly more seimogram like
            tmpDouble = tmpDouble * tmpDouble * tmpDouble * tmpDouble
                    * tmpDouble;
            dataBits[i] = (int)Math.round(tmpDouble * 2000.0);
        }
        return dataBits;
    }

    public static LocalSeismogramImpl createTestData() {
        return createTestData("Fake Data");
    }

    public static LocalSeismogramImpl createTestData(String name) {
        return createTestData(name, createRandomDataBits(100));
    }

    public static LocalSeismogramImpl createTestData(String name, int[] dataBits) {
        return createTestData(name, createBits(dataBits), dataBits.length);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     float[] dataBits) {
        return createTestData(name, createBits(dataBits), dataBits.length);
    }

    private static LocalSeismogramImpl createTestData(String name,
                                                      TimeSeriesDataSel bits,
                                                      int bitsLength) {
        edu.iris.Fissures.Time time = new edu.iris.Fissures.Time("19991231T235959.000Z",
                                                                 -1);
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        return createTestData(name,
                              bits,
                              bitsLength,
                              time,
                              makeChanId(time),
                              sampling);
    }

    public static ChannelId makeChanId(edu.iris.Fissures.Time time) {
        return new ChannelId(new NetworkId("XX", time),
                             "FAKE",
                             "00",
                             "BHZ",
                             time);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     edu.iris.Fissures.Time time) {
        return createTestData(name, dataBits, time, makeChanId(time));
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     edu.iris.Fissures.Time time,
                                                     ChannelId channelID) {
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        return createTestData(name, dataBits, time, channelID, sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     edu.iris.Fissures.Time time,
                                                     ChannelId channelID,
                                                     SamplingImpl sampling) {
        return createTestData(name,
                              createBits(dataBits),
                              dataBits.length,
                              time,
                              channelID,
                              sampling);
    }

    public static LocalSeismogramImpl createTestData(String name,
                                                     float[] dataBits,
                                                     edu.iris.Fissures.Time time,
                                                     ChannelId channelID,
                                                     SamplingImpl sampling) {
        return createTestData(name,
                              createBits(dataBits),
                              dataBits.length,
                              time,
                              channelID,
                              sampling);
    }

    private static LocalSeismogramImpl createTestData(String name,
                                                      TimeSeriesDataSel bits,
                                                      int bitsLength,
                                                      edu.iris.Fissures.Time time,
                                                      ChannelId channelID,
                                                      SamplingImpl sampling) {
        String id = "Nowhere: " + name;
        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
        TimeInterval[] time_corr = new TimeInterval[1];
        time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = new LocalSeismogramImpl(id,
                                                           props,
                                                           time,
                                                           bitsLength,
                                                           sampling,
                                                           UnitImpl.COUNT,
                                                           channelID,
                                                           new ParameterRef[0],
                                                           time_corr,
                                                           new SamplingImpl[0],
                                                           bits);
        return seis;
    }

    private static TimeSeriesDataSel createBits(int[] dataBits) {
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);
        return bits;
    }

    private static TimeSeriesDataSel createBits(float[] dataBits) {
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.flt_values(dataBits);
        return bits;
    }

    public static LocalSeismogramImpl createCustomSineWave() {
        int[] dataBits = new int[1200];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(0 + i * Math.PI * 1 / 20.0) * 1000);
        }
        return createTestData("Sine Wave",
                              dataBits,
                              new edu.iris.Fissures.Time("19911015T163000.000Z",
                                                         -1));
    }

    public static LocalSeismogramImpl createSineWave() {
        return createSineWave(0);
    }

    public static LocalSeismogramImpl createSineWave(double phase) {
        return createSineWave(phase, 1);
    }

    public static LocalSeismogramImpl createSineWave(double phase, double hertz) {
        return createSineWave(phase, hertz, 1200);
    }

    public static LocalSeismogramImpl createSineWave(double phase,
                                                     double hertz,
                                                     int numPoints) {
        return createSineWave(phase, hertz, numPoints, 1000);
    }

    public static LocalSeismogramImpl createSineWave(double phase,
                                                     double hertz,
                                                     int numPoints,
                                                     double amp) {
        int[] dataBits = new int[numPoints];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0)
                    * amp);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createFloatSineWave(double phase,
                                                          double hertz,
                                                          int numPoints,
                                                          double amp) {
        float[] dataBits = new float[numPoints];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (float)(Math.sin(phase + i * Math.PI * hertz / 20.0) * amp);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createHighSineWave(double phase,
                                                         double hertz) {
        int[] dataBits = new int[120];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0) * 1000.0 + 500);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createLowSineWave(double phase,
                                                        double hertz) {
        int[] dataBits = new int[120];
        for(int i = 0; i < dataBits.length; i++) {
            dataBits[i] = (int)Math.round(Math.sin(phase + i * Math.PI * hertz
                    / 20.0) * 1000.0 - 500);
        }
        return createTestData("Sine Wave, phase " + phase + " hertz " + hertz,
                              dataBits);
    }

    public static LocalSeismogramImpl createDelta() {
        MicroSecondDate now = ClockUtil.now();
        TimeInterval traceLength = new TimeInterval(50, UnitImpl.SECOND);
        double traceSecs = traceLength.getValue(UnitImpl.SECOND);
        int[] dataBits = new int[(int)(SPIKE_SAMPLES_PER_SECOND * traceSecs)];
        dataBits[0] = 1;
        return createTestData("kronecker delta at 0",
                              dataBits,
                              now.getFissuresTime(),
                              makeChanId(now.getFissuresTime()));
    }

    public static LocalSeismogramImpl createSpike() {
        return createSpike(ClockUtil.now());
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate spikeTime) {
        return createSpike(spikeTime, new TimeInterval(50, UnitImpl.SECOND));
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate spikeTime,
                                                  TimeInterval traceLength) {
        return createSpike(spikeTime,
                           traceLength,
                           20,
                           makeChanId(spikeTime.getFissuresTime()));
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate time,
                                                  TimeInterval traceLength,
                                                  int samplesPerSpike,
                                                  ChannelId id) {
        return createRaggedSpike(time, traceLength, samplesPerSpike, 0, id);
    }

    public static LocalSeismogramImpl createRaggedSpike(MicroSecondDate time,
                                                        TimeInterval traceLength,
                                                        int samplesPerSpike,
                                                        int missingSamples,
                                                        ChannelId id) {
        return createRaggedSpike(time,
                                 traceLength,
                                 samplesPerSpike,
                                 missingSamples,
                                 id,
                                 SPIKE_SAMPLES_PER_SECOND);
    }

    public static LocalSeismogramImpl createRaggedSpike(MicroSecondDate time,
                                                        TimeInterval traceLength,
                                                        int samplesPerSpike,
                                                        int missingSamples,
                                                        ChannelId id,
                                                        double samplesPerSecond) {
        double secondShift = missingSamples / samplesPerSecond;
        TimeInterval shiftInt = new TimeInterval(secondShift, UnitImpl.SECOND);
        time = time.add(shiftInt);
        traceLength = traceLength.subtract(shiftInt);
        String name = "spike at " + time.toString();
        double traceSecs = traceLength.convertTo(UnitImpl.SECOND).getValue();
        int[] dataBits = new int[(int)Math.round((samplesPerSecond * traceSecs))];
        for(int i = 0; i < dataBits.length; i++) {
            if((i + missingSamples) % samplesPerSpike == 0) {
                dataBits[i] = 100;
            }
        }
        return createTestData(name,
                              dataBits,
                              time.getFissuresTime(),
                              id,
                              new SamplingImpl(dataBits.length,
                                               new TimeInterval(traceSecs,
                                                                UnitImpl.SECOND)));
    }

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public static final int SPIKE_SAMPLES_PER_SECOND = 20;

    static Category logger = Category.getInstance(SimplePlotUtil.class.getName());
} // SimplePlotUtil
