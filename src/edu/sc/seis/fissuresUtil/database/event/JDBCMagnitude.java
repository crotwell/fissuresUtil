package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCMagnitude  extends EventTable {
    public JDBCMagnitude(Connection conn) throws SQLException{
        this(conn, new JDBCContributor(conn));
    }

    public JDBCMagnitude(Connection conn, JDBCContributor jdbcContributor) throws SQLException {
        super("magnitude", conn);
        this.jdbcContributor = jdbcContributor;
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("magnitude", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("magnitude.create"));
        }
        putStmt = conn.prepareStatement("INSERT INTO magnitude"+
                                            " ( magnitudetype,"+
                                            " magnitudevalue,"+
                                            " magnitudecontributorid,"+
                                            " originid)"+
                                            " VALUES(?,?,?,?)");
        exists = conn.prepareStatement(" SELECT * FROM magnitude"+
                                           " WHERE magnitudetype = ? AND"+
                                           " magnitudevalue = ? AND "+
                                           " magnitudecontributorid = ? AND "+
                                           " originid = ?");
        getStmt = conn.prepareStatement(" SELECT magnitudetype ,"+
                                            " magnitudevalue ,"+
                                            " magnitudecontributorid FROM magnitude"+
                                            " WHERE originid = ?");
        dropAll = conn.prepareStatement("DELETE FROM magnitude");
    }

    public boolean exists(Magnitude magnitude, int originId) throws SQLException, NotFound {
        insert(magnitude,originId,exists);
        ResultSet rs = exists.executeQuery();
        if(rs.next())  return true;
        throw new NotFound("magnitude is not found");
    }

    public void insert(Magnitude magnitude,int originId, PreparedStatement stmt)
        throws SQLException {
        stmt.setString(1,magnitude.type);
        stmt.setFloat(2,magnitude.value);
        stmt.setInt(3,jdbcContributor.put(magnitude.contributor));
        stmt.setInt(4, originId);
    }

    public void put(Magnitude magnitude, int originId) throws SQLException{
        try {
            exists(magnitude, originId);
        } catch(NotFound e) {
            insert(magnitude,originId,putStmt);
            putStmt.executeUpdate();
        }
    }

    /**
     * This method is used to put magnitudes (array) into the database
     * @ param magnitudes - array of Magnitude
     */
    public void put(Magnitude[] magnitudes, int originId) throws SQLException{
        for(int i=0; i<magnitudes.length; i++ ) put(magnitudes[i], originId);
    }


    /**
     * returns the Magnitudes given the dbid
     * @param id - dbid
     * @return - Magnitude.
     */
    public Magnitude[] get(int id) throws SQLException,NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        List mags = new ArrayList();
        while(rs.next()){
            mags.add(extract(rs));
        }
        return (Magnitude[])mags.toArray(new Magnitude[mags.size()]);
    }

    /**
     * returns a magnitude object given the resultset.
     * @param rs - ResultSet
     * @return - Magnitude.
     */
    public Magnitude extract(ResultSet rs) throws SQLException, NotFound {
        return new Magnitude(rs.getString("magnitudetype"),
                             rs.getFloat("magnitudevalue"),
                             jdbcContributor.get(rs.getInt("magnitudecontributorid")));
    }

    protected JDBCContributor jdbcContributor;

    protected PreparedStatement getStmt;

    protected PreparedStatement exists;

    protected PreparedStatement putStmt;
    
    protected PreparedStatement dropAll;

    public void clean() throws SQLException {
        dropAll.executeUpdate();
    }
} // JDBCMagnitude

