package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import org.apache.log4j.Category;
import org.omg.RTCORBA.minPriority;
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
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;

/**
 * SimplePlotUtil.java Created: Thu Jul 8 11:22:02 1999
 * 
 * @author Philip Crotwell, Charlie Groves
 * @version $Id: SimplePlotUtil.java 11103 2004-11-08 16:21:23Z groves $
 */
public class SimplePlotUtil {

    private static MicroSecondDate getTime(MicroSecondDate time,
                                           int sample,
                                           int spd) {
        int jday = PlottableChunk.getJDay(time);
        int year = PlottableChunk.getYear(time);
        return PlottableChunk.getTime(sample, jday, year, spd);
    }

    public static int[][] makePlottable(LocalSeismogramImpl seis,
                                        MicroSecondTimeRange tr,
                                        int samplesPerDay)
            throws CodecException {
        MicroSecondDate startTime = tr.getBeginTime();
        if(seis.getBeginTime().after(startTime)) {
            startTime = seis.getBeginTime();
        }
        MicroSecondDate stopTime = tr.getEndTime();
        if(seis.getEndTime().before(stopTime)) {
            stopTime = seis.getEndTime();
        }
        int startSample = PlottableChunk.getSample(startTime, samplesPerDay);
        startTime = getTime(startTime, startSample, samplesPerDay);
        int stopSample = PlottableChunk.getSample(stopTime, samplesPerDay);
        stopTime = getTime(stopTime, stopSample, samplesPerDay);
        double lengthInDays = stopTime.difference(startTime)
                .getValue(UnitImpl.DAY);
        int numSamples = (int)Math.ceil(lengthInDays * samplesPerDay);
        if(numSamples % 2 == 1) {
            numSamples++;
        }
        int[][] out = new int[2][numSamples];
        TimeInterval startShift = startTime.subtract(tr.getBeginTime());
        double daysShifted = startShift.getValue(UnitImpl.DAY);
        int xOffset = (int)Math.floor(daysShifted * samplesPerDay / 2);
        TimeInterval plottableSampleLength = (TimeInterval)new TimeInterval(1,
                                                                            UnitImpl.DAY).divideBy(samplesPerDay)
                .convertTo(UnitImpl.SECOND);
        double seisSamplesPerSample = plottableSampleLength.divideBy(seis.getSampling()
                .getPeriod()
                .convertTo(UnitImpl.SECOND))
                .getValue();
        for(int i = 0; i < numSamples; i += 2) {
            out[0][i] = xOffset + i / 2;
            out[0][i + 1] = xOffset + i / 2;
            int startSeisSample = (int)Math.floor(i * seisSamplesPerSample);
            if(startSeisSample < 0) {
                startSeisSample = 0;
            }
            int stopSeisSample = (int)Math.ceil((i + 1) * seisSamplesPerSample);
            if(stopSeisSample > seis.getNumPoints()) {
                stopSeisSample = seis.getNumPoints();
            }
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for(int j = startSeisSample; j < stopSeisSample; j++) {
                int val = (int)seis.getValueAt(j).getValue();
                if(val < min) {
                    min = val;
                }
                if(val > max) {
                    max = val;
                }
            }
            out[1][i] = min;
            out[1][i + 1] = max;
        }
        return out;
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

    public static LocalSeismogramImpl createTestData(String name,
                                                     int[] dataBits,
                                                     edu.iris.Fissures.Time time) {
        String id = "Nowhere: " + name;
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
        return createSpike(spikeTime, new TimeInterval(50, UnitImpl.SECOND), 20);
    }

    public static LocalSeismogramImpl createSpike(MicroSecondDate time,
                                                  TimeInterval traceLength,
                                                  int samplesPerSpike) {
        return createRaggedSpike(time, traceLength, samplesPerSpike, 0);
    }

    public static LocalSeismogramImpl createRaggedSpike(MicroSecondDate time,
                                                        TimeInterval traceLength,
                                                        int samplesPerSpike,
                                                        int missingSamples) {
        double secondShift = missingSamples / (double)SPIKE_SAMPLES_PER_SECOND;
        TimeInterval shiftInt = new TimeInterval(secondShift, UnitImpl.SECOND);
        time = time.add(shiftInt);
        traceLength = traceLength.subtract(shiftInt);
        String name = "spike at " + time.toString();
        int seconds = (int)Math.ceil(traceLength.convertTo(UnitImpl.SECOND)
                .getValue());
        int[] dataBits = new int[SPIKE_SAMPLES_PER_SECOND * seconds];
        for(int i = 0; i < dataBits.length; i++) {
            if(i % samplesPerSpike == 0 && i >= missingSamples) {
                dataBits[i] = 100;
            }
        }
        return createTestData(name, dataBits, time.getFissuresTime());
    }

    public static final int SPIKE_SAMPLES_PER_SECOND = 20;

    static Category logger = Category.getInstance(SimplePlotUtil.class.getName());
} // SimplePlotUtil
