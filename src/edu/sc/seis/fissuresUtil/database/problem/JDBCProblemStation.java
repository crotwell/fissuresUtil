package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;

public class JDBCProblemStation extends JDBCNameTable {

    public JDBCProblemStation() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblemStation(Connection conn) throws SQLException {
        super("problemstation", conn, "station_id", "station_code");
    }

    protected PreparedStatement getGet() {
        return get;
    }

    protected PreparedStatement getGetDbId() {
        return getDbId;
    }

    protected PreparedStatement getPut() {
        return put;
    }

    protected PreparedStatement put, get, getDbId;
}
