package edu.sc.seis.fissuresUtil.database;

import java.io.IOException;
import junit.framework.TestCase;

public class ConnMgrTest extends TestCase{
    public ConnMgrTest(String name){
        super(name);
    }

    public void testSetDB()throws IOException{
        ConnMgr.setDB();
        assertEquals("org.hsqldb.jdbcDriver", ConnMgr.getSQL("driver"));
        assertEquals("CREATE SEQUENCE ParameterRefSeq",
                     ConnMgr.getSQL("ParameterRefSeq.create"));
        assertEquals("CALL NEXT VALUE FOR ParameterRefSeq",
                     ConnMgr.getSQL("ParameterRefSeq.nextVal"));
    }

    public void testAddPropLocation() throws IOException{
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/event/");
        ConnMgr.setDB();
        assertEquals("Extra event values", ConnMgr.getSQL("Extra.event.values"));
    }
}
