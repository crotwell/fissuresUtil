package edu.sc.seis.fissuresUtil.database.event;

import edu.sc.seis.fissuresUtil.*;
import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.utility.Logger;
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
        this.jdbcQuantity = jdbcQuantity;
        Statement stmt = conn.createStatement();
        seq = new JDBCSequence(conn, "LocationSeq");
        
        if(!DBUtil.tableExists("location", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("location.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO location"+
                                            " ( locationid,"+
                                            " locationlatitude, "+
                                            " locationlongitude, "+
                                            " locationelevationvalue,"+
                                            " locationelevationunitid,"+
                                            " locationdepthvalue,"+
                                            " locationdepthunitid,"+
                                            " locationtype )"+
                                            " VALUES"+
                                            " (?,?,?,?,?,?,?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT locationid FROM location"+
                                                " WHERE  locationlatitude = ? AND"+
                                                " locationlongitude = ? AND"+
                                                " locationelevationvalue = ? AND"+
                                                " locationelevationunitid = ? AND"+
                                                " locationdepthvalue = ? AND"+
                                                " locationdepthunitid = ? AND"+
                                                " locationtype = ? ");
        getStmt = conn.prepareStatement(" SELECT locationlatitude, locationlongitude, "+
                                            " locationelevationvalue, locationelevationunitid,"+
                                            " locationdepthvalue, locationdepthunitid, " +
                                            " locationtype FROM location"+
                                            " WHERE locationid = ? "
                                       );
    }
    
    
    /**
     * Inserting the details from a location object into database
     * @param location - Location
     * @return int - the dbid
     */
    
    public int put(Location location)
        throws SQLException {
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
    
    /**
     * Returns the dbid if a location object is given as input
     * @param location - Location
     * @return int - the dbid
     */
    
    public int getDBId(Location location)
        throws SQLException, NotFound {
        insert(location,getDBIdStmt,1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            return rs.getInt("locationid");
        }
        throw new NotFound("location object not found");
    }
    
    
    /**
     * returns the Location Object based on the id
     * @param id - the dbid
     * @return Location
     */
    public Location get(int id) throws SQLException, NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) {
            return extract(rs);
        }
        throw new NotFound(" NO location Id found for the location id = "+id);
    }
    
    
    /**
     * Inserting the values from a Location Object into a preparedStatement
     * @param location - Location
     * @param stmt - PreparedStatement
     * @index - the index
     * @return - returns the resultant index
     */
    
    public int insert(Location location,PreparedStatement stmt, int index)
        throws SQLException {
        stmt.setFloat(index++, location.latitude);
        stmt.setFloat(index++, location.longitude);
        index = jdbcQuantity.insert(location.elevation,stmt,index);
        index = jdbcQuantity.insert(location.depth,stmt,index);
        stmt.setInt(index++, location.type.value());
        return index;
    }
    
    
    /**
     * returns  a Location
     * @param rs - ResultSet
     * @return Location - Location
     */
    
    public Location extract(ResultSet rs) throws SQLException, NotFound {
        return new Location(rs.getFloat("locationlatitude"),
                            rs.getFloat("locationlongitude"),
                            jdbcQuantity.extract(rs.getInt("locationelevationunitid"),
                                                 rs.getDouble("locationelevationvalue")),
                            jdbcQuantity.extract(rs.getInt("locationdepthunitid"),
                                                 rs.getDouble("locationdepthvalue")),
                            edu.iris.Fissures.LocationType.from_int(rs.getInt("locationtype"))
                           );
        
    }
    
    public String getUnitTableName(){ return jdbcQuantity.getUnitTableName(); }
    
    private JDBCQuantity jdbcQuantity;
    
    protected PreparedStatement putStmt;
    
    protected PreparedStatement getDBIdStmt;
    
    protected PreparedStatement getStmt;
    
    private JDBCSequence seq;
} // JDBCLocation
