package edu.sc.seis.fissuresUtil.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.dmc.seedcodec.Codec;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Oct 18, 2004
 */
public class PlottableChunk {

    /** for hibernate */
    protected PlottableChunk() {}

    /**
     * Creates a plottable chunk consisting of the plottable in data, starting
     * start pixels into the jday and year of otherstuff at
     * otherstuff.getPixelsPerDay ppd.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          PlottableChunk otherStuff) {
        this(data,
             startPixel,
             otherStuff.getJDay(),
             otherStuff.getYear(),
             otherStuff.getPixelsPerDay(),
             otherStuff.getNetworkCode(),
             otherStuff.getStationCode(),
             otherStuff.getSiteCode(),
             otherStuff.getChannelCode());
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year of start data at pixelsPerDay
     * NOTE: The start pixel should be relative to the beginning of the jday of
     * the start date. Otherwise, things get screwy.
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          MicroSecondDate startDate,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this(data,
             startPixel,
             getJDay(startDate),
             getYear(startDate),
             pixelsPerDay,
             networkCode,
             stationCode,
             siteCode,
             channelCode);
    }

    /**
     * Creates a plottable chunk based on the plottable in data, starting
     * startPixel pixels into the jday and year at pixelsPerDay
     */
    public PlottableChunk(Plottable data,
                          int startPixel,
                          int jday,
                          int year,
                          int pixelsPerDay,
                          String networkCode,
                          String stationCode,
                          String siteCode,
                          String channelCode) {
        this.data = data;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(out));
            for(int k = 0; k < data.y_coor.length; k++) {
                dos.writeInt(data.y_coor[k]);
            }
            dos.close();
        } catch(IOException e) {
            throw new RuntimeException("Should never happen with a ByteArrayOutputStream", e);
        }
        yBytes = out.toByteArray();
        
        // here we shall get rid of days of dead space if they exist
        if(startPixel >= pixelsPerDay) {
            int numDaysToAdd = startPixel / pixelsPerDay;
            MicroSecondDate date = getDate(jday, year);
            date = date.add(new TimeInterval(new TimeInterval(1.0, UnitImpl.DAY).multiplyBy(numDaysToAdd)));
            jday = getJDay(date);
            year = getYear(date);
            startPixel = startPixel % pixelsPerDay;
        }
        this.beginPixel = startPixel;
        this.pixelsPerDay = pixelsPerDay;
        this.numDataPoints = data.y_coor.length;
        this.jday = jday;
        this.year = year;
        this.networkCode = networkCode;
        this.stationCode = stationCode;
        this.siteCode = siteCode;
        this.channelCode = channelCode;
    }

    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof PlottableChunk) {
            PlottableChunk oChunk = (PlottableChunk)o;
            if(networkCode.equals(oChunk.networkCode) &&
                    stationCode.equals(oChunk.stationCode) &&
                    siteCode.equals(oChunk.siteCode) &&
                    channelCode.equals(oChunk.channelCode) &&
                    pixelsPerDay == oChunk.pixelsPerDay &&
                    jday == oChunk.jday &&
                    year == oChunk.year &&
                    getNumDataPoints() == oChunk.getNumDataPoints()) {
                return true;
            }
        }
        return false;
    }

    public static Calendar makeCal() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }

    public static Calendar makeCalWithDate(int jday, int year) {
        Calendar cal = makeCal();
        cal.set(Calendar.DAY_OF_YEAR, jday);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static MicroSecondDate getDate(int jday, int year) {
        return new MicroSecondDate(makeCalWithDate(jday, year).getTimeInMillis() * 1000);
    }

    public static MicroSecondDate getTime(int pixel,
                                          int jday,
                                          int year,
                                          int pixelsPerDay) {
        Calendar cal = makeCalWithDate(jday, year);
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

    public Plottable getData() {
        synchronized(this) {
            if (data == null) {
                int[] yValues = getYData();
                int[] xValues = new int[yValues.length];
                for (int i = 0; i < xValues.length; i++) {
                    xValues[i] = i/2;
                }
                data = new Plottable(xValues, yValues);
            }
        }
        return data;
    }

    public int getPixelsPerDay() {
        return pixelsPerDay;
    }

    public int getBeginPixel() {
        return beginPixel;
    }

    public int getNumPixels() {
        return getNumDataPoints() / 2;
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
        int hashCode = 81 + networkCode.hashCode();
        hashCode = 37 * hashCode + stationCode.hashCode();
        hashCode = 37 * hashCode + siteCode.hashCode();
        hashCode = 37 * hashCode + channelCode.hashCode();
        hashCode = 37 * hashCode + pixelsPerDay;
        hashCode = 37 * hashCode + jday;
        hashCode = 37 * hashCode + year;
        return 37 * hashCode + getNumDataPoints();
    }

    public String toString() {
        return getNumPixels() + " pixel chunk from "
                + networkCode + "."
                + stationCode + "." + siteCode + "." + channelCode + " at "
                + pixelsPerDay + " ppd from " + getTimeRange();
    }

    public List<PlottableChunk> breakIntoDays() {
        int numDays = (int)Math.ceil((beginPixel + getNumPixels())
                / (double)getPixelsPerDay());
        List<PlottableChunk> dayChunks = new ArrayList<PlottableChunk>();
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
            System.arraycopy(getYData(), startPixel * 2, y, 0, y.length);
            Plottable p = new Plottable(null, y);
            dayChunks.add(new PlottableChunk(p,
                                             pixelIntoNewDay,
                                             getJDay(time),
                                             getYear(time),
                                             getPixelsPerDay(),
                                             getNetworkCode(),
                                             getStationCode(),
                                             getSiteCode(),
                                             getChannelCode()));
            time = time.add(ONE_DAY);
        }
        return dayChunks;
    }

    // hibernate

    protected void setData(Plottable data) {
        this.data = data;
    }

    protected void setPixelsPerDay(int pixelsPerDay) {
        this.pixelsPerDay = pixelsPerDay;
    }

    protected void setBeginPixel(int beginPixel) {
        this.beginPixel = beginPixel;
    }

    protected void setJday(int jday) {
        this.jday = jday;
    }

    protected void setYear(int year) {
        this.year = year;
    }

    public long getDbid() {
        return dbid;
    }

    protected void setDbid(long dbid) {
        this.dbid = dbid;
    }
    
    protected Timestamp getBeginTimestamp() {
        return getBeginTime().getTimestamp();
    }
    
    protected void setBeginTimestamp(Timestamp begin) {
        MicroSecondDate msd = new MicroSecondDate(begin);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(msd);
        year = cal.get(Calendar.YEAR);
        jday = cal.get(Calendar.DAY_OF_YEAR);
    }
    
    protected Timestamp getEndTimestamp() {
        return getEndTime().getTimestamp();
    }
    
    protected void setEndTimestamp(Timestamp begin) {
        // this is generated from begin and pixels, so no need to set
        // have this method as a no-op so hibernate doesn't get mad
    }

    private long dbid;

    private String networkCode;
    private String stationCode;
    private String siteCode;
    private String channelCode;

    private byte[] yBytes;
    
    private transient Plottable data = null;

    private int pixelsPerDay, beginPixel, numDataPoints;

    private int jday, year;

    private static final Logger logger = LoggerFactory.getLogger(PlottableChunk.class);

    
    public String getNetworkCode() {
        return networkCode;
    }

    
    public void setNetworkCode(String networkCode) {
        this.networkCode = networkCode;
    }

    
    public String getStationCode() {
        return stationCode;
    }

    
    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    
    public String getSiteCode() {
        return siteCode;
    }

    
    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    
    public String getChannelCode() {
        return channelCode;
    }

    
    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public byte[] getYBytes() {
        return yBytes;
    }

    protected void setYBytes(byte[] bytes) {
        yBytes = bytes;
    }
    
    public int[] getYData() {
        return toIntArray(getYBytes());
    }
    
    private int[] toIntArray(byte[] bytes) {
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            int[] decomp = new int[getNumDataPoints()];
            for (int i = 0; i < decomp.length; i++) {
                decomp[i] = dis.readInt();
            }
            return decomp;
        } catch(IOException e) {
            // cant happen
            throw new RuntimeException("Should never happen", e);
        }
    }

    
    public int getNumDataPoints() {
        return numDataPoints;
    }

    
    protected void setNumDataPoints(int numDataPoints) {
        this.numDataPoints = numDataPoints;
    }
}
