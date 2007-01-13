package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;

public class JDBCProblemType extends JDBCNameTable {

    public JDBCProblemType() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblemType(Connection conn) throws SQLException {
        super("problemtype", conn, "type_id", "type_name");
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
