package edu.sc.seis.fissuresUtil.database.event;


import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfEvent.MockEventAttr;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

public class JDBCEventAttrTest extends TestCase {
    public JDBCEventAttrTest(String testname)throws SQLException  {
        super(testname);
        eventAttrs.add(MockEventAttr.create("name1", 50));
        eventAttrs.add(MockEventAttr.create("name2", 60));
        eventAttrs.add(MockEventAttr.create("name3", 70));
        jdbcEventAttr = new JDBCEventAttr(ConnMgr.getConnection());
    }
    
    public void testEventInOut() throws SQLException, NotFound {
        Iterator it = eventAttrs.iterator();
        while (it.hasNext()) {
            EventAttr eventAttr = (EventAttr)it.next();
            int dbid = jdbcEventAttr.put(eventAttr);
            EventAttr outEvent = jdbcEventAttr.get(dbid);
            assertEquals(eventAttr.name, outEvent.name);
            assertEquals(eventAttr.region.number, outEvent.region.number);
        }
    }
    
    public void testDoubleInsert() throws SQLException {
        Iterator it = eventAttrs.iterator();
        while (it.hasNext()) {
           EventAttr eventAttr = (EventAttr)it.next();
            int dbidA = jdbcEventAttr.put(eventAttr);
            int dbidB = jdbcEventAttr.put(eventAttr);
            assertEquals(dbidA, dbidB);
        }
    }
    
    private List eventAttrs = new ArrayList();
    
    private JDBCEventAttr jdbcEventAttr;
}
