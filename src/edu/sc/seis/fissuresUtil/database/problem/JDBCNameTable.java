package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public abstract class JDBCNameTable extends JDBCTable {

    public JDBCNameTable(String name,
                         Connection conn,
                         String idFieldName,
                         String nameFieldName) throws SQLException {
        super(name, conn);
        TableSetup.setup(this,
                         "edu/sc/seis/fissuresUtil/database/problem/problemchannels.vm");
        this.idFieldName = idFieldName;
        this.nameFieldName = nameFieldName;
        seq = new JDBCSequence(conn, name + "_seq");
    }

    public String getName(int dbId) throws SQLException, NotFound {
        PreparedStatement get = getGet();
        get.setInt(1, dbId);
        ResultSet rs = get.executeQuery();
        if(rs.next()) {
            return rs.getString(nameFieldName);
        }
        throw new NotFound("No problem type for id " + dbId);
    }

    public int put(String name) throws SQLException {
        PreparedStatement getDbId = getGetDbId();
        System.out.println(getDbId);
        getDbId.setString(1, name);
        ResultSet rs = getDbId.executeQuery();
        if(rs.next()) {
            return rs.getInt(idFieldName);
        } else {
            int id = seq.next();
            PreparedStatement put = getPut();
            put.setInt(1, id);
            put.setString(2, name);
            put.executeUpdate();
            return id;
        }
    }

    protected abstract PreparedStatement getGet();

    protected abstract PreparedStatement getPut();

    protected abstract PreparedStatement getGetDbId();

    private String idFieldName, nameFieldName;

    private JDBCSequence seq;
}
