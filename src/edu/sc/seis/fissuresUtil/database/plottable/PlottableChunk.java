package edu.sc.seis.fissuresUtil.database.plottable;

import java.util.Calendar;
import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Oct 18, 2004
 */
public class PlottableChunk {

    public PlottableChunk(Plottable data, int startSample,
            PlottableChunk otherStuff) {
        this(data,
             startSample,
             otherStuff.getJDay(),
             otherStuff.getYear(),
             otherStuff.getSamplesPerDay(),
             otherStuff.getChannel());
    }

    public PlottableChunk(Plottable data, int startSample,
            MicroSecondDate startDate, int samplesPerDay, ChannelId channel) {
        this(data,
             startSample,
             getJDay(startDate),
             getYear(startDate),
             samplesPerDay,
             channel);
    }

    public PlottableChunk(Plottable data, int startSample, int jday, int year,
            int samplesPerDay, ChannelId channel) {
        this.data = data;
        this.beginSample = startSample;
        this.samplesPerDay = samplesPerDay;
        this.jday = jday;
        this.year = year;
        this.channel = channel;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof PlottableChunk) {
            PlottableChunk oChunk = (PlottableChunk)o;
            if(ChannelIdUtil.areEqual(channel, oChunk.channel)) {
                if(samplesPerDay == oChunk.samplesPerDay) {
                    if(jday == oChunk.jday) {
                        if(year == oChunk.year) {
                            if(data.x_coor.length == oChunk.data.x_coor.length) {
                                for(int i = 0; i < data.x_coor.length; i++) {
                                    if(data.x_coor[i] != oChunk.data.x_coor[i]) {
                                        System.out.println(i + " "
                                                + data.x_coor[i] + " "
                                                + oChunk.data.x_coor[i]);
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static MicroSecondDate getTime(int sample,
                                          int jday,
                                          int year,
                                          int samplesPerDay) {
        Calendar cal = JDBCPlottable.makeCal();
        cal.set(Calendar.DAY_OF_YEAR, jday);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        double sampleMillis = SimplePlotUtil.linearInterp(0,
                                                          0,
                                                          samplesPerDay,
                                                          MILLIS_IN_DAY,
                                                          sample);
        sampleMillis = Math.floor(sampleMillis);
        return new MicroSecondDate((cal.getTimeInMillis() + (long)sampleMillis) * 1000);
    }

    public static int getJDay(MicroSecondDate time) {
        Calendar cal = JDBCPlottable.makeCal();
        cal.setTime(time);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static int getYear(MicroSecondDate time) {
        Calendar cal = JDBCPlottable.makeCal();
        cal.setTime(time);
        return cal.get(Calendar.YEAR);
    }

    public static int getSample(MicroSecondDate time, int samplesPerDay) {
        Calendar cal = JDBCPlottable.makeCal();
        cal.setTime(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        MicroSecondDate day = new MicroSecondDate(cal.getTime());
        MicroSecondTimeRange tr = new MicroSecondTimeRange(day, ONE_DAY);
        double pixel = SimplePlotUtil.getPixel(samplesPerDay, tr, time);
        return (int)Math.floor(pixel);
    }

    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    private static final TimeInterval ONE_DAY = new TimeInterval(1,
                                                                 UnitImpl.DAY);

    public ChannelId getChannel() {
        return channel;
    }

    public Plottable getData() {
        return data;
    }

    public int getSamplesPerDay() {
        return samplesPerDay;
    }

    public int getBeginSample() {
        return beginSample;
    }

    public int getEndSample() {
        return beginSample + data.y_coor.length;
    }

    public MicroSecondDate getTime(int sample) {
        return getTime(sample, getJDay(), getYear(), getSamplesPerDay());
    }

    public MicroSecondDate getBeginTime() {
        return getTime(beginSample);
    }

    public MicroSecondDate getEndTime() {
        return getTime(getEndSample());
    }

    public MicroSecondTimeRange getTimeRange() {
        return new MicroSecondTimeRange(getBeginTime(), getEndTime());
    }

    public int getJDay() {
        return jday;
    }

    public int getYear() {
        return year;
    }

    public int hashCode() {
        int hashCode = 81 + ChannelIdUtil.hashCode(getChannel());
        hashCode = 37 * hashCode + samplesPerDay;
        hashCode = 37 * hashCode + jday;
        hashCode = 37 * hashCode + year;
        return 37 * hashCode + data.y_coor.length;
    }

    public String toString() {
        return data.y_coor.length + " point chunk from "
                + ChannelIdUtil.toStringNoDates(channel) + " at "
                + samplesPerDay + " spd from " + getTimeRange();
    }

    public PlottableChunk[] breakIntoDays() {
        int numDays = (int)Math.ceil(getData().y_coor.length
                / getSamplesPerDay());
        PlottableChunk[] dayChunks = new PlottableChunk[numDays];
        MicroSecondDate time = getBeginTime();
        for(int i = 0; i < dayChunks.length; i++) {
            int copyStartPoint = i * getSamplesPerDay();
            int newChunkStartPoint = 0;
            if(i == 0) {
                copyStartPoint = getBeginSample();
                newChunkStartPoint = getBeginSample();
            }
            int endOfDaySample = (i + 1) * getSamplesPerDay();
            int copyEndPoint = endOfDaySample;
            if(endOfDaySample > getEndSample()) {
                copyEndPoint = getEndSample();
            }
            int[] y = new int[copyEndPoint - copyStartPoint];
            System.arraycopy(getData().y_coor, copyStartPoint, y, 0, y.length);
            Plottable p = new Plottable(null, y);
            dayChunks[i] = new PlottableChunk(p,
                                              newChunkStartPoint,
                                              getJDay(time),
                                              getYear(time),
                                              getSamplesPerDay(),
                                              getChannel());
            time = time.add(ONE_DAY);
        }
        return dayChunks;
    }

    private ChannelId channel;

    private Plottable data;

    private int samplesPerDay, beginSample;

    private int jday, year;
}