package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.model.TimeUtils;


/**
 * @author crotwell
 * Created on Sep 15, 2004
 */
public class JDBCTimeTest extends TestCase {
    
    public JDBCTimeTest(String testname) { super(testname);  }
    
    public void setUp() throws SQLException{
        table = new JDBCTime(ConnMgr.createConnection());
    }
    
    public void testPutAndGet() throws SQLException, NotFound{
        int id = table.put(TimeUtils.future.getFissuresTime());
        assertEquals(TimeUtils.future.getFissuresTime().date_time, table.get(id).date_time);
    }
    
    public void testDoublePut() throws SQLException {
        assertEquals(table.put(TimeUtils.future.getFissuresTime()),
                     table.put(TimeUtils.future.getFissuresTime()));
    }
    
    private JDBCTime table;
}
