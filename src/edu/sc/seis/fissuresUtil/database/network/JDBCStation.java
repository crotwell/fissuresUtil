package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * JDBCStation.java All methods are unsyncronized, the calling application
 * should make sure that a single instance of this class is not accessed by more
 * than one thread at a time. Because of the use of prepared statements and a
 * single connection per instance, this class IS NOT THREAD-SAFE! Created: Fri
 * May 4 14:11:19 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class JDBCStation extends NetworkTable {

    public JDBCStation() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCStation(Connection conn) throws SQLException {
        this(conn,
             new JDBCLocation(conn),
             new JDBCNetwork(conn),
             new JDBCTime(conn));
    }

    public JDBCStation(Connection conn,
                       JDBCLocation jdbcLocation,
                       JDBCNetwork jdbcNetwork,
                       JDBCTime time) throws SQLException {
        super("station", conn);
        this.locTable = jdbcLocation;
        this.netTable = jdbcNetwork;
        this.time = time;
        seq = new JDBCSequence(conn, "StationSeq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/props/network/default.props");
    }

    private StationId[] extractAll(PreparedStatement query) throws SQLException {
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        try {
            while(rs.next())
                aList.add(extractId(rs, netTable, time));
        } catch(NotFound e) {
            return new StationId[] {};
        }
        return (StationId[])aList.toArray(new StationId[aList.size()]);
    }

    public Station[] extractAll(ResultSet rs) throws SQLException {
        List stations = new ArrayList();
        while(rs.next()) {
            try {
                stations.add(extract(rs, locTable, netTable, time));
            } catch(NotFound e) {
                GlobalExceptionHandler.handle("Got a not found for a particular station",
                                              e);
            }
        }
        return (Station[])stations.toArray(new Station[stations.size()]);
    }

    public Station get(int dbid) throws SQLException, NotFound {
        Station sta = (Station)dbIdsToStations.get(new Integer(dbid));
        if(sta != null) {
            return sta;
        }
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if(rs.next()) {
            return extract(rs, locTable, netTable, time);
        }
        throw new NotFound("No Station found for database id = " + dbid);
    }

    public Station get(StationId id) throws SQLException, NotFound {
        return get(getDBId(id));
    }

    public StationId[] getAllStationIds() throws SQLException {
        return extractAll(getAllQuery);
    }

    public Station[] getAllStations() throws SQLException {
        ResultSet rs = getAllStations.executeQuery();
        return extractAll(rs);
    }

    public StationId[] getAllStationIds(int netDbId) throws SQLException {
        getAllForNet.setInt(1, netDbId);
        return extractAll(getAllForNet);
    }

    /*
     * @returns - a 0 length array if the network isn't found
     */
    public StationId[] getAllStationIds(NetworkId net) throws SQLException {
        try {
            return getAllStationIds(netTable.getDbId(net));
        } catch(NotFound e) {
            return new StationId[] {};
        }
    }

    /*
     * @returns - a 0 length array if the network isn't found
     */
    public Station[] getAllStations(NetworkId myId) throws SQLException {
        StationId[] ids = getAllStationIds(myId);
        Station[] stations = new Station[ids.length];
        for(int i = 0; i < stations.length; i++) {
            try {
                stations[i] = get(ids[i]);
            } catch(NotFound e) {
                GlobalExceptionHandler.handle("Unable to extract a station right after getting its id from the db",
                                              e);
                return new Station[0];
            }
        }
        return stations;
    }

    public int getDBId(StationId id) throws SQLException, NotFound {
        Integer dbId = (Integer)stationIdsToDbIds.get(id);
        if(dbId != null) {
            return dbId.intValue();
        }
        insertId(id, getDBId, 1, netTable, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()) {
            int dbid = rs.getInt("sta_id");
            stationIdsToDbIds.put(id, new Integer(dbid));
            return dbid;
        }
        throw new NotFound("No such station id in the db");
    }

    public int[] getDBIds(int netDbId, String station_code)
            throws SQLException, NotFound {
        getDBIdsForNetAndCode.setInt(1, netDbId);
        getDBIdsForNetAndCode.setString(2, station_code);
        List ids = new ArrayList();
        ResultSet rs = getDBIdsForNetAndCode.executeQuery();
        while(rs.next()) {
            ids.add(new Integer(rs.getInt("sta_id")));
        }
        if(ids.size() > 0) {
            int[] intIds = new int[ids.size()];
            for(int i = 0; i < ids.size(); i++) {
                intIds[i] = ((Integer)ids.get(i)).intValue();
            }
            return intIds;
        }
        throw new NotFound("No stations in the database of code "
                + station_code + " for netDbId " + netDbId);
    }

    public int[] getDBIds(NetworkId net, String stationCode)
            throws SQLException, NotFound {
        return getDBIds(netTable.getDbId(net), stationCode);
    }

    public JDBCNetwork getNetTable() {
        return netTable;
    }

    public StationId getStationId(int dbid) throws SQLException, NotFound {
        Station sta = (Station)dbIdsToStations.get(new Integer(dbid));
        if(sta != null) {
            return sta.get_id();
        }
        getStationIdByDBId.setInt(1, dbid);
        ResultSet rs = getStationIdByDBId.executeQuery();
        if(rs.next()) {
            return extractId(rs, netTable, time);
        }
        throw new NotFound("No StationId found for database id = " + dbid);
    }

    public int put(ChannelId id) throws SQLException {
        int netDbId = netTable.put(id.network_id);
        getByChanIdBits.setInt(1, netDbId);
        getByChanIdBits.setString(2, id.station_code);
        ResultSet rs = getByChanIdBits.executeQuery();
        if(rs.next()) {
            return rs.getInt(1);
        }
        int dbId = seq.next();
        putChanIdBits.setInt(1, dbId);
        putChanIdBits.setInt(2, netDbId);
        putChanIdBits.setString(3, id.station_code);
        putChanIdBits.executeUpdate();
        return dbId;
    }

    public int put(Station sta) throws SQLException {
        int dbid;
        try {
            dbid = getDBId(sta.get_id());
            // No NotFound exception, so already added the id
            // now check if the attrs are added
            getIfNameExists.setInt(1, dbid);
            ResultSet rs = getIfNameExists.executeQuery();
            if(!rs.next()) {//No name, so we need to add the attr part
                updateStation(sta, dbid);
            }
        } catch(NotFound notFound) {
            // no id found so check for wrong begin time
            try {
            int[] dbidForCode = getDBIds(sta.get_id().network_id, sta.get_code());
            MicroSecondDate staBeginDate = new MicroSecondDate(sta.effective_time.start_time);
            MicroSecondDate staEndDate = new MicroSecondDate(sta.effective_time.end_time);
            MicroSecondDate nowPlusTen = ClockUtil.now().add(new TimeInterval(10, UnitImpl.GREGORIAN_YEAR));
            for(int i = 0; i < dbidForCode.length; i++) {
                Station dbsta = get(dbidForCode[i]);
                MicroSecondDate dbEndDate = new MicroSecondDate(dbsta.effective_time.end_time);
                MicroSecondDate dbBeginDate = new MicroSecondDate(dbsta.effective_time.start_time);
                if (dbBeginDate.before(staBeginDate) && dbEndDate.after(nowPlusTen) && staEndDate.after(nowPlusTen)) {
                    // both end in future, so end db station and insert new with db station end as new station begin
                    dbsta.effective_time.end_time = sta.effective_time.start_time;
                    updateStation(dbsta, dbidForCode[i]);
                }
            }
            } catch (NotFound e) {
                // not found by code, so go ahead an add it
            }
            // ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(sta, putAll, 2, netTable, locTable, time);
            dbIdsToStations.put(new Integer(dbid), sta);
            putAll.executeUpdate();
        }
        return dbid;
    }
    
    public void updateStation(Station sta, int dbid) throws SQLException {
        int index = insertOnlyStation(sta,
                                      updateSta,
                                      1,
                                      netTable,
                                      locTable,
                                      time);
        updateSta.setInt(index, dbid);
        updateSta.executeUpdate();
    }

    public int put(StationId id) throws SQLException {
        int dbid;
        try {
            dbid = getDBId(id);
        } catch(NotFound e) {
            dbid = seq.next();
            putId.setInt(1, dbid);
            insertId(id, putId, 2, netTable, time);
            putId.executeUpdate();
        }
        return dbid;
    }

    private PreparedStatement getAllQuery, getAllForNet, getIfNameExists,
            getByDBId, getStationIdByDBId, getDBId, updateSta, putAll, putId,
            putChanIdBits, getByChanIdBits, getDBIdsForNetAndCode,
            deleteStation, getAllStations;

    private JDBCLocation locTable;

    private JDBCNetwork netTable;

    private JDBCSequence seq;

    private JDBCTime time;

    public static void emptyCache() {
        stationIdsToDbIds.clear();
        dbIdsToStations.clear();
    }

    public static Station extract(ResultSet rs,
                                  JDBCLocation locTable,
                                  JDBCNetwork netTable,
                                  JDBCTime time) throws SQLException, NotFound {
        Station sta = (Station)dbIdsToStations.get(new Integer(rs.getInt("sta_id")));
        if(sta != null) {
            return sta;
        }
        StationId id = extractId(rs, netTable, time);
        edu.iris.Fissures.Time endTime = time.get(rs.getInt("sta_end_id"));
        sta = new StationImpl(id,
                              rs.getString("sta_name"),
                              locTable.get(rs.getInt("loc_id")),
                              new TimeRange(id.begin_time, endTime),
                              rs.getString("sta_operator"),
                              rs.getString("sta_description"),
                              rs.getString("sta_comment"),
                              netTable.get(rs.getInt("net_id")));
        dbIdsToStations.put(new Integer(rs.getInt("sta_id")), sta);
        return sta;
    }

    public static StationId extractId(ResultSet rs,
                                      JDBCNetwork netTable,
                                      JDBCTime time) throws SQLException,
            NotFound {
        edu.iris.Fissures.Time begin_time = time.get(rs.getInt("sta_begin_id"));
        try {
            NetworkId netId = netTable.getNetworkId(rs.getInt("net_id"));
            StationId id = new StationId(netId,
                                         rs.getString("sta_code"),
                                         begin_time);
            stationIdsToDbIds.put(id, new Integer(rs.getInt("sta_id")));
            return id;
        } catch(NotFound e) {
            throw new RuntimeException("There is a foreign key constraint requiring that a net_id be in the network table, but it just returned a not found for one such key.  This probably indicates a db problem!",
                                       e);
        }
    }

    public static String getNeededForStation() {
        return getNeededForStationId()
                + ", sta_end_id, sta_name, station.loc_id, sta_operator, sta_description, sta_comment";
    }

    public static String getNeededForStationId() {
        return "sta_id, station.net_id, sta_code, sta_begin_id";
    }

    public static int insertAll(Station sta,
                                PreparedStatement stmt,
                                int index,
                                JDBCNetwork netTable,
                                JDBCLocation locTable,
                                JDBCTime time) throws SQLException {
        index = insertId(sta.get_id(), stmt, index, netTable, time);
        index = insertOnlyStation(sta, stmt, index, netTable, locTable, time);
        return index;
    }

    public static int insertId(StationId id,
                               PreparedStatement stmt,
                               int index,
                               JDBCNetwork netTable,
                               JDBCTime time) throws SQLException {
        stmt.setInt(index++, netTable.put(id.network_id));
        stmt.setString(index++, id.station_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;
    }

    public static int insertOnlyStation(Station sta,
                                        PreparedStatement stmt,
                                        int index,
                                        JDBCNetwork netTable,
                                        JDBCLocation locTable,
                                        JDBCTime time) throws SQLException {
        // make sure network is completely inserted
        netTable.put(sta.my_network);
        stmt.setInt(index++, time.put(sta.effective_time.end_time));
        stmt.setString(index++, sta.name);
        stmt.setString(index++, sta.operator);
        stmt.setString(index++, sta.description);
        stmt.setString(index++, sta.comment);
        stmt.setInt(index++, locTable.put(sta.my_location));
        return index;
    }

    private static Map dbIdsToStations = Collections.synchronizedMap(new HashMap());

    private static Map stationIdsToDbIds = Collections.synchronizedMap(new HashMap());

    public void cleanupVestigesOfLonelyChannelId(int sta_id)
            throws SQLException {
        deleteStation.setInt(1, sta_id);
        deleteStation.executeUpdate();
    }
}// JDBCStation
