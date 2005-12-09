package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.SiteImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

/**
 * JDBCSite.java All methods are unsynchronized, the calling application should
 * make sure that a single instance of this class is not accessed by more than
 * one thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE! Created: Fri Jan 26
 * 12:01:39 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class JDBCSite extends NetworkTable {

    public JDBCSite() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCSite(Connection conn) throws SQLException {
        this(conn,
             new JDBCLocation(conn),
             new JDBCStation(conn),
             new JDBCTime(conn));
    }

    public JDBCSite(Connection conn,
                    JDBCLocation locTable,
                    JDBCStation stationTable,
                    JDBCTime time) throws SQLException {
        super("site", conn);
        this.locTable = locTable;
        this.stationTable = stationTable;
        this.time = time;
        seq = new JDBCSequence(conn, "SiteSeq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/props/network/default.props");
    }

    public Site get(int dbid) throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if(rs.next()) {
            return extract(rs, locTable, stationTable, time);
        }
        throw new NotFound("No Site found for database id = " + dbid);
    }

    public int getDBId(SiteId id, Station staId) throws SQLException, NotFound {
        insertId(id, staId, getDBId, 1, stationTable, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()) {
            return rs.getInt("site_id");
        }
        throw new NotFound("No such Site id in the db");
    }

    public int[] getDBIds(int[] possibleStaDbIds, String site_code)
            throws SQLException, NotFound {
        List ids = new ArrayList();
        for(int i = 0; i < possibleStaDbIds.length; i++) {
            getDBIdsForStaAndCode.setInt(1, possibleStaDbIds[i]);
            getDBIdsForStaAndCode.setString(2, site_code);
            ResultSet rs = getDBIdsForStaAndCode.executeQuery();
            while(rs.next()) {
                ids.add(new Integer(rs.getInt("site_id")));
            }
        }
        if(ids.size() > 0) {
            int[] intIds = new int[ids.size()];
            for(int i = 0; i < ids.size(); i++) {
                intIds[i] = ((Integer)ids.get(i)).intValue();
            }
            return intIds;
        }
        throw new NotFound("No sites in the database of code '" + site_code
                + "' for given station ids");
    }

    public SiteId getSiteId(int dbid) throws SQLException, NotFound {
        getSiteIdByDBId.setInt(1, dbid);
        ResultSet rs = getSiteIdByDBId.executeQuery();
        if(rs.next()) {
            return extractId(rs, stationTable, time);
        }
        throw new NotFound("No SiteId found for database id = " + dbid);
    }

    public JDBCStation getStationTable() {
        return stationTable;
    }

    public int put(ChannelId id) throws SQLException {
        int sta_id = stationTable.put(id);
        getByChanIdBits.setInt(1, sta_id);
        getByChanIdBits.setString(2, id.site_code);
        ResultSet rs = getByChanIdBits.executeQuery();
        if(rs.next()) {
            return rs.getInt(1);
        }
        int dbid = seq.next();
        putChanIdBits.setInt(1, dbid);
        putChanIdBits.setInt(2, sta_id);
        putChanIdBits.setString(3, id.site_code);
        putChanIdBits.executeUpdate();
        return dbid;
    }

    public int put(Site site) throws SQLException {
        int dbid;
        try {
            dbid = getDBId(site.get_id(), site.my_station);
            // No NotFound exception, so already added the id
            // now check if the attrs are added
            getIfCommentExists.setInt(1, dbid);
            ResultSet rs = getIfCommentExists.executeQuery();
            if(!rs.next()) {// No name, so we need to add the attr part
                int index = insertOnlySite(site, updateSite, 1, locTable, time);
                updateSite.setInt(index, dbid);
                updateSite.executeUpdate();
            }
        } catch(NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(site, putAll, 2, stationTable, locTable, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    private PreparedStatement getIfCommentExists, getByDBId, getSiteIdByDBId,
            getDBId, updateSite, putAll, getDBIdsForStaAndCode, putChanIdBits,
            getByChanIdBits, deleteSite, count;

    private JDBCLocation locTable;

    private JDBCSequence seq;

    private JDBCStation stationTable;

    private JDBCTime time;

    public static Site extract(ResultSet rs,
                               JDBCLocation locTable,
                               JDBCStation stationTable,
                               JDBCTime time) throws SQLException, NotFound {
        SiteId id = extractId(rs, stationTable, time);
        return new SiteImpl(id,
                            locTable.get(rs.getInt("loc_id")),
                            new TimeRange(id.begin_time,
                                          time.get(rs.getInt("site_end_id"))),
                            stationTable.get(rs.getInt("sta_id")),
                            rs.getString("site_comment"));
    }

    public static SiteId extractId(ResultSet rs,
                                   JDBCStation staTable,
                                   JDBCTime time) throws SQLException {
        try {
            StationId staId = staTable.getStationId(rs.getInt("sta_id"));
            return new SiteId(staId.network_id,
                              staId.station_code,
                              rs.getString("site_code"),
                              time.get(rs.getInt("site_begin_id")));
        } catch(NotFound e) {
            throw new RuntimeException("There is a foreign key constraint requiring that a sta_id be in the station table, but it just returned a not found for one such key.  This probably indicates a db problem!",
                                       e);
        }
    }

    public static int insertAll(Site site,
                                PreparedStatement stmt,
                                int index,
                                JDBCStation stationTable,
                                JDBCLocation locTable,
                                JDBCTime time) throws SQLException {
        index = insertId(site.get_id(),
                         site.my_station,
                         stmt,
                         index,
                         stationTable,
                         time);
        index = insertOnlySite(site, stmt, index, locTable, time);
        return index;
    }

    public static int insertId(SiteId id,
                               Station sta,
                               PreparedStatement stmt,
                               int index,
                               JDBCStation stationTable,
                               JDBCTime time) throws SQLException {
        int sta_id = stationTable.put(sta);
        stmt.setInt(index++, sta_id);
        stmt.setString(index++, id.site_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;
    }

    public static int insertOnlySite(Site site,
                                     PreparedStatement stmt,
                                     int index,
                                     JDBCLocation locTable,
                                     JDBCTime time) throws SQLException {
        stmt.setInt(index++, time.put(site.effective_time.end_time));
        stmt.setString(index++, site.comment);
        stmt.setInt(index++, locTable.put(site.my_location));
        return index;
    }

    public void cleanupVestigesOfLonelyChannelId(int currentSiteId)
            throws SQLException {
        getByDBId.setInt(1, currentSiteId);
        ResultSet rs = getByDBId.executeQuery();
        rs.next();
        int sta_id = rs.getInt("sta_id");
        deleteSite.setInt(1, currentSiteId);
        deleteSite.executeUpdate();
        stationTable.cleanupVestigesOfLonelyChannelId(sta_id);
    }

    public int size() throws SQLException {
        ResultSet rs = count.executeQuery();
        rs.next();
        return rs.getInt(1);
    }
} // JDBCSite
