package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;

public class JDBCProblemComponent extends JDBCNameTable {

    public JDBCProblemComponent() throws SQLException {
        this(new ConnectionCreator(new String[0]).createConnection());
    }

    public JDBCProblemComponent(Connection conn) throws SQLException {
        super("problemcomponent", conn, "component_id", "component_name");
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
