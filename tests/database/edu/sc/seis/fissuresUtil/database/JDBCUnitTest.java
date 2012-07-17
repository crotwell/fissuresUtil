package edu.sc.seis.fissuresUtil.database;



import java.sql.SQLException;

import edu.iris.Fissures.model.UnitImpl;

public class JDBCUnitTest extends JDBCTearDown {
    
    public void setUp() throws SQLException{
        table = new JDBCUnit(ConnMgr.createConnection());
    }
    
    public void testPutAndGet() throws SQLException, NotFound{
        int id = table.put(UnitImpl.CUBIC_METER);
        assertEquals(UnitImpl.CUBIC_METER, table.get(id));
    }
    

    public void testPutAndGetNoCache() throws SQLException, NotFound{
        int id = table.put(UnitImpl.CUBIC_METER);
        table.cache.clear();
        assertEquals(UnitImpl.CUBIC_METER, table.get(id));
        
    }
    public void testGetDBId() throws NotFound, SQLException {
        int dbid = table.put(UnitImpl.CUBIC_METER);
        int gottenid = table.getDBId(UnitImpl.CUBIC_METER);
        assertEquals(dbid, gottenid);
    }

    public void testGetDBIdNoCache() throws NotFound, SQLException {
        int dbid = table.put(UnitImpl.CUBIC_METER);
        table.cache.clear();
        int gottenid = table.getDBId(UnitImpl.CUBIC_METER);
        assertEquals(dbid, gottenid);
    }
    private JDBCUnit table;
} // JDBCUnitTest
