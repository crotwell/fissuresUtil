package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.Category;

/**
 * ClockUtil.java
 *
 *
 * Created: Mon Mar 17 09:34:25 2003
 *
 * @author <a href="mailto:crotwell@owl-seis-sc-edu.local.">Philip Crotwell</a>
 * @version 1.0
 */
public class ClockUtil {
    
    /** Calculates the difference between the CPU clock and the time retreived
        from the server. This uses a simple cgi-bin script located at
        http://www.seis.sc.edu/cgi-bin/date_time.pl. The URL can be overridden
        with the setTimeURL method.
    */
    public static TimeInterval getTimeOffset() {
        if (serverOffset == null) {
            try {
                serverOffset = getServerTimeOffset();
            } catch (IOException e) {
                // oh well, cant get to server, use CPU time, so
                // offset is zero
                return ZERO_OFFSET;
            } // end of try-catch
            
        } // end of if ()
        return serverOffset;
    }

    /** Creates a new MicroSecondDate that reflects the current time to
     *  the best ability of the system. If a connection to a remote server
     *  cannot be estabilished, then the current CPU time is used.
     */
    public static MicroSecondDate now() {
        return new MicroSecondDate().add(getTimeOffset());
    }
    
    public static MicroSecondDate future() { return now().add(ONE_DAY); }

    /** Sets the URL used to get a time. The format of the string returned
     *  from the URL must correspond to the form at
     *  http://www.seis.sc.edu/cgi-bin/date_time.pl.
     */
    public static void setTimeURL(URL newTimeURL) {
        timeURL = newTimeURL;
        serverOffset = null;
    }
         
    public static TimeInterval getServerTimeOffset() throws IOException {
        if ( timeURL == null) {
            setTimeURL(SEIS_SC_EDU_URL);
        } // end of if ()
        
        URL url = timeURL;
        InputStream is = url.openStream();
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
        MicroSecondDate serverDate = new MicroSecondDate(serverTime);
        TimeInterval offset = new TimeInterval(localTime, serverDate);

        // assume small time
        //if(java.lang.Math.abs(offset.value) <  2000000) offset = new TimeInterval(serverDate, serverDate);
        return offset;
    }

    private static TimeInterval serverOffset = null;

    private static final TimeInterval ZERO_OFFSET = new TimeInterval(0,
                                                                     UnitImpl.SECOND);

    private static URL SEIS_SC_EDU_URL;

    private static Category logger = Category.getInstance(ClockUtil.class.getName());

    static {
        // we have to do this in a static block because of the exception
        try {
            SEIS_SC_EDU_URL =
                new URL("http://www.seis.sc.edu/cgi-bin/date_time.pl");
        } catch (MalformedURLException e) {
            // Can't happen
            logger.error("Caught MalformedURL with seis data_time.pl URL. This should never happen.", e);
        } // end of try-catch
    }
    
    private static TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);

    private static URL timeURL = null;

} // ClockUtil
