package edu.sc.seis.fissuresUtil.hibernate;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;
    private static final Configuration configuration;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            configuration = new Configuration().configure("edu/sc/seis/fissuresUtil/hibernate/hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
    
}