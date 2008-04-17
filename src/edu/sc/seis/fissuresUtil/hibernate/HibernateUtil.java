package edu.sc.seis.fissuresUtil.hibernate;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.sc.seis.fissuresUtil.database.ConnMgr;

public class HibernateUtil {

    private static String configFile = "edu/sc/seis/fissuresUtil/hibernate/hibernate.cfg.xml";

    private static SessionFactory sessionFactory;

    private static Configuration configuration;

    public synchronized static SessionFactory getSessionFactory() {
        if(sessionFactory == null) {
            logger.debug("Sessionfactory is null, creating...");
            sessionFactory = getConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    public synchronized static Configuration getConfiguration() {
        if(configuration == null) {
            logger.debug("Hibernate configuration is null, loading config from "
                    + configFile);
            configuration = new Configuration().configure(configFile);
        }
        return configuration;
    }

    public synchronized static void setConfigFile(String s) {
        logger.warn("Reseting hibernate configuration: " + s);
        configFile = s;
        sessionFactory = null;
        configuration = null;
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HibernateUtil.class);

    public static void setUpFromConnMgr(Properties props) {
        synchronized(HibernateUtil.class) {
            if(ConnMgr.getDB_TYPE().equals(ConnMgr.HSQL)) {
                logger.info("using hsql dialect");
                getConfiguration().setProperty("hibernate.dialect", org.hibernate.dialect.HSQLDialect.class.getName());
            } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.POSTGRES)) {
                logger.info("using postgres dialect");
                getConfiguration().setProperty("hibernate.dialect", org.hibernate.dialect.PostgreSQLDialect.class.getName());
            }
            getConfiguration().setProperty("hibernate.connection.driver_class",
                                           ConnMgr.getDriver())
                    .setProperty("hibernate.connection.url", ConnMgr.getURL())
                    .setProperty("hibernate.connection.username",
                                 ConnMgr.getUser())
                    .setProperty("hibernate.connection.password",
                                 ConnMgr.getPass())
                    .addProperties(props)
                    .addProperties(ConnMgr.getDBProps());
        }
    }
}