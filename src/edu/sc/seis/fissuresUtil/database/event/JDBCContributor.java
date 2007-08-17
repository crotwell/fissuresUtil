package edu.sc.seis.fissuresUtil.database.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCContributor extends EventTable {
    public JDBCContributor(Connection conn) throws SQLException {
        super("contributor", conn);
        seq = new JDBCSequence(conn, "ContributorSeq");
        Statement stmt = conn.createStatement();
        TableSetup.setup(this,
        "edu/sc/seis/fissuresUtil/database/props/event/default.props");
        putStmt = conn.prepareStatement(" INSERT INTO contributor "+
                                            " (contributor_id, "+
                                            " contributor_name)"+
                                            " VALUES( ?, ?) ");
        getStmt = conn.prepareStatement(" SELECT contributor_name FROM contributor "+
                                            " WHERE contributor_id = ? ");
        getDBIdStmt = conn.prepareStatement(" SELECT contributor_id  "+
                                                " FROM contributor "+
                                                " WHERE contributor_name = ?");
        getAllStmt = conn.prepareStatement(" SELECT DISTINCT contributor_id "+
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
            return rs.getInt("contributor_id");
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
        if(rs.next()) return extract(rs);
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
            aList.add(rs.getString("contributor_name"));
        }
        String[] contributors = new String[aList.size()];
        contributors = (String[])aList.toArray(contributors);
        return contributors;

    }

    public String extract(ResultSet rs) throws SQLException {
        return rs.getString("contributor_name");
    }
    protected PreparedStatement getDBIdStmt;

    protected PreparedStatement getStmt;

    protected PreparedStatement putStmt;

    protected PreparedStatement getAllStmt;

    private JDBCSequence seq;

} // JDBCContributor
