package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;

/**
 * JDBCTime.java
 *
 *
 * Created: Fri Dec  8 14:22:49 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class JDBCTime extends JDBCTable{
    public JDBCTime() throws SQLException{this(ConnMgr.createConnection());}

    public JDBCTime(Connection conn) throws SQLException{
        super("time", conn);
        seq = new JDBCSequence(conn, "TimeSeq");
        if(!DBUtil.tableExists("time", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("time.create"));
        }
        getById = conn.prepareStatement("SELECT * FROM time WHERE time_id = ?");
        put = conn.prepareStatement("INSERT INTO time " +
                                        "(time_id, time_stamp, time_nanos, time_leapsec) " +
                                        "VALUES (?, ?, ?, ?)");
        getByValues = conn.prepareStatement("SELECT time_id FROM time WHERE time_stamp = ? " +
                                                "AND time_nanos = ? AND time_leapsec = ?");
    }

    public Time get(int dbid) throws SQLException, NotFound{
        getById.setInt(1, dbid);
        ResultSet rs = getById.executeQuery();
        if(rs.next()) return makeTime(rs.getTimestamp("time_stamp"), rs.getInt("time_nanos"),
                                      rs.getInt("time_leapsec"));
        throw new NotFound("No time for id " + dbid);
    }

    public int put(edu.iris.Fissures.Time time) throws SQLException{
        insert(time, getByValues, 1);
        ResultSet rs = getByValues.executeQuery();
        if(rs.next()) { return rs.getInt("time_id");
        }else{
            int dbid = seq.next();
            put.setInt(1, dbid);
            insert(time, put, 2);
            put.executeUpdate();
            return dbid;
        }
    }

    private PreparedStatement getById, getByValues, put;

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
    public static int insert(Time time, PreparedStatement stmt, int index)
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

    public static Time makeTime(Timestamp ts, int nanos, int leapsec) {
        // check for dates to far in future
        if (ts.after(future)) {
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

    private JDBCSequence seq;

} // JDBCTime
