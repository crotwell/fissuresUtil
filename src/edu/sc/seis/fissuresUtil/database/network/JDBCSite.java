
package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.SiteImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;

/**
 * JDBCSite.java
 *
 * All methods are unsyncronized, the calling application should make sure
 * that a single instance of this class is not accessed by more than one
 * thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE!
 *
 * Created: Fri Jan 26 12:01:39 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class JDBCSite extends NetworkTable {
    public JDBCSite() throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCSite(Connection conn) throws SQLException{
        this(conn, new JDBCLocation(conn), new JDBCStation(conn),
             new JDBCTime(conn));
    }


    public JDBCSite(Connection conn, JDBCLocation locTable,
                    JDBCStation stationTable, JDBCTime time) throws SQLException {
        super("site", conn);
        this.locTable = locTable;
        this.stationTable = stationTable;
        this.time = time;
        seq = new JDBCSequence(conn, "SiteSeq");
        if(!DBUtil.tableExists("site", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("site.create"));
        }
        putAll = conn.prepareStatement("INSERT INTO site (site_id, sta_id, " +
                                           "site_code, site_begin_id, site_end_id, "+
                                           "site_comment, loc_id) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, ?)");
        putId = conn.prepareStatement("INSERT INTO site (site_id, sta_id, " +
                                          "site_code, site_begin_id) " +
                                          "VALUES (?, ?, ?, ?)");
        String getAllIdsQuery = "SELECT site_id, sta_id, site_code, site_begin_id FROM site";
        getAllIds = conn.prepareStatement(getAllIdsQuery);
        getAllIdsForSta = conn.prepareStatement(getAllIdsQuery + " WHERE sta_id = ?");
        getAllIdsForNet = conn.prepareStatement(getAllIdsQuery + ", station WHERE site.sta_id = station.sta_id AND station.net_id = ?");
        String getAllQuery = "SELECT * FROM site";
        getAllSites = conn.prepareStatement(getAllQuery);
        getAllSitesForSta = conn.prepareStatement(getAllQuery + " WHERE sta_id = ?");
        getAllSitesForNet = conn.prepareStatement(getAllQuery + ", station WHERE site.sta_id =  station.sta_id AND station.net_id = ?");
        getIfCommentExists = conn.prepareStatement("SELECT site_id FROM site " +
                                                       "WHERE site_id = ? AND " +
                                                       "site_comment IS NOT NULL");
        getByDBId = conn.prepareStatement("SELECT * FROM site WHERE site_id = ?");
        getDBId = conn.prepareStatement("SELECT site_id FROM site WHERE sta_id = ? AND " +
                                            "site_code = ? AND site_begin_id = ?");
        updateSite = conn.prepareStatement("UPDATE site SET site_end_id = ?, " +
                                               "site_comment = ?, loc_id = ? WHERE site_id = ?");
    }

    public SiteId[] getAllSiteIds() throws SQLException {
        return extractAllSiteIds(getAllIds);
    }

    /**
     *@returns - a 0 length array if the station isn't found
     */
    public SiteId[] getAllSiteIds(StationId sta) throws SQLException{
        try {
            int sta_id = stationTable.getDBId(sta);
            getAllIdsForSta.setInt(1, sta_id);
            return extractAllSiteIds(getAllIdsForSta);
        } catch (NotFound e) { return new SiteId[]{};  }
    }

    public SiteId[] getAllSiteIds(NetworkId net) throws SQLException{
        try {
            int net_id = stationTable.getNetTable().getDBId(net);
            getAllIdsForNet.setInt(1, net_id);
            return extractAllSiteIds(getAllIdsForSta);
        } catch (NotFound e) { return new SiteId[]{};  }
    }

    public Site[] getAllSites() throws SQLException, NotFound {
        return extractAllSites(getAllSites);
    }

    public Site[] getAllSites(StationId sta) throws SQLException{
        try{
            int sta_id = stationTable.getDBId(sta);
            getAllSitesForSta.setInt(1, sta_id);
            return extractAllSites(getAllSitesForSta);
        } catch (NotFound e) {return new Site[]{}; }
    }

    public Site[] getAllSites(NetworkId net) throws SQLException{
        try{
            int net_id = stationTable.getNetTable().getDBId(net);
            getAllSitesForNet.setInt(1, net_id);
            return extractAllSites(getAllSitesForNet);
        } catch (NotFound e) {return new Site[]{}; }
    }

    private Site[] extractAllSites(PreparedStatement query) throws SQLException, NotFound{
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        while(rs.next()){ aList.add(extract(rs, locTable, stationTable, time)); }
        return (Site[])aList.toArray(new Site[aList.size()]);
    }

    private SiteId[] extractAllSiteIds(PreparedStatement query) throws SQLException{
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        while (rs.next()){ aList.add(extractId(rs, stationTable, time)); }
        return  (SiteId[])aList.toArray(new SiteId[aList.size()]);
    }

    public int put(Site site)  throws SQLException {
        int dbid;
        try {
            dbid = getDBId(site.get_id(), site.my_station);
            // No NotFound exception, so already added the id
            // now check if the attrs are added
            getIfCommentExists.setInt(1, dbid);
            ResultSet rs = getIfCommentExists.executeQuery();
            if(!rs.next()) {//No name, so we need to add the attr part
                int index = insertOnlySite(site, updateSite, 1, locTable, time);
                updateSite.setInt(index, dbid);
                updateSite.executeUpdate();
            }
        } catch (NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(site, putAll, 2, stationTable, locTable, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    public Site get(int dbid)  throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if (rs.next()){ return extract(rs, locTable, stationTable, time);}
        throw new NotFound("No Station found for database id = "+dbid);
    }

    public Site get(SiteId id, Station staId)throws SQLException, NotFound {
        return get(getDBId(id, staId));
    }

    public int getDBId(SiteId id, Station staId)  throws SQLException, NotFound {
        insertId(id, staId, getDBId, 1, stationTable, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()){ return rs.getInt("site_id"); }
        throw new NotFound("No such station id in the db");
    }

    public static Site extract(ResultSet rs, JDBCLocation locTable,
                               JDBCStation stationTable, JDBCTime time) throws SQLException, NotFound {
        SiteId id = extractId(rs,stationTable, time);
        return new SiteImpl(id, locTable.get(rs.getInt("loc_id")),
                            new TimeRange(id.begin_time,
                                          time.get(rs.getInt("site_end_id"))),
                            stationTable.get(rs.getInt("sta_id")),
                            rs.getString("site_comment"));
    }

    public static SiteId extractId(ResultSet rs, JDBCStation staTable,
                                   JDBCTime time) throws SQLException{
        try {
            StationId staId = staTable.get(rs.getInt("sta_id")).get_id();
            return new SiteId(staId.network_id, staId.station_code,
                              rs.getString("site_code"),
                              time.get(rs.getInt("site_begin_id")));
        }catch (NotFound e) {
            throw new RuntimeException("There is a foreign key constraint requiring that a net_id be in the network table, but it just returned a not found for one such key.  This probably indicates a db problem!",
                                       e);
        }
    }

    public static int insertAll(Site site, PreparedStatement stmt, int index,
                                JDBCStation stationTable, JDBCLocation locTable,
                                JDBCTime time)throws SQLException {
        index = insertId(site.get_id(), site.my_station, stmt, index,
                         stationTable, time);
        index = insertOnlySite(site, stmt, index, locTable, time);
        return index;
    }

    public static int insertOnlySite(Site site, PreparedStatement stmt,
                                     int index, JDBCLocation locTable,
                                     JDBCTime time)throws SQLException{
        stmt.setInt(index++, time.put(site.effective_time.end_time));
        stmt.setString(index++, site.comment);
        stmt.setInt(index++, locTable.put(site.my_location));
        return index;
    }

    public static int insertId(SiteId id, Station sta, PreparedStatement stmt, int index,
                               JDBCStation stationTable, JDBCTime time) throws SQLException{
        stmt.setInt(index++, stationTable.put(sta));
        stmt.setString(index++, id.site_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;

    }

    public JDBCStation getStationTable(){ return stationTable; }

    private PreparedStatement getAllIds, getAllIdsForSta, getIfCommentExists, getByDBId,
        getDBId, updateSite, putAll, putId, getAllIdsForNet, getAllSites, getAllSitesForSta,
        getAllSitesForNet;
    private JDBCSequence seq;
    private JDBCLocation locTable;
    private JDBCStation stationTable;
    private JDBCTime time;

    private static final Logger logger = Logger.getLogger(JDBCSite.class);
} // JDBCSite
