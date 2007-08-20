package edu.sc.seis.fissuresUtil.database.event;

import java.sql.SQLException;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class ThreadedEventAccessTest extends TestCase {

	public void testThreadedPut() {
		Runnable r = new Runnable() {
			public void run() {
				
				try {
					JDBCEventAccess eventTable = new JDBCEventAccess(ConnMgr.createConnection());
					EventAccessOperations[] events = MockEventAccessOperations
							.createEvents();
					for (int i = 0; i < events.length; i++) {
						eventTable.put(events[i], null, null, null);
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
		Thread[] t = new Thread[10];
		for (int i = 0; i < t.length; i++) {
			t[i] = new Thread(r);
		}
		for (int i = 0; i < t.length; i++) {
			t[i].start();
		}
	}
}
