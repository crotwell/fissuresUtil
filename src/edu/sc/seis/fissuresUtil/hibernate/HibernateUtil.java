package edu.sc.seis.fissuresUtil.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static String configFile = "edu/sc/seis/fissuresUtil/hibernate/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;

    private static Configuration configuration;

    public synchronized static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public synchronized static Configuration getConfiguration() {
        if(configuration == null) {
            configuration = new Configuration().configure(configFile);
        }
        return configuration;
    }
    
    public synchronized static void setConfigFile(String s) {
        System.out.println("WARNING: Reseting hibernate configuration: "+s);
        configFile = s;
        sessionFactory = null;
        configuration = null;
    }
}