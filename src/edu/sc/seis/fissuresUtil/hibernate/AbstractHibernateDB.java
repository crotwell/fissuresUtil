package edu.sc.seis.fissuresUtil.hibernate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.UnitImpl;

public abstract class AbstractHibernateDB {

    public AbstractHibernateDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public AbstractHibernateDB(SessionFactory factory) {
        this.factory = factory;
        logger.debug("init " + factory.toString());
    }

    private void loadUnits(Session s) {
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

    public void deploySchema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }

    protected Session createSession() {
        Session cacheSession = factory.openSession();
        cacheSession.beginTransaction();
        //logger.debug("TRANSACTION Begin: " + this + " on " + cacheSession);
        return cacheSession;
    }

    public synchronized Session getSession() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            s = createSession();
            sessionTL.set(s);
        }
        return s;
    }

    public void flush() {
        getSession().flush();
    }

    public synchronized void commit() {
        Session s = (Session)sessionTL.get();
        if(s == null) {
            throw new RuntimeException("Can not commit before session creation");
        }
        //logger.debug("TRANSACTION Commit: " + this + " on " + s);
        sessionTL.set(null);
        unitCacheTL.set(null);
        s.getTransaction().commit();
        s.close();
    }

    public synchronized void rollback() {
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

    public void internUnit(Location loc) {
        internUnit(loc.depth);
        internUnit(loc.elevation);
    }
    public void internUnit(Quantity q) {
        q.the_units = intern((UnitImpl)q.the_units);
    }

    protected UnitImpl intern(UnitImpl unit) {
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

    protected HashSet getUnitCache() {
        HashSet out = (HashSet)unitCacheTL.get();
        if(out == null) {
            out = new HashSet();
            unitCacheTL.set(out);
        }
        return out;
    }

    private ThreadLocal unitCacheTL = new ThreadLocal() {

        protected synchronized Object initialValue() {
            return new HashSet();
        }
    };

    SessionFactory factory;

    private ThreadLocal sessionTL = new ThreadLocal() {

        protected synchronized Object initialValue() {
            logger.debug("new hibernate session");
            return createSession();
        }
    };

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractHibernateDB.class);
}
