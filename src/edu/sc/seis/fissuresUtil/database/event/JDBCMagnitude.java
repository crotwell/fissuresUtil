package edu.sc.seis.fissuresUtil.database.event;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCMagnitude  extends JDBCTable {
    public JDBCMagnitude(Connection conn) throws SQLException{
        this(conn, new JDBCContributor(conn));
    }
    
    
    public JDBCMagnitude(Connection conn, JDBCContributor jdbcContributor) throws SQLException {
        super("magnitude", conn);
        this.jdbcContributor = jdbcContributor;
        Statement stmt = conn.createStatement();
        seq = new JDBCSequence(conn, "MagnitudeSeq");
        if(!DBUtil.tableExists("magnitude", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("magnitude.create"));
        }
        putStmt = conn.prepareStatement("INSERT INTO magnitude"+
                                            " (magnitudeid,"+
                                            " magnitudetype,"+
                                            " magnitudevalue,"+
                                            " magnitudecontributorid)"+
                                            " VALUES(?,?,?,?)");
        getDBIdStmt = conn.prepareStatement(" SELECT magnitudeid FROM magnitude"+
                                                " WHERE magnitudetype = ? AND"+
                                                " magnitudevalue = ? AND "+
                                                " magnitudecontributorid = ? ");
        getStmt = conn.prepareStatement(" SELECT magnitudetype ,"+
                                            " magnitudevalue ,"+
                                            " magnitudecontributorid FROM magnitude"+
                                            " WHERE magnitudeid = ?");
    }
    
    /**
     * This function returns the dbid given the magnitude object.
     * @param magnitude - the Magnitude
     * @return dbid
     */
    public int getDBId(Magnitude magnitude) throws SQLException, NotFound {
        insert(magnitude,getDBIdStmt,1);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            return rs.getInt("magnitudeid");
        }
        throw new NotFound("magnitude is not found");
    }
    
    
    /**
     * This inserts the details of magnitude into a preparedStatement
     * @param magnitude - Magnitude.
     * @param stmt - PreparedStatement
     * @param index - the index
     * @return - the resulting index.
     */
    public int insert(Magnitude magnitude,PreparedStatement stmt,int index)
        throws SQLException {
        stmt.setString(index++,magnitude.type);
        stmt.setFloat(index++,magnitude.value);
        stmt.setInt(index++,jdbcContributor.put(magnitude.contributor));
        return index;
    }
    
    
    /**
     * Inserts the details of magnitude into the database and returns the dbid
     * @param magnitude - Magnitude.
     * @return int - dbid
     */
    public int put(Magnitude magnitude) throws SQLException{
        try {
            return getDBId(magnitude);
        } catch(NotFound e) {
            int id = seq.next();
            putStmt.setInt(1,id);
            insert(magnitude,putStmt,2);
            putStmt.executeUpdate();
            return id;
            
        }
    }
    
    /**
     * This method is used to put magnitudes (array) into the database
     * @ param magnitudes - array of Magnitude
     */
    public void put(Magnitude[] magnitudes) throws SQLException{
        for(int i=0; i<magnitudes.length; i++ ) put(magnitudes[i]);
    }
    
    
    /**
     * returns the Magnitudes given the dbid
     * @param id - dbid
     * @return - Magnitude.
     */
    public Magnitude get(int id) throws SQLException,NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next())  return extract(rs);
        throw new NotFound("NO Magnitude is found for the given id");
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
    
    protected PreparedStatement getDBIdStmt;
    
    protected PreparedStatement putStmt;
    
    private JDBCSequence seq;
} // JDBCMagnitude

