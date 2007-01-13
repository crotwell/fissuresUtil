package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;

public class JDBCProblemStatus extends JDBCNameTable {

    public JDBCProblemStatus() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblemStatus(Connection conn) throws SQLException {
        super("problemstatus", conn, "status_id", "status_name");
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
