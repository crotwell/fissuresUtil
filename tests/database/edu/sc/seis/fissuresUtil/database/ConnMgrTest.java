package edu.sc.seis.fissuresUtil.database;

import java.io.IOException;
import junit.framework.TestCase;

public class ConnMgrTest extends TestCase {

    public void testAddPropLocation() throws IOException {
        ConnMgr.addPropsLocation("edu/sc/seis/fissuresUtil/database/event/");
        ConnMgr.setDB();
        assertEquals("Extra event values", ConnMgr.getSQL("Extra.event.values"));
    }
}
