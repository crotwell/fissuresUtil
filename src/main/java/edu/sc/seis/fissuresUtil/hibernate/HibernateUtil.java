package edu.sc.seis.fissuresUtil.hibernate;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.ConnectionCreator;

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

    public static void setUpFromConnMgr(Properties props, URL ehcacheConfig) {
    	setUpEHCache(ehcacheConfig);
        setUpFromConnMgr(props);
    }
    
    static void setUpEHCache(URL ehcacheConfig) {
        if (ehcacheConfig == null) {throw new IllegalArgumentException("ehcacheConfig cannot be null");}
        // configure EhCache
        try {
            CacheManager singletonManager = CacheManager.create(ehcacheConfig.openStream());
        } catch(IOException e) {
            throw new RuntimeException("Trouble finding EhCache config from "+ehcacheConfig.toString(), e);
        }
    }
    
    public static void setUpFromConnMgr(Properties props) {
        String dialect;
        if(ConnMgr.getDB_TYPE().equals(ConnMgr.HSQL)) {
            logger.info("using hsql dialect");
            dialect = org.hibernate.dialect.HSQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.POSTGRES)) {
            logger.info("using postgres dialect");
            dialect = org.hibernate.dialect.PostgreSQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.MYSQL)) {
            logger.info("using mysql dialect");
            dialect = org.hibernate.dialect.MySQLDialect.class.getName();
        } else if(ConnMgr.getDB_TYPE().equals(ConnMgr.ORACLE)) {
            logger.info("using oracle dialect");
            dialect = org.hibernate.dialect.Oracle10gDialect.class.getName();
        } else {
            throw new RuntimeException("Unknown database type: '"+ConnMgr.getDB_TYPE()+"'");
        }
        setUp(dialect, ConnMgr.getDriver(), ConnMgr.getURL(), ConnMgr.getUser(), ConnMgr.getPass(), props);
        getConfiguration().addProperties(ConnMgr.getDBProps());
    }
    
    public static void setUpFromConnectionCreator(ConnectionCreator c, URL ehcacheConfig) {
    	setUpEHCache(ehcacheConfig);
        String dialect;
        if(c.getType().equals(ConnectionCreator.HSQL)) {
            logger.info("using hsql dialect");
            dialect = org.hibernate.dialect.HSQLDialect.class.getName();
        } else if(c.getType().equals(ConnectionCreator.POSTGRES)) {
            logger.info("using postgres dialect");
            dialect = org.hibernate.dialect.PostgreSQLDialect.class.getName();
        } else {
            throw new RuntimeException("Unknown database type: '"+c.getType()+"'");
        }
        setUp(dialect, c.getDriverClass().getName(), c.getUrl(), c.getUsername(), c.getPassword(), c.getExtraProps());
    }
    
    public static void setUp(String dialect, String driverClass, String dbURL, String username, String password, Properties props) {
        synchronized(HibernateUtil.class) {
            getConfiguration().setProperty("hibernate.dialect", dialect);
            getConfiguration().setProperty("hibernate.connection.driver_class",
                                           driverClass)
                    .setProperty("hibernate.connection.url", dbURL)
                    .setProperty("hibernate.connection.username",
                                 username)
                    .setProperty("hibernate.connection.password",
                                 password)
                    .addProperties(props);
        }
    }
    
    public static final URL DEFAULT_EHCACHE_CONFIG = HibernateUtil.class.getClassLoader().getResource("edu/sc/seis/fissuresUtil/hibernate/ehcache.xml");

}