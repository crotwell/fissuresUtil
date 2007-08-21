package edu.sc.seis.fissuresUtil.database.network;

import java.sql.SQLException;

import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class ThreadedNetTableTest extends JDBCTearDown {

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
        ThreadGroup tgroup = new ThreadGroup("ThreadedNetTableTest");
        Thread[] t = new Thread[4];
        for(int i = 0; i < t.length; i++) {
            t[i] = new Thread(tgroup, r);
        }
        for(int i = 0; i < t.length; i++) {
            t[i].start();
        }
        try {
            Thread.sleep(100);
        } catch(InterruptedException e) {}
        r.run();
        while (tgroup.activeCount() > 0) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}
        }
	}
}
