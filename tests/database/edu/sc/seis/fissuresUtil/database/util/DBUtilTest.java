package edu.sc.seis.fissuresUtil.database.util;

import java.sql.Connection;
import java.sql.ResultSet;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import junit.framework.TestCase;

/**
 * @author crotwell Created on Jan 21, 2005
 */
public class DBUtilTest extends TestCase {

    public DBUtilTest(String name) { super(name); }
    
    public void testSequenceExists() throws Exception {
        // for postgres, make sure a createdb fisTest is done first:
        //ConnMgr.setDB(ConnMgr.POSTGRES);
        //ConnMgr.setURL("jdbc:postgresql:fisTest");
        
        Connection conn = ConnMgr.createConnection();
        String seqName = "testDBUtilSeq";
        JDBCSequence seq = new JDBCSequence(conn, seqName);
        assertTrue(seqName+" should exist", DBUtil.sequenceExists(seqName, conn));
    }
}