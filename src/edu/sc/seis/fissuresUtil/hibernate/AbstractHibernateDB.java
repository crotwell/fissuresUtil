package edu.sc.seis.fissuresUtil.hibernate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.UnitImpl;

public abstract class AbstractHibernateDB {

    public AbstractHibernateDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public AbstractHibernateDB(SessionFactory factory) {
        this.factory = factory;
        logger.debug("init "+factory.toString());
    }

    private void loadUnits(Session s) {
        Query q = s.createQuery("From edu.iris.Fissures.model.UnitImpl");
        List result = q.list();
        unitCache.addAll(result);
    }

    public void deploySchema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }
    
    protected Session createSession() {
        Session cacheSession = factory.openSession();
        cacheSession.beginTransaction();
        logger.debug("TRANSACTION Begin: "+this+" on "+cacheSession);
        loadUnits(cacheSession);
        return cacheSession;
    }

    public synchronized Session getSession() {
        Session s = (Session)sessionTL.get();
        if (s == null) {
            s = createSession();
            sessionTL.set(s);
        }
        return s;
    }
    
    public void flush() {
        getSession().flush();
    }
    
    public synchronized  void commit() {
        Session s = (Session)sessionTL.get();
        if (s == null) {throw new RuntimeException("Can not commit before session creation");}
        logger.debug("TRANSACTION Commit: "+this+" on "+s);
        s.getTransaction().commit();
        s.close();
        sessionTL.set(null);
    }
    
    public synchronized  void rollback() {
        Session s = (Session)sessionTL.get();
        if (s == null) {throw new RuntimeException("Can not rollback before session creation");}
        logger.debug("TRANSACTION Rollback: "+this+" on "+s);
        s.getTransaction().rollback();
        s.close();
        sessionTL.set(null);
    }

    public void internUnit(Location loc) {
        loc.depth.the_units = intern((UnitImpl)loc.depth.the_units);
        loc.elevation.the_units = intern((UnitImpl)loc.elevation.the_units);
    }
    
    protected UnitImpl intern(UnitImpl unit) {
        Iterator it = unitCache.iterator();
        while(it.hasNext()) {
            UnitImpl internUnit = (UnitImpl)it.next();
            if (unit.equals(internUnit)) {
                return internUnit;
            }
        }
        Session session = getSession();
        Integer dbid = (Integer)session.save(unit);
        unitCache.add(unit);
        return unit;
    }
    
    protected HashSet unitCache = new HashSet();
    
    SessionFactory factory;

    private ThreadLocal sessionTL = new ThreadLocal() {
        protected synchronized Object initialValue() { 
            logger.debug("new networkDB");
            return createSession();
        }
    };
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AbstractHibernateDB.class);
}
