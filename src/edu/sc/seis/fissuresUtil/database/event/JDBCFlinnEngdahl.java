package edu.sc.seis.fissuresUtil.database.event;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.model.FlinnEngdahlRegionImpl;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCFlinnEngdahl extends EventTable {

    /**
     * Constructor for the class. The following classes are created in this.
     * 1. FlinnEngdahl
     */
    public JDBCFlinnEngdahl(Connection conn) throws SQLException {
        super("flinnengdahl",conn);
        Statement stmt = conn.createStatement();
        //Creating the sequence
        seq = new JDBCSequence(conn, "FlinnEngdahlSeq");

        //creating the table FlinnEngdahl
        if(!DBUtil.tableExists("flinnengdahl", conn)){
            stmt.executeUpdate(" CREATE TABLE flinnengdahl"+
                                   "(flinnengdahlid int primary key, " +
                                   "flinnengdahltype int, "+
                                   "flinnengdahlnumber int)");
        }
        putStmt = conn.prepareStatement( "INSERT INTO flinnengdahl"+
                                            " ( flinnengdahlid,"+
                                            " flinnengdahltype,"+
                                            " flinnengdahlnumber)"+
                                            "VALUES(?,?,?)"
                                       );

        getDBIdStmt = conn.prepareStatement( "SELECT flinnengdahlid FROM flinnengdahl"+
                                                " WHERE flinnengdahltype = ? "+
                                                " AND flinnengdahlnumber = ?"
                                           );

        getStmt = conn.prepareStatement( " SELECT flinnengdahltype , flinnengdahlnumber FROM flinnengdahl"+
                                            " WHERE  flinnengdahlid = ? "
                                       );
    }


    /**
     * puts the FlinnEngdahlRegion object details into a table
     * @param region - FlinnEngdahlRedion
     * @return int - the dbid of the inserted record
     */

    public  int put(FlinnEngdahlRegion region)  throws SQLException {
        try{
            return getDBId(region);
        } catch(NotFound ex) {
            int id = seq.next();
            putStmt.setInt(1,id);
            insert(region,putStmt,2);
            putStmt.executeUpdate();
            return id;
        }
    }


    /**
     * return dbid given the object of FlinnEngdahlRegion
     * @param region - FlinnEngdahlRedion
     * @return int - the dbid
     */
    public int getDBId(FlinnEngdahlRegion region)
        throws SQLException, NotFound {
        insert(region,getDBIdStmt,1);

        ResultSet rs = getDBIdStmt.executeQuery();
        if(rs.next()) {
            return rs.getInt("flinnengdahlid");
        }
        throw new NotFound("region is not found");
    }


    /**
     * Returns the FlinnEngdahlRegion for a given dbid
     * @param id - the dbid
     * @return FlinnEngdahlRegion - the FLinnEngdahlRegion
     */


    public  FlinnEngdahlRegion get(int id)
        throws SQLException,NotFound{
        getStmt.setInt(1,id);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) {
            return extract(rs);
        }
        throw new NotFound("No Region Found for this given id");
    }


    /**
     * Inserts the fields from FlinnEngdahlRegion into a preparedStatement
     * @param region - FlinnEngdahlRegion
     * @param stmt - PreparedStatement
     * @param index - the index
     * @return int - the resulting index
     */
    public int insert(FlinnEngdahlRegion region,PreparedStatement stmt, int index)
        throws SQLException{
        stmt.setInt(index++, region.type.value());
        stmt.setInt(index++, region.number);
        return index;
    }


    /**
     * Returns a FlinnEngdahlRegion given the result set
     * @param rs - the resultset
     * @return FlinnEngdahlRegion
     */
    public  FlinnEngdahlRegionImpl extract(ResultSet rs) throws SQLException{
        return new FlinnEngdahlRegionImpl(edu.iris.Fissures.FlinnEngdahlType.from_int(rs.getInt("flinnengdahltype")),
                                          rs.getInt("flinnengdahlnumber"));
    }

    private JDBCSequence seq;

    protected PreparedStatement getStmt;

    protected PreparedStatement getDBIdStmt;

    protected PreparedStatement putStmt;
} // JDBCFlinnEngdahl
