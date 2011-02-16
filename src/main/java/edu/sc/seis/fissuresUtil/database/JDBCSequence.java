package edu.sc.seis.fissuresUtil.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCSequence{
    
    public JDBCSequence(Connection conn, String name)throws SQLException{
        try{
            synchronized(JDBCSequence.class) {
                if(!DBUtil.sequenceExists(name, conn)){
                    conn.createStatement().executeUpdate(initCreateStmt(conn, name));
                }
            }
        }catch(SQLException e){
            logger.info("Sequence must already exist for "+name, e);
        }
        nextVal = conn.prepareStatement(initNextValStmt(conn, name));
    }

    public int next() throws SQLException {
        ResultSet rs = nextVal.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private static String initNextValStmt(Connection conn, String seqName) throws SQLException {
        if (conn.getMetaData().getURL().startsWith("jdbc:postgresql:")) {
            // assume postgres style nextval stmt
            return "SELECT NEXTVAL ('"+seqName+"')";
        } else if (conn.getMetaData().getURL().startsWith("jdbc:hsqldb:")) {
            // assume hsqldb style nextval stmt
            return "CALL NEXT VALUE FOR "+seqName;
        } else {
            // try from props
            return ConnMgr.getSQL(seqName + ".nextVal");
        }
    }
    
    private static String initCreateStmt(Connection conn, String seqName) throws SQLException {
        if (conn.getMetaData().getURL().startsWith("jdbc:postgresql:") || conn.getMetaData().getURL().startsWith("jdbc:hsqldb:")) {
            // assume postgres/hsqldb style create stmt
            return "CREATE SEQUENCE "+seqName;
        } else {
            // try from props
            return ConnMgr.getSQL(seqName + ".create");
        }
    }
    
    private PreparedStatement nextVal;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JDBCSequence.class);
}
