package edu.sc.seis.fissuresUtil.database;



import edu.iris.Fissures.model.UnitImpl;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCUnitTest extends TestCase {
    public JDBCUnitTest(String testname) { super(testname);  }
    
    public void setUp() throws SQLException{
        table = new JDBCUnit(ConnMgr.getConnection());
    }
    
    public void testPutAndGet() throws SQLException, NotFound{
        int id = table.put(UnitImpl.CUBIC_METER);
        assertEquals(UnitImpl.CUBIC_METER, table.get(id));
    }
    
    public void testGetDBId() throws NotFound, SQLException {
        int dbid = table.put(UnitImpl.CUBIC_METER);
        int gottenid = table.getDBId(UnitImpl.CUBIC_METER);
        assertEquals(dbid, gottenid);
    }
    
    private JDBCUnit table;
} // JDBCUnitTest
