package edu.sc.seis.fissuresUtil.chooser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnsupportedFormat;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * ClockUtil.java Created: Mon Mar 17 09:34:25 2003
 * 
 * @author Philip Crotwell
 */
public class ClockUtil {

    /**
     * Calculates the difference between the CPU clock and the time retrieved
     * from the http://www.seis.sc.edu/cgi-bin/date_time.pl. 
     */
    public static TimeInterval getTimeOffset() {
        if(serverOffset == null ) {
            if (warnServerFail ) {
                // already tried and failed, so...
                return ZERO_OFFSET;
            }
            try {
                serverOffset = getServerTimeOffset();
            } catch(Throwable e) {
            	noGoClock(e);
                return ZERO_OFFSET;
            } // end of try-catch
        } // end of if ()
        return serverOffset;
    }
    
    private static void noGoClock(Throwable e) {
        warnServerFail = true;
        // oh well, can't get to server, use CPU time, so
        // offset is zero, check for really bad clocks first
        logger.warn("Unable to make a connection to "+SEIS_SC_EDU_URL+" to verify system clock.", e);
        MicroSecondDate localNow = new MicroSecondDate();
        if(!warnBadBadClock && OLD_DATE.after(localNow)) {
            warnBadBadClock = true;
            GlobalExceptionHandler.handle("Unable to check the time from the server and the computer's clock is obviously wrong. Please reset the clock on your computer to be closer to real time. \nComputer Time="
                                                  + localNow
                                                  + "\nTime checking url="
                                                  + SEIS_SC_EDU_URL,
                                          e);
        }
    }

    /**
     * Creates a new MicroSecondDate that reflects the current time to the best
     * ability of the system. If a connection to a remote server cannot be
     * established, then the current CPU time is used.
     */
    public static MicroSecondDate now() {
        return new MicroSecondDate().add(getTimeOffset());
    }

    public static MicroSecondDate future() {
        return now().add(ONE_DAY);
    }

    public static TimeInterval getServerTimeOffset() throws IOException {
        InputStream is = SEIS_SC_EDU_URL.openStream();
        InputStreamReader isReader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(isReader);
        String str;
        String timeStr = null;
        while((str = bufferedReader.readLine()) != null) {
            timeStr = str;
        }
        MicroSecondDate localTime = new MicroSecondDate();
        edu.iris.Fissures.Time serverTime = new edu.iris.Fissures.Time();
        if(timeStr != null) {
            serverTime = new edu.iris.Fissures.Time(timeStr, -1);
        }
        return new TimeInterval(localTime, new MicroSecondDate(serverTime));
    }
    
    private static boolean warnServerFail = false;

    private static boolean warnBadBadClock = false;

    private static TimeInterval serverOffset = null;

    private static final TimeInterval ZERO_OFFSET = new TimeInterval(0,
                                                                     UnitImpl.SECOND);

    private static URL SEIS_SC_EDU_URL;
    static {
        // we have to do this in a static block because of the exception
        try {
            SEIS_SC_EDU_URL = new URL("http://www.seis.sc.edu/cgi-bin/date_time.pl");
        } catch(MalformedURLException e) {
            // Can't happen
            GlobalExceptionHandler.handle("Caught MalformedURL with seis data_time.pl URL. This should never happen.",
                                          e);
        } // end of try-catch
    }

    /** Used to check for really obviously wrong system clocks, set to a day prior to the release date. */
    private static MicroSecondDate OLD_DATE = new ISOTime("2007-08-01T00:00:00.000Z").getDate();

    private static TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ClockUtil.class);
} // ClockUtil
