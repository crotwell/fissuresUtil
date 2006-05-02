package edu.sc.seis.fissuresUtil.database.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTime;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

/**
 * JDBCNetwork.java
 *
 * All methods are unsyncronized, the calling application should make sure
 * that a single instance of this class is not accessed by more than one
 * thread at a time. Because of the use of prepared statements and a single
 * connection per instance, this class IS NOT THREAD-SAFE!
 *
 * Created: Fri May  4 14:37:24 2001
 *
 * @author <a href="mailto: "Philip Crotwell</a>
 * @version
 */

public class JDBCNetwork extends NetworkTable{
    public JDBCNetwork() throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCNetwork(Connection conn) throws SQLException{
        this(conn, new JDBCTime(conn));
    }

    public JDBCNetwork (Connection conn, JDBCTime time) throws SQLException {
        super("network", conn);
        this.time = time;
        seq = new JDBCSequence(conn, "NetworkSeq");
        TableSetup.setup(this, "edu/sc/seis/fissuresUtil/database/props/network/default.props");
        putAll = conn.prepareStatement("INSERT INTO network (net_id, net_code, "+
                                           "net_begin_id, net_end_id,"+
                                           "net_name, net_owner, net_description) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, ?)");
        putId = conn.prepareStatement("INSERT INTO network(net_id, net_code, " +
                                          "net_begin_id) "+
                                          "VALUES (?, ?, ?)");
        updateAttr = conn.prepareStatement("UPDATE network SET net_end_id = ?, " +
                                               "net_name = ?, net_owner = ?, net_description = ? " +
                                               "WHERE net_id = ?");
        getAll = conn.prepareStatement("SELECT * FROM network ORDER BY net_code");
        getByDBId = conn.prepareStatement("SELECT * FROM network WHERE net_id = ?");
        getDBId = conn.prepareStatement("SELECT net_id FROM network WHERE net_code = ? AND "+
                                            "net_begin_id = ?");
        getIfNameExists = conn.prepareStatement("SELECT net_id FROM network " +
                                                    "WHERE net_id = ? AND " +
                                                    "net_name IS NOT NULL");
        getNetIdByDBId = conn.prepareStatement("SELECT net_id, net_code, net_begin_id FROM network WHERE net_id = ?");
    }

    public int[] getAllNetworkDBIds() throws SQLException {
        ResultSet rs = getAll.executeQuery();
        List aList = new ArrayList();
        while (rs.next()) aList.add(new Integer(rs.getInt("net_id")));
        int[] out = new int[aList.size()];
        int i=0;
        Iterator it = aList.iterator();
        while (it.hasNext()) {
            out[i] = ((Integer)it.next()).intValue();
            i++;
        }
        return out;
    }

    public NetworkId[] getAllNetworkIds() throws SQLException, NotFound {
        ResultSet rs = getAll.executeQuery();
        List aList = new ArrayList();
        while (rs.next()) aList.add(extractId(rs, time));
        return  (NetworkId[])aList.toArray(new NetworkId[aList.size()]);
    }
    
    public NetworkAttr[] getAllNetworkAttrs() throws SQLException, NotFound {
        ResultSet rs = getAll.executeQuery();
        List aList = new ArrayList();
        while (rs.next()) aList.add(extract(rs, time));
        return  (NetworkAttr[])aList.toArray(new NetworkAttr[aList.size()]);
    }

    public int put(NetworkAttr network)  throws SQLException {
        int dbid;
        try {
            dbid = getDbId(network.get_id());
            // No NotFound exception, so already added the id
            // now check if the attrs are added
            getIfNameExists.setInt(1, dbid);
            ResultSet rs = getIfNameExists.executeQuery();
            if(!rs.next()) {//No name, so we need to add the attr part
                int index = insertOnlyAttr(network, updateAttr, 1, time);
                updateAttr.setInt(index, dbid);
                updateAttr.executeUpdate();
            }
        } catch (NotFound notFound) {
            // no id found so ok to add the whole thing
            dbid = seq.next();
            putAll.setInt(1, dbid);
            insertAll(network, putAll, 2, time);
            putAll.executeUpdate();
        }
        return dbid;
    }

    public int put(NetworkId id) throws SQLException{
        int dbid;
        try {
            dbid = getDbId(id);
        }catch(NotFound e){
            dbid = seq.next();
            putId.setInt(1, dbid);
            insertId(id, putId, 2, time);
            putId.executeUpdate();
        }
        return dbid;
    }

    public NetworkAttr get(int dbid)  throws SQLException, NotFound {
        getByDBId.setInt(1, dbid);
        ResultSet rs = getByDBId.executeQuery();
        if (rs.next()){ return extract(rs, time);}
        throw new NotFound("No Network found for database id = "+dbid);
    }

    public NetworkId getNetworkId(int dbid)  throws SQLException, NotFound {
        getNetIdByDBId.setInt(1, dbid);
        ResultSet rs = getNetIdByDBId.executeQuery();
        if (rs.next()){ return extractId(rs, time);}
        throw new NotFound("No Network found for database id = "+dbid);
    }
    
    public NetworkAttr get(NetworkId id)throws SQLException, NotFound {
        return get(getDbId(id));
    }

    public NetworkId[] getByCode(String netCode) throws SQLException, NotFound {
        getByCode.setString(1, netCode);
        ResultSet rs = getByCode.executeQuery();
        List aList = new ArrayList();
        while (rs.next()) {
            aList.add(extractId(rs, time));
        }
        return  (NetworkId[])aList.toArray(new NetworkId[aList.size()]);
    }

    public int getDbId(NetworkId id)  throws SQLException, NotFound {
        insertId(id, getDBId, 1, time);
        ResultSet rs = getDBId.executeQuery();
        if(rs.next()){ return rs.getInt("net_id"); }
        // didn't find it, try by code in case date is different
        NetworkId[] nets = getByCode(id.network_code);
        for(int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(id, nets[i])) {
                return getDbId(nets[i]);
            }
        }
        throw new NotFound("No such network id in the db: "+NetworkIdUtil.toString(id));
    }

    public static NetworkAttr extract(ResultSet rs, JDBCTime time) throws SQLException, NotFound {
        NetworkId networkId = extractId(rs, time);
        return new NetworkAttrImpl(networkId,
                                   rs.getString("net_name"),
                                   rs.getString("net_description"),
                                   rs.getString("net_owner"),
                                   new TimeRange(networkId.begin_time,
                                                 time.get(rs.getInt("net_end_id"))));
    }

    public static NetworkId extractId(ResultSet rs, JDBCTime time) throws SQLException, NotFound{
        return new NetworkId(rs.getString("net_code"),
                             time.get(rs.getInt("net_begin_id")));
    }

    public static int insertAll(NetworkAttr net, PreparedStatement stmt,
                                int index, JDBCTime time)
        throws SQLException {
        index = insertId(net.get_id(), stmt, index, time);
        index = insertOnlyAttr(net, stmt, index, time);
        return index;
    }

    public static int insertOnlyAttr(NetworkAttr net, PreparedStatement stmt,
                                     int index, JDBCTime time)
        throws SQLException{
        stmt.setInt(index++, time.put(net.effective_time.end_time));
        stmt.setString(index++, net.name);
        stmt.setString(index++, net.owner);
        stmt.setString(index++, net.description);
        return index;
    }

    public static int insertId(NetworkId id, PreparedStatement stmt, int index,
                              JDBCTime time) throws SQLException{
        stmt.setString(index++, id.network_code);
        stmt.setInt(index++, time.put(id.begin_time));
        return index;
    }
    
    private JDBCSequence seq;
    private JDBCTime time;

    protected PreparedStatement putAll, putId, getAll, getIfNameExists,
        getByDBId, getDBId, updateAttr, getNetIdByDBId, getByCode;

}// JDBCNetwork
