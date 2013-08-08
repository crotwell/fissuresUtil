package edu.sc.seis.fissuresUtil.database;

import static org.junit.Assert.*;

import org.junit.Test;


public class ConnMgrTest {

    @Test
    public void parseOracleURL() {
        try {
        ConnMgr.setURL("jdbc:oracle:oci:ears_test/ears_testpw@dbserv1.iris.washington.edu:1521:ears");
        } catch (RuntimeException e) {}
        assertEquals("user", "ears_test", ConnMgr.getUser());
        assertEquals("passwd", "ears_testpw", ConnMgr.getPass());
    }
    
    @Test
    public void parseOracleURL2() {
        try {
        ConnMgr.setURL("jdbc:oracle:oci:ears_test/ears_testpw@//dbserv1.iris.washington.edu:1521:ears");
        } catch (RuntimeException e) {}
        assertEquals("user", "ears_test", ConnMgr.getUser());
        assertEquals("passwd", "ears_testpw", ConnMgr.getPass());
    }
}
