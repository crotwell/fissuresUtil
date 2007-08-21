package edu.sc.seis.fissuresUtil.database.event;

import java.sql.SQLException;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class ThreadedEventAccessTest extends JDBCTearDown {

    public void testThreadedPut() {
        Runnable r = new Runnable() {

            public void run() {
                try {
                    JDBCEventAccess eventTable = new JDBCEventAccess(ConnMgr.createConnection());
                    EventAccessOperations[] events = MockEventAccessOperations.createEvents();
                    for(int i = 0; i < events.length; i++) {
                        eventTable.put(events[i], null, null, null);
                    }
                } catch(SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        };
        ThreadGroup tgroup = new ThreadGroup("ThreadedEventAccessTest");
        Thread[] t = new Thread[4];
        for(int i = 0; i < t.length; i++) {
            t[i] = new Thread(tgroup, r);
        }
        for(int i = 0; i < t.length; i++) {
            t[i].start();
        }
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}
        r.run();
        while (tgroup.activeCount() > 0) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}
        }
    }
}
