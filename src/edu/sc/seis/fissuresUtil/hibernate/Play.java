package edu.sc.seis.fissuresUtil.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.event.JDBCOrigin;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.fissuresUtil.simple.TimeOMatic;

public class Play {

	public static void main(String[] args) throws SQLException {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.INFO);
			Play mgr = new Play();
			TimeOMatic.start();
			String todo = args[2];
			System.out.println("arg is: " + todo);
			if (todo.equals("schema")) {
				mgr.schema();
			} else if (todo.equals("store")) {
				mgr.createAndStoreEvent("My Event", new Date());
			} else if (todo.equals("retrieve")) {
				mgr.retrieve();
			} else if (todo.equals("oldstore")) {
				mgr.oldStore();
			} else if (todo.equals("oldretrieve")) {
				mgr.oldRetrieve();
				// network
			} else if (todo.equals("storenet")) {
				mgr.createAndStoreNet();
			} else if (todo.equals("retrievenet")) {
				mgr.retrieveNet();
			} else if (todo.equals("oldstorenet")) {
				mgr.oldStoreNet();
			} else if (todo.equals("oldretrievenet")) {
				mgr.oldRetrieveNet();
			} else {
				System.err.println("Unknown arg: " + todo);
			}
			TimeOMatic.print("end");
		} catch (Throwable t) {
			logger.error("big problem!", t);
		}
	}

	private Station[] createStation() {
		return MockStation.createMultiSplendoredStations(2, 2);
	}

	private ChannelImpl[] createChannel() {
		ArrayList out = new ArrayList();
		Station[] sta = createStation();
		for (int i = 0; i < sta.length; i++) {
		    ChannelImpl[] c = MockChannel.createMotionVector(sta[i]);
		    for(int j = 0; j < c.length; j++) {
		        out.add(c[j]);
            }
			
		}
		return (ChannelImpl[]) out.toArray(new ChannelImpl[0]);
	}

	private void oldRetrieveNet() throws SQLException {
		JDBCStation j = new JDBCStation(getConn());
		Station[] s = j.getAllStations();
	}

	private void oldStoreNet() throws SQLException {
		JDBCStation j = new JDBCStation(getConn());
		Station[] s = createStation();
		for (int i = 0; i < s.length; i++) {
			j.put(s[i]);
		}
	}

	private void retrieveNet() {
		NetworkDB netDB = new NetworkDB();
		Station[] out = netDB.getAllStations();
		System.out.println("retrieved " + out.length + " stations");
	}

	private void createAndStoreNet() {
		NetworkDB netDB = new NetworkDB();
		try {
            netDB.put(MockNetworkAttr.createNetworkAttr());
            netDB.put(MockNetworkAttr.createOtherNetworkAttr());
        } catch (ConstraintViolationException e) {
            logger.debug("Caught e, going on", e);
            netDB.rollback();
        }
        Station[] s = createStation();
        for (int i = 0; i < s.length; i++) {
            System.out.println("preput station " + i);
            netDB.put(s[i]);
            System.out.println("postput station " + i);
        }
		ChannelImpl[] chan = createChannel();
		for(int i = 0; i < chan.length; i++) {
            netDB.put(chan[i]);
        }
		netDB.commit();
	}

	private void schema() {
		SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
		update.execute(false, true);
	}

	private CacheEvent[] getOrigins() {
		Origin[] origins = MockOrigin.createOrigins(10);
		CacheEvent[] out = new CacheEvent[origins.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = new CacheEvent(MockEventAttr.create(i % 700), origins[i]);
		}
		return out;
	}

	private void createAndStoreEvent(String title, Date theDate) {
		EventDB eventDB = new EventDB();
		CacheEvent[] origins = getOrigins();
		for (int i = 0; i < origins.length; i++) {
			eventDB.put(origins[i]);
		}
		for (int i = 0; i < origins.length; i++) {
			System.out.println("before origin: " + i + "  dbid="
					+ origins[i].getDbId());
		}
		eventDB.commit();
		for (int i = 0; i < origins.length; i++) {
			System.out.println("origin: " + i + "  dbid="
					+ origins[i].getDbId());
		}
	}

	private void retrieve() {
		EventFinderQuery q = new EventFinderQuery();
		q.setArea(new GlobalAreaImpl());
		q.setMaxDepth(1000);
		q.setMaxMag(10);
		q.setMinDepth(0);
		q.setTime(new MicroSecondTimeRange(new MicroSecondDate(new Time(
				"2006-07-01T000000Z", -1)), new MicroSecondDate(new Time(
				"2007-10-01T000000Z", -1))));
		q.setMinMag(0);
		EventDB eventDB = new EventDB(HibernateUtil.getSessionFactory());
		CacheEvent[] events = eventDB.query(q);
		System.out.println("Got " + events.length + " origins");
		HibernateUtil.getSessionFactory().close();
	}

	private void oldStore() throws SQLException {
		CacheEvent[] origins = getOrigins();
		JDBCEventAccess jdbcEA = new JDBCEventAccess(getConn());
		JDBCOrigin jdbcOrigin = jdbcEA.getJDBCOrigin();
		for (int i = 0; i < origins.length; i++) {
			jdbcEA.put(origins[i], null, null, null);
		}
	}

	private void oldRetrieve() throws SQLException {
		JDBCEventAccess jdbcEA = new JDBCEventAccess(getConn());
		List out = jdbcEA.getAllEventsList();
		System.out.println("Got " + out.size() + " origins");
	}

	private Connection getConn() throws SQLException {
		ConnMgr.setURL("jdbc:hsqldb:hsql://localhost");
		return ConnMgr.createConnection();
	}

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(Play.class);
}
