package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.utility.Logger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * JDBCTime.java
 *
 *
 * Created: Fri Dec  8 14:22:49 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class JDBCTime  {
    /** Puts the attributes of a Fissures Time object into a prepared
     statement starting at index. THis assumes that the prepared
     statement has the following in order:<break>
     index   Timestamp timestamp <break>
     +1      int       nanos<break>
     +2      int       leap_seconds<break>
     JDBCTime is slightly different than the other JDBC classes here as
     it is expected that the times will be part of other tables instead
     of in a separate table.
     
     @returns the next index after the ones used by the time
     */
    public static int insert(Time time,
                             PreparedStatement stmt,
                             int index)
        throws SQLException {
        
        Timestamp ts;
        if (time.date_time.equals(timeUnknown.date_time)) {
            // got unknown time, likely part of a time range that hasn't ended
            // use default future time
            ts = futurePlusOne.getTimestamp();
        } else {
            MicroSecondDate ms = new MicroSecondDate(time);
            ts = ms.getTimestamp();
        }
        int nanos = ts.getNanos();
        ts.setNanos(0);
        stmt.setTimestamp(index++, ts);
        stmt.setInt(index++, nanos);
        stmt.setInt(index++, time.leap_seconds_version);
        return index;
    }
    
    public static edu.iris.Fissures.Time makeTime(Timestamp ts,
                                                  int nanos,
                                                  int leapsec) {
        // check for dates to far in future
        if (ts.after(future)) {
            Logger.log("Unknown time: "+ts);
            return timeUnknown;
        }
        ts = new Timestamp(ts.getTime());
        ts.setNanos(nanos);
        MicroSecondDate micro = new MicroSecondDate(ts, leapsec);
        return micro.getFissuresTime();
    }
    
    public static final MicroSecondDate future =
        edu.iris.Fissures.model.TimeUtils.future;
    
    /** future plus one day so that is is after(future)
     */
    public static final MicroSecondDate futurePlusOne =
        edu.iris.Fissures.model.TimeUtils.futurePlusOne;
    
    public static final edu.iris.Fissures.Time timeUnknown =
        edu.iris.Fissures.model.TimeUtils.timeUnknown;
    
} // JDBCTime
