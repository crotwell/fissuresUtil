package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
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
 * @version $Id: SimplePlotUtil.java 11561 2005-01-05 18:23:08Z groves $
 */
public class SimplePlotUtil {

    /**
     * Creates a plottable with all the data from the seismogram that falls
     * inside of the time range at samplesPerDay. Each pixel in the plottable is
     * of 1/samplesPerDay * 2 days long so that two values can be returned for
     * each x point in the plottable. The first value is the min value over the
     * time covered in the seismogram, and the second value is the max. The
     * seismogram points in a plottable pixel consist of the first point at or
     * after the start time of the pixel to the last point before the start time
     * of the next pixel.
     */
    public static Plottable makePlottable(LocalSeismogramImpl seis,
                                          MicroSecondTimeRange tr,
                                          int samplesPerDay)
            throws CodecException {
        //Calculating the number of plottable pixels to cover the full time
        // range
        double pixelPeriod = 1 / (double)samplesPerDay * 2.0d;//in days
        TimeInterval trInt = (TimeInterval)tr.getInterval()
                .convertTo(UnitImpl.DAY);
        double exactNumPixels = trInt.divideBy(pixelPeriod).getValue();
        //always round up since a partial pixel means the caller requested data
        // in that pixel
        int numPixels = (int)Math.ceil(exactNumPixels);
        TimeInterval pointPeriod = (TimeInterval)seis.getSampling()
                .getPeriod()
                .convertTo(UnitImpl.DAY);
        double pointsPerPixel = pixelPeriod / pointPeriod.getValue();
        int startPoint = getPoint(seis, tr.getBeginTime());
        int endPoint = startPoint + (int)(pointsPerPixel * numPixels);
        int startPixel = 0;
        if(startPoint < 0) {
            //Requested time begins before seis, scoot up the start pixel up
            startPixel = (int)Math.floor((startPoint * -1) / pointsPerPixel);
            numPixels -= startPixel;
        }
        if(endPoint > seis.getNumPoints()) {
            //Requested time ends after seis, scoot the end pixel back
            int pointShift = endPoint - seis.getNumPoints();
            numPixels -= (int)Math.floor(pointShift / pointsPerPixel);
            endPoint = seis.getNumPoints();
        }
        int[][] pixels = new int[2][numPixels * 2];
        int pixelPoint = startPoint < 0 ? 0 : startPoint;
        for(int i = 0; i < numPixels; i++) {
            int pos = 2 * i;
            int nextPos = pos + 1;
            pixels[0][pos] = startPixel + i;
            pixels[0][nextPos] = pixels[0][pos];
            int nextPixelPoint = startPoint
                    + (int)((pixels[0][pos] + 1) * pointsPerPixel);
            if(i == numPixels - 1) {
                nextPixelPoint = endPoint;
            }
            QuantityImpl min = seis.getMinValue(pixelPoint, nextPixelPoint);
            pixels[1][pos] = (int)min.getValue();
            QuantityImpl max = seis.getMaxValue(pixelPoint, nextPixelPoint);
            pixels[1][nextPos] = (int)max.getValue();
            pixelPoint = nextPixelPoint;
        }
        return new Plottable(pixels[0], pixels[1]);
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
            if(yValues[i] < minValue) minValue = yValues[i];
        }
        return minValue;
    }

    private static int getMaxValue(int[] yValues, int startIndex, int endIndex) {
        int maxValue = java.lang.Integer.MIN_VALUE;
        for(int i = startIndex; i <= endIndex; i++) {
            if(yValues[i] > maxValue) maxValue = yValues[i];
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
        if(x == xa) return ya;
        if(x == xb) return yb;
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
        return (int)Math.round(linearInterp(begin.getMicroSecondTime(),
                                            0,
                                            end.getMicroSecondTime(),
                                            totalPixels,
                                            value.getMicroSecondTime()));
    }

    public static final MicroSecondDate getValue(int totalPixels,
                                                 MicroSecondDate begin,
                                                 MicroSecondDate end,
                                                 int pixel) {
        double value = linearInterp(0, 0, totalPixels, end.getMicroSecondTime()
                - begin.getMicroSecondTime(), pixel);
        return new MicroSecondDate(begin.getMicroSecondTime()
                + Math.round(value));
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
        return (int)Math.round(linearInterp(range.getMinValue(),
                                            0,
                                            range.getMaxValue(),
                                            totalPixels,
                                            value));
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

    public static LocalSeismogramImpl createTestData() {
        return createTestData("Fake Data");
    }

    public static LocalSeismogramImpl createTestData(String name) {
        int[] dataBits = new int[100];
        double tmpDouble;
        for(int i = 0; i < dataBits.length; i++) {
            tmpDouble = Math.random() * 2.0 - 1.0;
            //    tmpDouble = .4 + Math.random()*.1;
            // this makes the values a little more likely to be close
            // to the center, making it slightly more seimogram like
            tmpDouble = tmpDouble * tmpDouble * tmpDouble * tmpDouble
                    * tmpDouble;
            dataBits[i] = (int)Math.round(tmpDouble * 2000.0);
        }
        return createTestData(name, dataBits);
    }

    public static LocalSeismogramImpl createTestData(String name, int[] dataBits) {
        String id = "Nowhere: " + name;
        edu.iris.Fissures.Time time = new edu.iris.Fissures.Time("19991231T235959.000Z",
                                                                 -1);
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        ChannelId channelID = new ChannelId(new NetworkId("XX", time),
                                            "FAKE",
                                            "00",
                                            "BHZ",
                                            time);
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);
        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
        TimeInterval[] time_corr = new TimeInterval[1];
        time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = new LocalSeismogramImpl(id,
                                                           props,
                                                           time,
                                                           dataBits.length,
                                                           sampling,
                                                           UnitImpl.COUNT,
                                                           channelID,
                                                           new ParameterRef[0],
                                                           time_corr,
                                                           new SamplingImpl[0],
                                                           bits);
        return seis;
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
        String id = "Nowhere: " + name;
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = new SamplingImpl(20, timeInterval);
        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);
        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
        TimeInterval[] time_corr = new TimeInterval[1];
        time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = new LocalSeismogramImpl(id,
                                                           props,
                                                           time,
                                                           dataBits.length,
                                                           sampling,
                                                           UnitImpl.COUNT,
                                                           channelID,
                                                           new ParameterRef[0],
                                                           time_corr,
                                                           new SamplingImpl[0],
                                                           bits);
        return seis;
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

    public static LocalSeismogramImpl createSpike() {
        return createSpike(ClockUtil.now());
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate spikeTime) {
        return createSpike(spikeTime,
                           new TimeInterval(50, UnitImpl.SECOND),
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
        double secondShift = missingSamples / (double)SPIKE_SAMPLES_PER_SECOND;
        TimeInterval shiftInt = new TimeInterval(secondShift, UnitImpl.SECOND);
        time = time.add(shiftInt);
        traceLength = traceLength.subtract(shiftInt);
        String name = "spike at " + time.toString();
        double traceSecs = traceLength.convertTo(UnitImpl.SECOND).getValue();
        int[] dataBits = new int[(int)(SPIKE_SAMPLES_PER_SECOND * traceSecs)];
        for(int i = 0; i < dataBits.length; i++) {
            if((i + missingSamples) % samplesPerSpike == 0) {
                dataBits[i] = 100;
            }
        }
        return createTestData(name, dataBits, time.getFissuresTime(), id);
    }

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public static final int SPIKE_SAMPLES_PER_SECOND = 20;

    static Category logger = Category.getInstance(SimplePlotUtil.class.getName());
} // SimplePlotUtil
