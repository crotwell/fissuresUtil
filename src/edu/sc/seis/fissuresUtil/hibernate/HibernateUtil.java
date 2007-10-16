package edu.sc.seis.fissuresUtil.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static String configFile = "edu/sc/seis/fissuresUtil/hibernate/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;

    private static Configuration configuration;

    public static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public static Configuration getConfiguration() {
        if(configuration == null) {
            configuration = new Configuration().configure(configFile);
        }
        return configuration;
    }
    
    public static void setConfigFile(String s) {
        configFile = s;
        sessionFactory = null;
        configuration = null;
    }
}