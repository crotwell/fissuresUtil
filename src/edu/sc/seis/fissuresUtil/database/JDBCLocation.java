package edu.sc.seis.fissuresUtil.database;



import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.model.QuantityImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class JDBCLocation extends JDBCTable {
    public JDBCLocation(Connection conn) throws SQLException{
        this(conn, new JDBCQuantity(conn));
    }

    public JDBCLocation(Connection conn,JDBCQuantity jdbcQuantity) throws SQLException {
        super("location",conn);
        this.quantityTable = jdbcQuantity;
        seq = new JDBCSequence(conn, "LocationSeq");
        if(!DBUtil.tableExists("location", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("location.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO location ( loc_id, "+
                                            "loc_lat, loc_lon, loc_elev_id, "+
                                            "loc_depth_id, loc_type) "+
                                            "VALUES (?,?,?,?,?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT loc_id FROM location"+
                                                " WHERE  loc_lat = ? AND"+
                                                " loc_lon = ? AND"+
                                                " loc_elev_id = ? AND"+
                                                " loc_depth_id = ? AND"+
                                                " loc_type = ? ");
        getStmt = conn.prepareStatement(" SELECT loc_lon, loc_lat, "+
                                            " loc_elev_id, loc_depth_id, " +
                                            " loc_type FROM location"+
                                            " WHERE loc_id= ? ");
    }

    public int put(Location location)throws SQLException {
        try{
            return getDBId(location);
        } catch(NotFound ex) {
            int id = seq.next();
            putStmt.setInt(1,id);
            insert(location, putStmt, 2);
            putStmt.executeUpdate();
            return id;
        }
    }

    public int getDBId(Location location) throws SQLException, NotFound {
        insert(location,getDBIdStmt,1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) { return rs.getInt("loc_id"); }
        throw new NotFound("location object not found");
    }

    public Location get(int id) throws SQLException, NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) { return extract(rs);  }
        throw new NotFound(" NO location Id found for the location id = "+id);
    }

    private int insert(Location location,PreparedStatement stmt, int index)
        throws SQLException {
        stmt.setFloat(index++, location.latitude);
        stmt.setFloat(index++, location.longitude);
        stmt.setInt(index++, quantityTable.put(location.elevation));
        stmt.setInt(index++, quantityTable.put(location.depth));
        stmt.setInt(index++, location.type.value());
        return index;
    }

    private Location extract(ResultSet rs) throws SQLException{
        float lat = rs.getFloat("loc_lat");
        float lon = rs.getFloat("loc_lon");
        try {
            QuantityImpl elev = quantityTable.get(rs.getInt("loc_elev_id"));
            QuantityImpl depth = quantityTable.get(rs.getInt("loc_depth_id"));
            LocationType type =  LocationType.from_int(rs.getInt("loc_type"));
            return new Location(lat, lon, elev, depth, type);
        } catch (NotFound e) {
            throw new RuntimeException("There is a foreign key constraint that requires that the elev id and depth id be in quantity, but it looks like this has been violated.  This indicates a db problem",
                                       e);
        }
    }

    private JDBCQuantity quantityTable;

    protected PreparedStatement putStmt, getDBIdStmt, getStmt;

    private JDBCSequence seq;
} // JDBCLocation
