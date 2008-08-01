package edu.sc.seis.fissuresUtil.hibernate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.UnitImpl;

public abstract class AbstractHibernateDB {

    public AbstractHibernateDB() {
        logger.debug("init "+this);
    }

    private static void loadUnits(Session s) {
        Query q = s.createQuery("From edu.iris.Fissures.model.UnitImpl");
        List result = q.list();
        getUnitCache().addAll(result);
        // check common units to make sure in db
        synchronized(AbstractHibernateDB.class) {
            UnitImpl[] unitsToAdd = new UnitImpl[] {UnitImpl.METER, UnitImpl.KILOMETER, UnitImpl.SECOND};
            for(int i = 0; i < unitsToAdd.length; i++) {
                if ( ! getUnitCache().contains(unitsToAdd[i])) {
                    getUnitCache().add(unitsToAdd[i]);
                    getSession().save(unitsToAdd[i]);
                }
            }
                
        }
    }

    public static void deploySchema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }

    protected static Session createSession() {
        Session cacheSession = HibernateUtil.getSessionFactory().openSession();
        cacheSession.beginTransaction();
        //logger.debug("TRANSACTION Begin: " + this + " on " + cacheSession);
        return cacheSession;
    }

    public static synchronized Session getSession() {
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
    public static synchronized void commit() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            // no session here, nothing to do
            logger.info("Commit session before creation, nothing to do");
            return;
        }
        //logger.debug("TRANSACTION Commit: " + this + " on " + s);
        sessionTL.set(null);
        unitCacheTL.set(null);
        s.getTransaction().commit();
        s.close();
    }

    /** rolls back the current session that is associated with the current thread. */
    public static synchronized void rollback() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            throw new RuntimeException("Can not rollback before session creation");
        }
        //logger.debug("TRANSACTION Rollback: " + this + " on " + s);
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
        HashSet unitCache = getUnitCache();
        if(unitCache.size() == 0) {
            loadUnits(getSession());
        }
        Iterator it = unitCache.iterator();
        while(it.hasNext()) {
            UnitImpl internUnit = (UnitImpl)it.next();
            if(unit.equals(internUnit)) {
                return internUnit;
            }
        }
        for(int i = 0; i < unit.getSubUnits().length; i++) {
            intern(unit.getSubUnit(i));
        }
        Session session = getSession();
        Integer dbid = (Integer)session.save(unit);
        unitCache.add(unit);
        return unit;
    }

    protected static HashSet getUnitCache() {
        HashSet out = (HashSet)unitCacheTL.get();
        if(out == null) {
            out = new HashSet();
            unitCacheTL.set(out);
        }
        return out;
    }

    private static ThreadLocal unitCacheTL = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new HashSet();
        }
    };

    private static ThreadLocal sessionTL = new ThreadLocal() {

        protected synchronized Object initialValue() {
            logger.debug("new hibernate session");
            return createSession();
        }
    };

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractHibernateDB.class);

}
