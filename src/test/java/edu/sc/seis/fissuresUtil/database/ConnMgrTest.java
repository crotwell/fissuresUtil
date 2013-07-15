package edu.sc.seis.fissuresUtil.database;

import static org.junit.Assert.*;

import org.junit.Test;


public class ConnMgrTest {

    @Test
    public void parseOracleURL() {
        ConnMgr.setURL("jdbc:oracle:oci:ears_test/ears_test@dbserv1.iris.washington.edu:1521:ears");
        assertEquals("user", "ears_test", ConnMgr.getUser());
        assertEquals("passwd", "ears_test", ConnMgr.getPass());
    }
    
    @Test
    public void parseOracleURL2() {
        ConnMgr.setURL("jdbc:oracle:oci:ears_test/ears_test@//dbserv1.iris.washington.edu:1521:ears");
        assertEquals("user", "ears_test", ConnMgr.getUser());
        assertEquals("passwd", "ears_test", ConnMgr.getPass());
    }
}
