package edu.sc.seis.fissuresUtil.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SeisInfoDb.java
 *
 *
 * Created: Fri Feb  7 10:45:48 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SeisInfoDb extends AbstractDb{
    private SeisInfoDb (String directoryName, String databaseName){
        super(directoryName, databaseName);
    create();
    }

    public static SeisInfoDb getSeisInfoDb(String directoryName, String databaseName) {
    if(seisInfoDb == null) {
        seisInfoDb = new SeisInfoDb(directoryName, databaseName);
    }
    return seisInfoDb;
    }

    public void create() {

    try {

        connection = getConnection();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(" CREATE TABLE seisInfoDb ( "+
                   " seisName VARCHAR_IGNORECASE , "+
                   " fileids VARCHAR_IGNORECASE ) ");

    } catch(SQLException sqle) {
        // sqle.printStackTrace();
    }

    try {
        insertStmt = connection.prepareStatement(" INSERT INTO seisInfoDb "+
                             " VALUES (?, ?) ");
        getStmt = connection.prepareStatement(" SELECT fileids FROM seisInfoDb "+
                          " WHERE seisName = ? ");
    } catch(SQLException sqle) {
        // sqle.printStackTrace();
    }

    }


    public void insert(String seisName, String fileids) {

    try {
        insertStmt.setString(1, seisName);
        insertStmt.setString(2, fileids);
        insertStmt.executeUpdate();
    } catch(SQLException sqle) {
        sqle.printStackTrace();
    }
    }

    public String getFileIds(String seisName) {
    try {
        getStmt.setString(1, seisName);
        ResultSet rs = getStmt.executeQuery();
        if(rs.next()) {
        return rs.getString("fileids");
        }
    } catch(SQLException sqle) {
        sqle.printStackTrace();
    }
    return null;
    }

    private PreparedStatement insertStmt;

    private PreparedStatement getStmt;

    private static SeisInfoDb seisInfoDb;

}// SeisInfoDb
