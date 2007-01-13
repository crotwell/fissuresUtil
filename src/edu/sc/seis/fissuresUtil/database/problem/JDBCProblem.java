package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCProblem extends JDBCTable {

    public JDBCProblem() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblem(Connection conn) throws SQLException {
        super("problem", conn);
        stationTable = new JDBCProblemStation(conn);
        typeTable = new JDBCProblemType(conn);
        statusTable = new JDBCProblemStatus(conn);
        seq = new JDBCSequence(conn, "problem_seq");
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/problem/problemchannels.vm");
    }

    public Problem getProblem(int dbId) throws SQLException, NotFound {
        int staId = -1;
        int typeId = -1;
        int statId = -1;
        get.setInt(1, dbId);
        ResultSet rs = get.executeQuery();
        if(rs.next()) {
            staId = rs.getInt("station_id");
            try {
                typeId = rs.getInt("type_id");
            } catch(SQLException e) {
                logger.info("no type_id in database entry");
            }
            try {
                statId = rs.getInt("status_id");
            } catch(SQLException e) {
                logger.info("no status_id in database entry");
            }
        } else {
            throw new NotFound("No problem for id " + dbId);
        }
        return new Problem(stationTable.getName(staId),
                           (typeId != -1 ? typeTable.getName(typeId) : ""),
                           (statId != -1 ? statusTable.getName(statId) : ""));
    }

    public int put(Problem problem) throws SQLException {
        return put(problem.getStationCode(),
                   problem.getType(),
                   problem.getStatus());
    }

    public int put(String station, String type, String status)
            throws SQLException {
        int stationId = stationTable.put(station);
        int typeId = typeTable.put(type);
        int statusId = statusTable.put(status);
        getDbId.setInt(1, stationId);
        getDbId.setInt(2, typeId);
        getDbId.setInt(3, statusId);
        ResultSet rs = getDbId.executeQuery();
        if(rs.next()) {
            return rs.getInt("problem_id");
        } else {
            int id = seq.next();
            put.setInt(1, id);
            put.setInt(2, stationId);
            put.setInt(3, typeId);
            put.setInt(4, statusId);
            put.executeUpdate();
            return id;
        }
    }

    protected PreparedStatement put, get, getDbId;

    private JDBCSequence seq;

    private JDBCProblemStation stationTable;

    private JDBCProblemType typeTable;

    private JDBCProblemStatus statusTable;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCProblem.class);
}
