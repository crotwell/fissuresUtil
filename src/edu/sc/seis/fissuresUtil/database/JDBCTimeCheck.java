package edu.sc.seis.fissuresUtil.database;


import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import java.sql.Timestamp;

public class JDBCTimeCheck extends JDBCTime {
    public static boolean compareTwo(Time time1, Time time2) {
        Timestamp ts1, ts2;
        int nanosec1 = 0, nanosec2 = 0;
        int leapsec1 = 0, leapsec2 = 0;
        
        if(time1.date_time.equals(timeUnknown.date_time)) {
            ts1 = futurePlusOne.getTimestamp();
        } else {
            MicroSecondDate ms = new MicroSecondDate(time1);
            ts1  = ms.getTimestamp();
            nanosec1 = ts1.getNanos();
            leapsec1 = time1.leap_seconds_version;
        }
        
        if(time2.date_time.equals(timeUnknown.date_time)) {
            ts2 = futurePlusOne.getTimestamp();
        } else {
            MicroSecondDate ms = new MicroSecondDate(time2);
            ts2  = ms.getTimestamp();
            nanosec2 = ts2.getNanos();
            leapsec2 = time2.leap_seconds_version;
        }
        
        if( !ts1.equals(ts2) ) {
            if( ts1.before(ts2) ) return true;
            else if(ts1.after(ts2)) return false;
        } else {
            if( nanosec1 != nanosec2 ) {
                if( nanosec1 > nanosec2 ) return true;
                else return false;
            } else {
                if( leapsec1 != leapsec2 ) {
                    if( leapsec1 > leapsec2 ) return true;
                    else return false;
                } else return true;
            }
        }
        return false;
    }
    
    public static boolean checkInRange( Time t1, Time t2, Time t3) {
        if(compareTwo(t3 , t1) && compareTwo(t2, t3)) return true;
        return false;
    }
    
    public static Timestamp getTimeStamp(Time time) {
        if(time.date_time.equals(timeUnknown.date_time)) {
            return futurePlusOne.getTimestamp();
        }
        MicroSecondDate ms = new MicroSecondDate(time);
        return ms.getTimestamp();
    }
} // JDBCTimeCheck
