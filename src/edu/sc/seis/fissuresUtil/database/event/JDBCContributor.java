package edu.sc.seis.fissuresUtil.database.event;

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
import java.util.ArrayList;

public class JDBCContributor extends JDBCTable {
    public JDBCContributor(Connection conn) throws SQLException {
        super("contributor", conn);
        seq = new JDBCSequence(conn, "ContributorSeq");
        Statement stmt = conn.createStatement();
        if(!DBUtil.tableExists("contributor", conn)){
            stmt.executeUpdate(ConnMgr.getSQL("contributor.create"));
        }
        putStmt = conn.prepareStatement(" INSERT INTO contributor "+
                                            " (contributorid, "+
                                            " contributor)"+
                                            " VALUES( ?, ?) ");
        getStmt = conn.prepareStatement(" SELECT contributor FROM contributor "+
                                            " WHERE contributorid = ? ");
        getDBIdStmt = conn.prepareStatement(" SELECT contributorid  "+
                                                " FROM contributor "+
                                                " WHERE contributor = ?");
        getAllStmt = conn.prepareStatement(" SELECT DISTINCT contributor "+
                                               " FROM contributor");
    }
    
    /**
     * This function inserts a row into the Contributor table
     * @param contributor - the contributor name
     * @return int - the dbid
     */
    public int put(String contributor) throws SQLException {
        try {
            return getDBId(contributor);
        } catch(NotFound nfe) {
            int id = seq.next();
            putStmt.setInt(1,id);
            putStmt.setString(2, contributor);
            putStmt.executeUpdate();
            return id;
        }
    }
    
    /***
     * This function returns the dbid given the contributor name
     * @ param contributor - the contributor name
     * @ return int - the dbid
     */
    public int getDBId(String  contributor) throws SQLException, NotFound {
        getDBIdStmt.setString(1, contributor);
        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            return rs.getInt("contributorid");
        }
        throw new NotFound("the entry for the given origin object is not found");
    }
    
    
    /**
     * This method returns the contributor name given the dbid
     * @param id - dbid
     * @return String - the name of the contributor
     */
    public String get(int id) throws SQLException, NotFound {
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) return rs.getString("contributor");
        throw new NotFound(" there is no Contributor name is associated  to the id "+id);
    }
    
    /**
     * This method returns the contributor names
     * @return String[] - an array of all the catalogs
     */
    public String[] getAll() throws SQLException {
        ArrayList aList = new ArrayList();
        ResultSet rs = getAllStmt.executeQuery();
        
        while( rs.next() ) {
            aList.add(rs.getString("contributor"));
        }
        String[] contributors = new String[aList.size()];
        contributors = (String[])aList.toArray(contributors);
        return contributors;
        
    }
    
    protected PreparedStatement getDBIdStmt;
    
    protected PreparedStatement getStmt;
    
    protected PreparedStatement putStmt;
    
    protected PreparedStatement getAllStmt;
    
    private JDBCSequence seq;
    
} // JDBCContributor
