package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.UnitBase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.DefaultExtractor;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public abstract class AbstractHibernateDB {

    public static boolean DEBUG_SESSION_CREATION = true;
    
    public static int DEBUG_SESSION_CREATION_SECONDS = 300;
    
    public AbstractHibernateDB() {
        logger.debug("init "+this);
    }

    /** this should probably only be called for postgres databases. */
    public static Object getTXID() {
        Query query = getSession().createSQLQuery("select virtualtransaction from pg_locks where pid = pg_backend_pid()");
        return query.list().get(0);
    }
    
    /** check common units to make sure in db
     * 
     */
    private static synchronized void saveCommonUnits() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.beginTransaction();
        Query q = s.createQuery("From edu.iris.Fissures.model.UnitImpl");
        List<UnitImpl> result = q.list();
        if (result.size() == 0) {
            // only save if no units in database
            saveCommonUnit(s, UnitImpl.METER);
            saveCommonUnit(s, UnitImpl.KILOMETER);
            saveCommonUnit(s, UnitImpl.SECOND);
            saveCommonUnit(s, UnitImpl.METER_PER_SECOND);
        }
        s.getTransaction().commit();
        s.close();
        commonUnitsSaved = true;
    }
    
    private static synchronized void saveCommonUnit(Session s, UnitImpl unitToAdd) {
        if ( ! unitToAdd.isBaseUnit()) {
            for (UnitImpl subU : unitToAdd.getSubUnitsList()) {
                saveCommonUnit(s, subU);
            }
        }
        s.saveOrUpdate(unitToAdd);
        logger.debug("save "+unitToAdd+" to database");
    }
    
    private static void loadUnits(Session s) {
        Query q = s.createQuery("From edu.iris.Fissures.model.UnitImpl");
        List<UnitImpl> result = q.list();
        getUnitCache().addAll(result);
    }

    public static void deploySchema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }

    protected static Session createSession() {
        if (commonUnitsSaved == false) {
            // only do this the first time
            saveCommonUnits();
        }
        final Session cacheSession = HibernateUtil.getSessionFactory().openSession();
        cacheSession.beginTransaction();
        //logger.debug("TRANSACTION Begin on " + cacheSession);
        if (DEBUG_SESSION_CREATION) {
            knownSessions.add(new SessionStackTrace(cacheSession, 
                                                    Thread.currentThread().getStackTrace()));
        }
        return cacheSession;
    }

    public static StatelessSession getReadOnlySession() {
        return HibernateUtil.getSessionFactory().openStatelessSession();
    }

    public static boolean isSessionOpen() {
        return sessionTL.get() != null;
    }
    
    public static Session getSession() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            s = createSession();
            sessionTL.set(s);
        }
        return s;
    }

    public static void flush() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            throw new RuntimeException("Can not flush before session creation");
        }
        s.flush();
    }

    /** commits the current session that is associated with the current thread. */
    public static void commit() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            // no session here, nothing to do
            logger.info("Commit session before creation, nothing to do");
            return;
        }
        logger.debug("TRANSACTION Commit on " + s+"  "+getTXID());
        sessionTL.set(null);
        unitCacheTL.set(null);
        s.getTransaction().commit();
        s.close();
    }

    /** rolls back the current session that is associated with the current thread. */
    public static void rollback() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            //nothing to do
            return;
        }
        //logger.debug("TRANSACTION Rollback on " + s);
        sessionTL.set(null);
        unitCacheTL.set(null);
        s.getTransaction().rollback();
        s.close();
    }

    public static void internUnit(Location loc) {
        internUnit(loc.depth);
        internUnit(loc.elevation);
    }
    public static void internUnit(Quantity q) {
        q.the_units = intern((UnitImpl)q.the_units);
    }

    protected static UnitImpl intern(UnitImpl unit) {
        // make sure unit not already in db
        if (unit == null || (unit.getDbid() != null && unit.getDbid() != 0)) {
            return unit;
        }
        HashSet<UnitImpl> unitCache = getUnitCache();
        if(unitCache.size() == 0) {
            loadUnits(getSession());
        }
        Iterator<UnitImpl> it = unitCache.iterator();
        while(it.hasNext()) {
            UnitImpl internUnit = it.next();
            if(unit.equals(internUnit)) {
                return internUnit;
            }
        }
        if (unit.getBaseUnit().equals(UnitBase.COMPOSITE)) {
            UnitImpl[] internedSubUnits = new UnitImpl[unit.getNumSubUnits()];
            for(int i = 0; i < unit.getNumSubUnits(); i++) {
                internedSubUnits[i] = intern(unit.getSubUnit(i));
            }
            unit = new UnitImpl(internedSubUnits,
                                unit.getPower(),
                                unit.name,
                                unit.getMultiFactor(),
                                unit.getExponent());
        }
        getSession().save(unit);
        unitCache.add(unit);
        return unit;
    }

    protected static HashSet<UnitImpl> getUnitCache() {
        HashSet<UnitImpl> out = unitCacheTL.get();
        if(out == null) {
            out = new HashSet<UnitImpl>();
            unitCacheTL.set(out);
        }
        return out;
    }

    private static ThreadLocal<HashSet<UnitImpl>> unitCacheTL = new ThreadLocal<HashSet<UnitImpl>>() {

        protected synchronized HashSet<UnitImpl> initialValue() {
            return new HashSet<UnitImpl>();
        }
    };
    
    private static boolean commonUnitsSaved = false;

    private static ThreadLocal<Session> sessionTL = new ThreadLocal<Session>();
    
    private static List<SessionStackTrace> knownSessions = Collections.synchronizedList(new LinkedList<SessionStackTrace>());

    private static TimeInterval MAX_SESSION_LIFE = new TimeInterval(300, UnitImpl.SECOND);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractHibernateDB.class);

    static {
        GlobalExceptionHandler.add(new DefaultExtractor() {

            public boolean canExtract(Throwable throwable) {
                return (throwable instanceof java.sql.SQLException);
            }

            public String extract(Throwable throwable) {
                return super.extract(throwable);
            }

            public Throwable getSubThrowable(Throwable throwable) {
                if(throwable instanceof java.sql.SQLException) {
                    return ((java.sql.SQLException)throwable).getNextException();
                }
                return null;
            }
        });

        if (DEBUG_SESSION_CREATION) {
            logger.info("zombie session checker started");
            Timer t = new Timer("zombie session checker", true);
            t.schedule(new TimerTask() {
                public void run() {
                    synchronized(knownSessions) {
                        Iterator<SessionStackTrace> iterator = knownSessions.iterator();
                        while (iterator.hasNext()) {
                            SessionStackTrace item = iterator.next();
                            if ( ! item.session.isOpen() ) {
                                iterator.remove();
                            } else if(ClockUtil.now().subtract(MAX_SESSION_LIFE).after(item.createTime)) {
                                TimeInterval aliveTime = (TimeInterval)ClockUtil.now().subtract(item.createTime).convertTo(UnitImpl.SECOND);
                                logger.warn("Session still open after "+aliveTime+" seconds. create time="+item.createTime);
                                for (int i = 0; i < item.stackTrace.length; i++) {
                                    logger.warn(item.stackTrace[i].toString());
                                }
                            }
                        }
                    }
                }
            }, DEBUG_SESSION_CREATION_SECONDS*1000, DEBUG_SESSION_CREATION_SECONDS*1000);
        }
    }
}


class SessionStackTrace {
    
    public SessionStackTrace(Session session, 
                             StackTraceElement[] stackTrace) {
        this.session = session;
        this.stackTrace = stackTrace;
        this.createTime = ClockUtil.now();
    }
    Session session;
    StackTraceElement[] stackTrace;
    MicroSecondDate createTime;
}
