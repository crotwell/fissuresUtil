package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCQuantity;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

/**
 * JDBCChannel.java All methods are unsyncronized, the calling application
 * should make sure that a single instance of this class is not accessed by more
 * than one thread at a time. Because of the use of prepared statements and a
 * single connection per instance, this class IS NOT THREAD-SAFE! Created: Thu
 * Jan 25 13:46:02 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class JDBCChannel extends NetworkTable {

    public JDBCChannel() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCChannel(Connection conn) throws SQLException {
        this(conn,
             new JDBCQuantity(conn),
             new JDBCSite(conn),
             new JDBCTime(conn));
    }

    public JDBCChannel(Connection conn,
                       JDBCQuantity quantityTable,
                       JDBCSite siteTable,
                       JDBCTime time) throws SQLException {
        super("channel", conn);
        this.quantityTable = quantityTable;
        this.time = time;
        this.siteTable = siteTable;
        stationTable = siteTable.getStationTable();
        netTable = stationTable.getNetTable();
        seq = new JDBCSequence(conn, "ChannelSeq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/props/network/default.props");
    }

    private ChannelId[] extractAllChanIds(PreparedStatement query)
            throws SQLException {
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        while(rs.next()) {
            aList.add(extractId(rs, siteTable, time));
        }
        return (ChannelId[])aList.toArray(new ChannelId[aList.size()]);
    }

    public DBChannel[] extractAllChans(PreparedStatement query)
            throws SQLException, NotFound {
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        while(rs.next()) {
            aList.add(extract(rs, siteTable, time, quantityTable));
        }
        return (DBChannel[])aList.toArray(new DBChannel[aList.size()]);
    }

    public DBChannel get(ChannelId id) throws SQLException, NotFound {
        return get(getDBId(id));
    }

    public ChannelId getId(int dbid) throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if(rs.next()) {
            return extractId(rs, siteTable, time);
        }
        throw new NotFound("No ChannelId found for database id = " + dbid);
    }

    public DBChannel get(int dbid) throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if(rs.next()) {
            return extract(rs, siteTable, time, quantityTable);
        }
        throw new NotFound("No Channel found for database id = " + dbid);
    }

    public int getStationDbId(int channelDbId) throws SQLException, NotFound {
        getStationDbId.setInt(1, channelDbId);
        ResultSet rs = getStationDbId.executeQuery();
        if(rs.next()) {
            return rs.getInt("sta_id");
        }
        throw new NotFound("No such channel " + channelDbId);
    }

    public ChannelId[] getAllChannelIds() throws SQLException {
        return extractAllChanIds(getAllIds);
    }

    public ChannelId[] getAllChannelIds(NetworkId network) throws NotFound,
            SQLException {
        int net_id = netTable.getDbId(network);
        getAllIdsForNetwork.setInt(1, net_id);
        return extractAllChanIds(getAllIdsForNetwork);
    }

    public ChannelId[] getAllChannelIds(StationId station) throws NotFound,
            SQLException {
        int sta_id = stationTable.getDBId(station);
        getAllIdsForStation.setInt(1, sta_id);
        return extractAllChanIds(getAllIdsForStation);
    }

    public ChannelId[] getAllChannelIdsForSiteDbId(int siteDbId)
            throws SQLException {
        getAllIdsForSite.setInt(1, siteDbId);
        return extractAllChanIds(getAllIdsForSite);
    }

    public DBChannel[] getAllChannels() throws NotFound, SQLException {
        return extractAllChans(getAllChans);
    }

    public DBChannel[] getAllChannels(NetworkId network) throws NotFound,
            SQLException {
        int net_id = netTable.getDbId(network);
        getAllChansForNetwork.setInt(1, net_id);
        return extractAllChans(getAllChansForNetwork);
    }

    public DBChannel[] getAllChannels(StationId station) throws NotFound,
            SQLException {
        return getAllChannelsForStation(stationTable.getDBId(station));
    }

    public DBChannel[] getAllChannelsForStation(int stationDbId)
            throws NotFound, SQLException {
        getAllChansForStation.setInt(1, stationDbId);
        return extractAllChans(getAllChansForStation);
    }

    public DBChannel[] getAllChannelsForStation(int stationDbId,
                                                MicroSecondDate activeAt)
            throws NotFound, SQLException {
        getAllChansForStationOnDate.setInt(1, stationDbId);
        getAllChansForStationOnDate.setTimestamp(2, activeAt.getTimestamp());
        getAllChansForStationOnDate.setTimestamp(3, activeAt.getTimestamp());
        return extractAllChans(getAllChansForStationOnDate);
    }

    public DBChannel[] getFirstChannel(StationId station) throws NotFound,
            SQLException {
        return getFirstChannelForStation(stationTable.getDBId(station));
    }

    public DBChannel[] getFirstChannel(StationId station, MicroSecondDate date)
            throws NotFound, SQLException {
        return getFirstChannelForStation(stationTable.getDBId(station), date);
    }

    public DBChannel[] getFirstChannelForStation(int stationDbId)
            throws NotFound, SQLException {
        getFirstChanForStation.setInt(1, stationDbId);
        return extractAllChans(getFirstChanForStation);
    }

    public DBChannel[] getFirstChannelForStation(int stationDbId,
                                                 MicroSecondDate activeAt)
            throws NotFound, SQLException {
        getFirstChanForStationOnDate.setInt(1, stationDbId);
        getFirstChanForStationOnDate.setTimestamp(2, activeAt.getTimestamp());
        getFirstChanForStationOnDate.setTimestamp(3, activeAt.getTimestamp());
        return extractAllChans(getFirstChanForStationOnDate);
    }

    public DBChannel[] getByCode(NetworkId networkId,
                                 String station_code,
                                 String site_code,
                                 String channel_code) throws SQLException,
            NotFound {
        int net_id = netTable.getDbId(networkId);
        int index = 1;
        getByCodes.setInt(index++, net_id);
        getByCodes.setString(index++, station_code);
        getByCodes.setString(index++, site_code);
        getByCodes.setString(index++, channel_code);
        return extractAllChans(getByCodes);
    }

    public ChannelId[] getIdsByCode(NetworkId networkId,
                                    String station_code,
                                    String site_code,
                                    String channel_code) throws SQLException,
            NotFound {
        int net_id = netTable.getDbId(networkId);
        int index = 1;
        getIdsByCodes.setInt(index++, net_id);
        getIdsByCodes.setString(index++, station_code);
        getIdsByCodes.setString(index++, site_code);
        getIdsByCodes.setString(index++, channel_code);
        ResultSet rs = getIdsByCodes.executeQuery();
        List aList = new ArrayList();
        while(rs.next()) {
            try {
                aList.add(new ChannelId(networkId,
                                        station_code,
                                        site_code,
                                        channel_code,
                                        time.get(rs.getInt("chan_begin_id"))));
            } catch(NotFound e) {
                throw new RuntimeException(e);
            }
        }
        return (ChannelId[])aList.toArray(new ChannelId[aList.size()]);
    }

    public int[] getDBIdIgnoringNetworkId(String network_code,
                                          String station_code,
                                          String site_code,
                                          String channel_code)
            throws SQLException, NotFound {
        String query = "SELECT chan_id FROM channel, site, station, network WHERE chan_code = '"
                + channel_code
                + "'"
                + " AND channel.site_id = site.site_id AND site_code = '"
                + site_code
                + "'"
                + " AND site.sta_id = station.sta_id AND sta_code = '"
                + station_code
                + "'"
                + " AND station.net_id = network.net_id AND net_code = '"
                + network_code + "'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List resultList = new ArrayList();
        if(rs.next()) {
            resultList.add(new Integer(rs.getInt("chan_id")));
            while(rs.next()) {
                resultList.add(new Integer(rs.getInt("chan_id")));
            }
            Iterator it = resultList.iterator();
            int[] result = new int[resultList.size()];
            for(int i = 0; it.hasNext(); i++) {
                result[i] = ((Integer)it.next()).intValue();
            }
            return result;
        }
        throw new NotFound("No such channel id in the db");
    }

    public int getDBId(ChannelId id) throws SQLException, NotFound {
        int netDbId = netTable.getDbId(id.network_id);
        int[] possibleStaDbIds = stationTable.getDBIds(netDbId, id.station_code);
        int[] possibleSiteIds = siteTable.getDBIds(possibleStaDbIds,
                                                   id.site_code);
        int beginId = time.put(id.begin_time);
        String query = "SELECT chan_id FROM channel WHERE chan_begin_id = "
                + beginId + " and chan_code = '" + id.channel_code + "'"
                + " and site_id IN (";
        for(int i = 0; i < possibleSiteIds.length - 1; i++) {
            query += possibleSiteIds[i] + ", ";
        }
        query += possibleSiteIds[possibleSiteIds.length - 1] + ")";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if(rs.next()) {
            return rs.getInt("chan_id");
        }
        throw new NotFound("No such channel id in the db");
    }

    public JDBCSite getSiteTable() {
        return siteTable;
    }

    public JDBCNetwork getNetworkTable() {
        return netTable;
    }

    public JDBCStation getStationTable() {
        return stationTable;
    }

    private void insertAdditonalChannelStuff(int chanDbId,
                                             int newSiteId,
                                             Channel chan) throws SQLException {
        int index = 1;
        updateNonId.setInt(index++, newSiteId);
        index = insertOnlyChannel(chan, updateNonId, index, quantityTable, time);
        updateNonId.setInt(index++, chanDbId);
        updateNonId.executeUpdate();
    }

    public int put(Channel chan) throws SQLException {
        int dbid;
        try {
            // If no NotFound exception, the channelId exists in the db
            dbid = getDBId(chan.get_id());
            reuniteChannelAndIdIfIdIsLonely(chan, dbid);
        } catch(NotFound notFound) {
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(chan, putAll, 2, siteTable, quantityTable, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    public int put(ChannelId id) throws SQLException {
        int dbid;
        try {
            dbid = getDBId(id);
        } catch(NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putId.setInt(1, dbid);
            putId.setInt(2, siteTable.put(id));
            putId.setString(3, id.channel_code);
            putId.setInt(4, time.put(id.begin_time));
            putId.executeUpdate();
        }
        return dbid;
    }

    private void reuniteChannelAndIdIfIdIsLonely(Channel chan, int dbid)
            throws SQLException {
        int siteDbIdForChannel = siteTable.put(chan.my_site);
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        rs.next();
        int currentSiteId = rs.getInt("site_id");
        if(siteDbIdForChannel != currentSiteId) {
            insertAdditonalChannelStuff(dbid, siteDbIdForChannel, chan);
            ChannelId[] sameSiteChans = getAllChannelIdsForSiteDbId(currentSiteId);
            // only delete if no other channels refer to this site
            if(sameSiteChans.length == 0) {
                siteTable.cleanupVestigesOfLonelyChannelId(currentSiteId);
            }
        }
    }

    private PreparedStatement getAllChans, getAllChansForStation,
            getAllChansForStationOnDate, getAllChansForNetwork,
            getFirstChanForStation, getFirstChanForStationOnDate, getByDBId,
            putAll, updateNonId, putId, getAllIds, getAllIdsForStation,
            getAllIdsForNetwork, getByCodes, getIdsByCodes, getStationDbId,
            getAllIdsForSite;

    private JDBCNetwork netTable;

    private JDBCQuantity quantityTable;

    private JDBCSequence seq;

    private JDBCSite siteTable;

    private JDBCStation stationTable;

    private JDBCTime time;

    public DBChannel extract(ResultSet rs) throws SQLException, NotFound {
        return extract(rs, siteTable, time, quantityTable);
    }

    public static DBChannel extract(ResultSet rs,
                                    JDBCSite siteTable,
                                    JDBCTime time,
                                    JDBCQuantity quantityTable)
            throws SQLException, NotFound {
        Orientation ori = new Orientation(rs.getFloat("chan_orientation_az"),
                                          rs.getFloat("chan_orientation_dip"));
        TimeInterval t = new TimeInterval(quantityTable.get(rs.getInt("chan_sampling_interval_id")));
        Sampling sampling = new SamplingImpl(rs.getInt("chan_sampling_numpoints"),
                                             t);
        ChannelId id = extractId(rs, siteTable, time);
        return new DBChannel(id,
                             rs.getString("chan_name"),
                             ori,
                             sampling,
                             new TimeRange(id.begin_time,
                                           time.get(rs.getInt("chan_end_id"))),
                             siteTable.get(rs.getInt("site_id")),
                             rs.getInt("chan_id"));
    }

    public static ChannelId extractId(ResultSet rs,
                                      JDBCSite siteTable,
                                      JDBCTime time) throws SQLException {
        try {
            SiteId siteId = siteTable.getSiteId(rs.getInt("site_id"));
            return new ChannelId(siteId.network_id,
                                 siteId.station_code,
                                 siteId.site_code,
                                 rs.getString("chan_code"),
                                 time.get(rs.getInt("chan_begin_id")));
        } catch(NotFound e) {
            throw new RuntimeException(e);
        }
    }

    public static int insertAll(Channel chan,
                                PreparedStatement stmt,
                                int index,
                                JDBCSite siteTable,
                                JDBCQuantity quantityTable,
                                JDBCTime time) throws SQLException {
        index = insertId(chan.get_id(),
                         chan.my_site,
                         stmt,
                         index,
                         siteTable,
                         time);
        index = insertOnlyChannel(chan, stmt, index, quantityTable, time);
        return index;
    }

    public static int insertId(ChannelId id,
                               int siteDbId,
                               PreparedStatement stmt,
                               int index,
                               JDBCSite siteTable,
                               JDBCTime time) throws SQLException {
        stmt.setInt(index++, siteDbId);
        stmt.setString(index++, id.channel_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;
    }

    public static int insertId(ChannelId id,
                               PreparedStatement stmt,
                               int index,
                               JDBCSite siteTable,
                               JDBCTime time) throws SQLException {
        return insertId(id, null, stmt, index, null, time);
    }

    public static int insertId(ChannelId id,
                               Site site,
                               PreparedStatement stmt,
                               int index,
                               JDBCSite siteTable,
                               JDBCTime time) throws SQLException {
        return insertId(id, siteTable.put(site), stmt, index, siteTable, time);
    }

    public static int insertOnlyChannel(Channel chan,
                                        PreparedStatement stmt,
                                        int index,
                                        JDBCQuantity quantityTable,
                                        JDBCTime time) throws SQLException {
        if(chan.sampling_info == null) {
            throw new SQLException("Channel's sampling_info is NULL for "
                    + ChannelIdUtil.toString(chan.get_id()));
        }
        stmt.setInt(index++, time.put(chan.effective_time.end_time));
        stmt.setString(index++, chan.name);
        stmt.setFloat(index++, chan.an_orientation.azimuth);
        stmt.setFloat(index++, chan.an_orientation.dip);
        stmt.setInt(index++, quantityTable.put(chan.sampling_info.interval));
        stmt.setInt(index++, chan.sampling_info.numPoints);
        return index;
    }

    public JDBCQuantity getQuantityTable() {
        return quantityTable;
    }

    public JDBCTime getTimeTable() {
        return time;
    }
} // JDBCChannel
