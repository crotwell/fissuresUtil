package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class ThreadedNetTableTest extends TestCase {

	public void testThreadedPut() {
		Runnable r = new Runnable() {
			public void run() {
				
				try {
					JDBCChannel chanTable = new JDBCChannel(ConnMgr.createConnection());
					Channel chan = MockChannel.createChannel();
					chanTable.put(chan);
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
		r.run();
	}
}
