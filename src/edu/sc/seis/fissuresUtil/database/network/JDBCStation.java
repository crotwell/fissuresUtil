package edu.sc.seis.fissuresUtil.database.network;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.JDBCLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * JDBCStation.java
 *
 * All methods are unsyncronized, the calling application should make sure
 * that a single instance of this class is not accessed by more than one
 * thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE!
 *
 * Created: Fri May  4 14:11:19 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class JDBCStation extends NetworkTable{
    public JDBCStation()throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCStation(Connection conn)throws SQLException{
        this(conn, new JDBCLocation(conn), new JDBCNetwork(conn),
             new JDBCTime(conn));
    }

    public JDBCStation (Connection conn, JDBCLocation jdbcLocation,
                        JDBCNetwork jdbcNetwork, JDBCTime time)
        throws SQLException {
        super("station", conn);
        this.locTable = jdbcLocation;
        this.netTable = jdbcNetwork;
        this.time = time;
        seq = new JDBCSequence(conn, "StationSeq");
        if(!DBUtil.tableExists("station", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("station.create"));
        }
        putAll = conn.prepareStatement("INSERT INTO station (sta_id, net_id, sta_code, "+
                                           "sta_begin_id, sta_end_id, "+
                                           "sta_name, sta_operator, sta_description, "+
                                           "loc_id) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        putId = conn.prepareStatement("INSERT INTO station (sta_id, net_id, sta_code, " +
                                          "sta_begin_id) "+
                                          "VALUES (?, ?, ?, ?)");
        String getAllQuery = "SELECT sta_id, net_id, sta_code, sta_begin_id FROM station";
        getAll = conn.prepareStatement(getAllQuery);
        getAllForNet = conn.prepareStatement(getAllQuery + " WHERE net_id = ?");
        getIfNameExists = conn.prepareStatement("SELECT sta_id FROM station " +
                                                    "WHERE sta_id = ? AND " +
                                                    "sta_name IS NOT NULL");
        getByDBId = conn.prepareStatement("SELECT * FROM station WHERE sta_id = ?");
        getDBId = conn.prepareStatement("SELECT sta_id FROM station WHERE net_id = ? AND " +
                                            "sta_code = ? AND sta_begin_id = ?");
        updateSta = conn.prepareStatement("UPDATE station SET sta_end_id = ?, " +
                                              "sta_name = ?, sta_operator = ?, "+
                                              "sta_description = ? , loc_id = ? " +
                                              "WHERE sta_id = ?");
    }

    public StationId[] getAllStationIds() throws SQLException {
        return extractAll(getAll);
    }

    /*
     *@returns - a 0 length array if the network isn't found
     */
    public StationId[] getAllStationIds(NetworkId net) throws SQLException{
        try {
            int net_id = netTable.getDBId(net);
            getAllForNet.setInt(1, net_id);
            return extractAll(getAllForNet);
        } catch (NotFound e) { return new StationId[]{};  }
    }

    private StationId[] extractAll(PreparedStatement query) throws SQLException{
        ResultSet rs = query.executeQuery();
        List aList = new ArrayList();
        try {
            while (rs.next()) aList.add(extractId(rs, netTable, time));
        } catch (NotFound e) {return new StationId[]{}; }
        return  (StationId[])aList.toArray(new StationId[aList.size()]);
    }

    public int put(Station sta)  throws SQLException {
        int dbid;
        try {
            dbid = getDBId(sta.get_id());
            // No NotFound exception, so already added the id
            // now check if the attrs are added
            getIfNameExists.setInt(1, dbid);
            ResultSet rs = getIfNameExists.executeQuery();
            if(!rs.next()) {//No name, so we need to add the attr part
                int index = insertOnlyStation(sta, updateSta, 1, locTable, time);
                updateSta.setInt(index, dbid);
                updateSta.executeUpdate();
            }
        } catch (NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(sta, putAll, 2, netTable, locTable, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    public int put(StationId id) throws SQLException{
        int dbid;
        try {
            dbid = getDBId(id);
        }catch(NotFound e){
            dbid = seq.next();
            putId.setInt(1, dbid);
            insertId(id, putId, 2, netTable, time);
            putId.executeUpdate();
        }
        return dbid;
    }

    public Station get(int dbid)  throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if (rs.next()){ return extract(rs, locTable, netTable, time);}
        throw new NotFound("No Station found for database id = "+dbid);
    }

    public Station get(StationId id)throws SQLException, NotFound {
        return get(getDBId(id));
    }

    public int getDBId(StationId id)  throws SQLException, NotFound {
        insertId(id, getDBId, 1, netTable, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()){ return rs.getInt("sta_id"); }
        throw new NotFound("No such station id in the db");
    }

    public static Station extract(ResultSet rs, JDBCLocation locTable,
                                  JDBCNetwork netTable, JDBCTime time) throws SQLException, NotFound {
        StationId id = extractId(rs,netTable, time);
        edu.iris.Fissures.Time endTime =  time.get(rs.getInt("sta_end_id"));
        return new StationImpl(id,
                               rs.getString("sta_name"),
                               locTable.get(rs.getInt("loc_id")),
                               new TimeRange(id.begin_time, endTime),
                               rs.getString("sta_operator"),
                               rs.getString("sta_description"),
                               rs.getString("sta_comment"),
                               netTable.get(rs.getInt("net_id")));
    }

    public static StationId extractId(ResultSet rs, JDBCNetwork netTable,
                                      JDBCTime time) throws SQLException, NotFound{
        edu.iris.Fissures.Time begin_time = time.get(rs.getInt("sta_begin_id"));
        try {
            NetworkId netId = netTable.get(rs.getInt("net_id")).get_id();
            return new StationId(netId, rs.getString("sta_code"),  begin_time);
        }catch (NotFound e) {
            throw new RuntimeException("There is a foreign key constraint requiring that a net_id be in the network table, but it just returned a not found for one such key.  This probably indicates a db problem!",
                                       e);
        }
    }

    public static int insertAll(Station sta, PreparedStatement stmt, int index,
                                JDBCNetwork netTable, JDBCLocation locTable,
                               JDBCTime time) throws SQLException {
        index = insertId(sta.get_id(), stmt, index, netTable, time);
        index = insertOnlyStation(sta, stmt, index, locTable, time);
        return index;
    }

    public static int insertOnlyStation(Station sta, PreparedStatement stmt,
                                        int index, JDBCLocation locTable,
                                       JDBCTime time) throws SQLException{
        stmt.setInt(index++, time.put(sta.effective_time.end_time));
        stmt.setString(index++, sta.name);
        stmt.setString(index++, sta.operator);
        stmt.setString(index++, sta.description);
        stmt.setInt(index++, locTable.put(sta.my_location));
        return index;
    }

    public static int insertId(StationId id, PreparedStatement stmt, int index,
                               JDBCNetwork netTable, JDBCTime time) throws SQLException{
        stmt.setInt(index++, netTable.put(id.network_id));
        stmt.setString(index++, id.station_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;
    }
    private JDBCLocation locTable;
    private JDBCNetwork netTable;
    private JDBCSequence seq;
    private JDBCTime time;
    private PreparedStatement getAll, getAllForNet, getIfNameExists, getByDBId,
        getDBId, updateSta, putAll, putId;

    private static final Logger logger = Logger.getLogger(JDBCStation.class);
}// JDBCStation
