package edu.sc.seis.fissuresUtil.database.plottable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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

    /**
     * Creates a plottable chunk consisting of the plottable in data, starting
     * start pixels into the jday and year of otherstuff at
     * otherstuff.getPixelsPerDay ppd.
     */
    public PlottableChunk(Plottable data, int startPixel,
            PlottableChunk otherStuff) {
        this(data,
             startPixel,
             otherStuff.getJDay(),
             otherStuff.getYear(),
             otherStuff.getPixelsPerDay(),
             otherStuff.getChannel());
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year of start data at pixelsPerDay
     */
    public PlottableChunk(Plottable data, int startPixel,
            MicroSecondDate startDate, int pixelsPerDay, ChannelId channel) {
        this(data,
             startPixel,
             getJDay(startDate),
             getYear(startDate),
             pixelsPerDay,
             channel);
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year at pixelsPerDay
     */
    public PlottableChunk(Plottable data, int startPixel, int jday, int year,
            int pixelsPerDay, ChannelId channel) {
        this.data = data;
        this.beginPixel = startPixel;
        this.pixelsPerDay = pixelsPerDay;
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
                if(pixelsPerDay == oChunk.pixelsPerDay) {
                    if(jday == oChunk.jday) {
                        if(year == oChunk.year) {
                            if(data.x_coor.length == oChunk.data.x_coor.length) {
                                for(int i = 0; i < data.x_coor.length; i++) {
                                    if(data.x_coor[i] != oChunk.data.x_coor[i]) { return false; }
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

    public static Calendar makeCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    }

    public static MicroSecondDate getTime(int pixel,
                                          int jday,
                                          int year,
                                          int pixelsPerDay) {
        Calendar cal = makeCal();
        cal.set(Calendar.DAY_OF_YEAR, jday);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        double sampleMillis = SimplePlotUtil.linearInterp(0,
                                                          0,
                                                          pixelsPerDay,
                                                          MILLIS_IN_DAY,
                                                          pixel);
        sampleMillis = Math.floor(sampleMillis);
        return new MicroSecondDate((cal.getTimeInMillis() + (long)sampleMillis) * 1000);
    }

    public static int getJDay(MicroSecondDate time) {
        Calendar cal = makeCal();
        cal.setTime(time);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static int getYear(MicroSecondDate time) {
        Calendar cal = makeCal();
        cal.setTime(time);
        return cal.get(Calendar.YEAR);
    }

    public static int getPixel(MicroSecondDate time, int pixelsPerDay) {
        MicroSecondDate day = new MicroSecondDate(stripToDay(time));
        MicroSecondTimeRange tr = new MicroSecondTimeRange(day, ONE_DAY);
        double pixel = SimplePlotUtil.getPixel(pixelsPerDay, tr, time);
        return (int)Math.floor(pixel);
    }

    public static MicroSecondDate stripToDay(Date d) {
        Calendar cal = makeCal();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new MicroSecondDate(cal.getTime());
    }

    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    public ChannelId getChannel() {
        return channel;
    }

    public Plottable getData() {
        return data;
    }

    public int getPixelsPerDay() {
        return pixelsPerDay;
    }

    public int getBeginPixel() {
        return beginPixel;
    }

    public int getNumPixels() {
        return data.y_coor.length / 2;
    }

    public MicroSecondDate getTime(int pixel) {
        return getTime(pixel, getJDay(), getYear(), getPixelsPerDay());
    }

    public MicroSecondDate getBeginTime() {
        return getTime(beginPixel);
    }

    public MicroSecondDate getEndTime() {
        return getTime(getBeginPixel() + getNumPixels());
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
        hashCode = 37 * hashCode + pixelsPerDay;
        hashCode = 37 * hashCode + jday;
        hashCode = 37 * hashCode + year;
        return 37 * hashCode + data.y_coor.length;
    }

    public String toString() {
        return getNumPixels() + " pixel chunk from "
                + ChannelIdUtil.toStringNoDates(channel) + " at "
                + pixelsPerDay + " ppd from " + getTimeRange();
    }

    public PlottableChunk[] breakIntoDays() {
        int numDays = (int)Math.ceil(getNumPixels() / (double)getPixelsPerDay());
        if(getNumPixels() % getPixelsPerDay() == 0 && beginPixel != 0) {
            numDays++;
        }
        List dayChunks = new ArrayList();
        MicroSecondDate time = getBeginTime();
        for(int i = 0; i < numDays; i++) {
            int firstDayPixels = pixelsPerDay - getBeginPixel();
            int startPixel = (i - 1) * pixelsPerDay + firstDayPixels;
            int stopPixel = i * pixelsPerDay + firstDayPixels;
            int pixelIntoNewDay = 0;
            if(i == 0) {
                startPixel = 0;
                stopPixel = firstDayPixels;
                pixelIntoNewDay = getBeginPixel();
            }
            if(i == numDays - 1) {
                stopPixel = getNumPixels();
            }
            int[] y = new int[(stopPixel - startPixel) * 2];
            System.arraycopy(data.y_coor, startPixel * 2, y, 0, y.length);
            Plottable p = new Plottable(null, y);
            dayChunks.add(new PlottableChunk(p,
                                             pixelIntoNewDay,
                                             getJDay(time),
                                             getYear(time),
                                             getPixelsPerDay(),
                                             getChannel()));
            time = time.add(ONE_DAY);
        }
        return (PlottableChunk[])dayChunks.toArray(new PlottableChunk[0]);
    }

    private ChannelId channel;

    private Plottable data;

    private int pixelsPerDay, beginPixel;

    private int jday, year;
}