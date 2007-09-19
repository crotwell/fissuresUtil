package edu.sc.seis.fissuresUtil.hibernate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.UnitImpl;

public abstract class AbstractHibernateDB {

    public AbstractHibernateDB() {
        this(HibernateUtil.getSessionFactory());
    }

    public AbstractHibernateDB(SessionFactory factory) {
        this.factory = factory;
        createSession();
        loadUnits();
    }

    private void loadUnits() {
        Query q = getSession().createQuery("From edu.iris.Fissures.model.UnitImpl");
        List result = q.list();
        unitCache.addAll(result);
    }

    public void deploySchema() {
        SchemaUpdate update = new SchemaUpdate(HibernateUtil.getConfiguration());
        update.execute(false, true);
    }
    
    protected Session createSession() {
        session = factory.getCurrentSession();
        Transaction tx = session.beginTransaction();
        return session;
    }

    protected Session getSession() {
        if (session == null) {
            createSession();
        }
        return session;
    }
    
    public void commit() {
        getSession().getTransaction().commit();
        session = null;
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
    Session session;
}
