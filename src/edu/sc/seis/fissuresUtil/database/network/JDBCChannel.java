
package edu.sc.seis.fissuresUtil.database.network;

import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 * JDBCChannel.java
 *
 * All methods are unsyncronized, the calling application should make sure
 * that a single instance of this class is not accessed by more than one
 * thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE!
 *
 * Created: Thu Jan 25 13:46:02 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class JDBCChannel extends NetworkTable {
    public JDBCChannel() throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCChannel(Connection conn)throws SQLException{
        this(conn, new JDBCQuantity(conn), new JDBCSite(conn), new JDBCTime(conn));
    }


    public JDBCChannel(Connection conn, JDBCQuantity quantityTable, JDBCSite siteTable,
                       JDBCTime time) throws SQLException{
        super("channel", conn);
        this.quantityTable = quantityTable;
        this.time = time;
        this.siteTable = siteTable;
        seq = new JDBCSequence(conn, "ChannelSeq");
        if(!DBUtil.tableExists("channel", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("channel.create"));
        }
        putAll = conn.prepareStatement("INSERT INTO channel (chan_id, site_id, chan_code, "+
                                           "chan_begin_id, chan_end_id, "+
                                           "chan_name, chan_orientation_az, "+
                                           "chan_orientation_dip, chan_sampling_interval_id, chan_sampling_numpoints) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        String getAllQuery = "SELECT chan_id, site_id, chan_code, chan_begin_id FROM channel";
        getAll = conn.prepareStatement(getAllQuery);
        getAllForSite = conn.prepareStatement(getAllQuery + " WHERE site_id = ?");
        getIfNameExists = conn.prepareStatement("SELECT chan_id FROM channel " +
                                                    "WHERE chan_id = ? AND " +
                                                    "chan_name IS NOT NULL");
        getByDBId = conn.prepareStatement("SELECT * FROM channel WHERE chan_id = ?");
        getDBId = conn.prepareStatement("SELECT chan_id FROM channel WHERE site_id = ? AND " +
                                            "chan_code = ? AND chan_begin_id = ?");
    }

    public int put(Channel chan)  throws SQLException {
        int dbid;
        try {
            dbid = getDBId(chan.get_id(), chan.my_site);
            // No NotFound exception, so already added the channel
        } catch (NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(chan, putAll, 2, siteTable, quantityTable, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    public Channel get(int dbid)  throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if (rs.next()){ return extract(rs, siteTable, time, quantityTable);}
        throw new NotFound("No Channel found for database id = "+dbid);
    }

    public Channel get(ChannelId id, Site site)throws SQLException, NotFound {
        return get(getDBId(id, site));
    }

    public int getDBId(ChannelId id, Site site)  throws SQLException, NotFound {
        insertId(id, site, getDBId, 1, siteTable, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()){ return rs.getInt("chan_id"); }
        throw new NotFound("No such channel id in the db");
    }

    public static Channel extract(ResultSet rs, JDBCSite siteTable,
                                  JDBCTime time, JDBCQuantity quantityTable)
        throws SQLException, NotFound {
        Orientation ori = new Orientation(rs.getFloat("chan_orientation_az"),
                                          rs.getFloat("chan_orientation_dip"));
        TimeInterval t = new TimeInterval(quantityTable.get(rs.getInt("chan_sampling_interval_id")));
        Sampling sampling = new SamplingImpl(rs.getInt("chan_sampling_numpoints"), t);
        ChannelId id = extractId(rs,siteTable, time);
        return new ChannelImpl(id, rs.getString("chan_name"),
                               ori, sampling,
                               new TimeRange(id.begin_time,
                                             time.get(rs.getInt("chan_end_id"))),
                               siteTable.get(rs.getInt("site_id")));
    }

    public static ChannelId extractId(ResultSet rs, JDBCSite siteTable,
                                      JDBCTime time) throws SQLException{
        try {
            SiteId siteId = siteTable.get(rs.getInt("site_id")).get_id();
            return new ChannelId(siteId.network_id, siteId.station_code,
                                 siteId.site_code, rs.getString("chan_code"),
                                 time.get(rs.getInt("chan_begin_id")));
        }catch (NotFound e) {
            throw new RuntimeException(e);
        }
    }

    public static int insertAll(Channel chan, PreparedStatement stmt, int index,
                                JDBCSite siteTable, JDBCQuantity quantityTable,
                                JDBCTime time)throws SQLException {
        index = insertId(chan.get_id(), chan.my_site, stmt, index,
                         siteTable, time);
        index = insertOnlyChannel(chan, stmt, index, quantityTable, time);
        return index;
    }

    public static int insertOnlyChannel(Channel chan, PreparedStatement stmt,
                                        int index, JDBCQuantity quantityTable,
                                        JDBCTime time)throws SQLException{
        stmt.setInt(index++, time.put(chan.effective_time.end_time));
        stmt.setString(index++, chan.name);
        stmt.setFloat(index++, chan.an_orientation.azimuth);
        stmt.setFloat(index++, chan.an_orientation.dip);
        stmt.setInt(index++, quantityTable.put(chan.sampling_info.interval));
        stmt.setInt(index++, chan.sampling_info.numPoints);
        return index;
    }

    public static int insertId(ChannelId id, Site site, PreparedStatement stmt, int index,
                               JDBCSite siteTable, JDBCTime time) throws SQLException{
        stmt.setInt(index++, siteTable.put(site));
        stmt.setString(index++, id.channel_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;

    }

    private PreparedStatement getAll, getAllForSite, getIfNameExists, getByDBId,
        getDBId, updateSite, putAll;
    private JDBCSequence seq;
    private JDBCQuantity quantityTable;
    private JDBCSite siteTable;
    private JDBCTime time;

    private static final Logger logger = Logger.getLogger(JDBCChannel.class);

} // JDBCChannel
