package edu.sc.seis.fissuresUtil.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
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
            if(!mgr.doIt(todo)) {
                System.err.println("Unknown arg: " + todo);
            }
            TimeOMatic.print("end");
        } catch(Throwable t) {
            logger.error("big problem!", t);
        }
    }

    protected boolean doIt(String todo) throws Exception {
        if(todo.equals("schema")) {
            schema();
        } else if(todo.equals("store")) {
            createAndStoreEvent("My Event", new Date());
        } else if(todo.equals("retrieve")) {
            retrieve();
        } else if(todo.equals("oldstore")) {
            oldStore();
        } else if(todo.equals("oldretrieve")) {
            oldRetrieve();
            // network
        } else if(todo.equals("storenet")) {
            createAndStoreNet();
        } else if(todo.equals("retrievenet")) {
            retrieveNet();
        } else if(todo.equals("oldstorenet")) {
            oldStoreNet();
        } else if(todo.equals("oldretrievenet")) {
            oldRetrieveNet();
        } else {
            return false;
        }
        return true;
    }

    private StationImpl[] createStation() {
        return MockStation.createMultiSplendoredStations(2, 2);
    }

    private ChannelImpl[] createChannel() {
        ArrayList out = new ArrayList();
        Station[] sta = createStation();
        for(int i = 0; i < sta.length; i++) {
            ChannelImpl[] c = MockChannel.createMotionVector(sta[i]);
            for(int j = 0; j < c.length; j++) {
                out.add(c[j]);
            }
        }
        return (ChannelImpl[])out.toArray(new ChannelImpl[0]);
    }

    private void oldRetrieveNet() throws SQLException {
        JDBCStation j = new JDBCStation(getConn());
        Station[] s = j.getAllStations();
    }

    private void oldStoreNet() throws SQLException {
        JDBCStation j = new JDBCStation(getConn());
        Station[] s = createStation();
        for(int i = 0; i < s.length; i++) {
            j.put(s[i]);
        }
    }

    private void retrieveNet() {
        NetworkDB netDB = new NetworkDB();
        StationImpl[] out = netDB.getAllStations();
        ChannelImpl[] chanout = netDB.getChannelsForStation(out[0]);
        System.out.println("retrieved " + out.length + " stations");
        for(int i = 0; i < chanout.length; i++) {
            System.out.println("Channel: "
                    + ChannelIdUtil.toString(chanout[i].get_id()));
        }
    }

    private void createAndStoreNet() {
        NetworkDB netDB = new NetworkDB();
        try {
            netDB.put(MockNetworkAttr.createNetworkAttr());
            netDB.put(MockNetworkAttr.createOtherNetworkAttr());
        } catch(ConstraintViolationException e) {
            logger.debug("Caught e, going on", e);
            netDB.rollback();
        }
        StationImpl[] s = createStation();
        for(int i = 0; i < s.length; i++) {
            System.out.println("preput station " + i);
            netDB.put(s[i]);
            System.out.println("postput station " + i);
        }
        ChannelImpl[] chan = createChannel();
        for(int i = 0; i < chan.length; i++) {
            netDB.put(chan[i]);
        }
        netDB.commit();
        // try reattach
        netDB.getSession().lock(s[0], LockMode.NONE);
        netDB.getSession().lock(chan[0], LockMode.NONE);
    }

    protected void schema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }

    private CacheEvent[] getOrigins() {
        OriginImpl[] origins = MockOrigin.createOrigins(10);
        CacheEvent[] out = new CacheEvent[origins.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = new CacheEvent(MockEventAttr.create(i % 700), origins[i]);
        }
        return out;
    }

    private void createAndStoreEvent(String title, Date theDate) {
        EventDB eventDB = new EventDB();
        CacheEvent[] origins = getOrigins();
        for(int i = 0; i < origins.length; i++) {
            eventDB.put(origins[i]);
        }
        eventDB.commit();
        for(int i = 0; i < origins.length; i++) {
            System.out.println("origin: " + i + "  dbid="
                    + origins[i].getDbid());
        }
        // try reattach
        eventDB.getSession().lock(origins[0], LockMode.NONE);
    }

    private void retrieve() throws NoPreferredOrigin {
        EventFinderQuery q = new EventFinderQuery();
        q.setArea(new GlobalAreaImpl());
        q.setMaxDepth(1000);
        q.setMaxMag(10);
        q.setMinDepth(0);
        q.setTime(new MicroSecondTimeRange(new MicroSecondDate(new Time("2006-07-01T000000Z",
                                                                        -1)),
                                           new MicroSecondDate(new Time("2008-10-01T000000Z",
                                                                        -1))));
        q.setMinMag(0);
        EventDB eventDB = new EventDB();
        CacheEvent[] events = eventDB.query(q);
        System.out.println("Got " + events.length + " origins");
        for(int i = 0; i < events.length; i++) {
            System.out.println("Event " + i + "  "
                    + events[i].get_preferred_origin().origin_time + "  "
                    + events[i].get_preferred_origin().my_location + "  "
                    + events[i].get_preferred_origin().my_location.type + "  "
                    + events[i].get_attributes());
        }
        HibernateUtil.getSessionFactory().close();
    }

    private void oldStore() throws SQLException {
        CacheEvent[] origins = getOrigins();
        JDBCEventAccess jdbcEA = new JDBCEventAccess(getConn());
        JDBCOrigin jdbcOrigin = jdbcEA.getJDBCOrigin();
        for(int i = 0; i < origins.length; i++) {
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Play.class);
}
