package edu.sc.seis.fissuresUtil.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class ConnMgr {

    /**
     * Use this method to add loacations for sql property files to be stored
     * When setDB is loaded, if there is a default.props file at that location,
     * it will be added to the existing properties, and if one of the <TYPE OF
     * DB>.props exists, it will also be loaded.
     */
    public static void addPropsLocation(String loc) {
        synchronized(propLocs) {
            if(!propLocs.contains(loc))
                propLocs.add(loc);
            if(props != null) {
                try {
                    load(loc, props);
                } catch(IOException e) {
                    throw new RuntimeException("Bad props location " + loc, e);
                }
            }
        }
    }

    /**
     * Sets the ConnMgr to use the default db, which as of now is an in-memory
     * HSQLDb
     */
    public static void setDB() throws IOException {
        setDB(DB_NAME);
    }

    /**
     * Sets the DB to be used based on the default values for the name. Names
     * fissuresUtil knows are ConnMgr.MCKOI, ConnMgr.HSQL, and ConnMgr.POSTGRES
     * 
     * @throws IOException
     *             if some of the props don't load
     */
    public static void setDB(String dbName) throws IOException {
        DB_NAME = dbName;
        Properties props = new Properties();
        synchronized(propLocs) {
            Iterator it = propLocs.iterator();
            while(it.hasNext())
                load((String)it.next(), props);
        }
        setDB(props);
    }

    private static void load(String loc, Properties existing)
            throws IOException {
        ClassLoader cl = ConnMgr.class.getClassLoader();
        load(cl, loc + DEFAULT_PROPS, existing);
        if(DB_NAME == HSQL)
            load(cl, loc + HSQL_PROPS, existing);
        else if(DB_NAME == MCKOI)
            load(cl, loc + MCKOI_PROPS, existing);
        else if(DB_NAME == POSTGRES)
            load(cl, loc + POSTGRES_PROPS, existing);
    }

    private static void load(ClassLoader cl, String loc, Properties existing)
            throws IOException {
        InputStream in = cl.getResourceAsStream(loc);
        if(in != null)
            existing.load(in);
    }

    public static void setDB(Properties newprops) {
        props = newprops;
    }

    public static boolean hasSQL(String key) {
        return getProps().containsKey(key);
    }

    public static String getSQL(String key) {
        String SQL = getProps().getProperty(key);
        if(SQL == null) {
            throw new IllegalArgumentException("No such sql entry "
                    + key
                    + " Make sure the properties files are in the jars and are being loaded");
        }
        return SQL;
    }

    private static String getDriver() {
        return getProps().getProperty("driver");
    }

    public static void setURL(String url) {
        ConnMgr.url = url;
    }

    public static void setURL(String url,
                              String databaseUser,
                              String databasePassword) {
        setURL(url);
        if(databaseUser != null) {
            getProps().setProperty("user", databaseUser);
        }
        if(databasePassword != null) {
            getProps().setProperty("password", databasePassword);
        }
    }

    public static String getURL() {
        if(url == null) {
            url = getProps().getProperty("URL");
        }
        return url;
    }

    private static String getPass() {
        return getProps().getProperty("password");
    }

    private static String getUser() {
        return getProps().getProperty("user");
    }

    private static Properties getProps() {
        synchronized(ConnMgr.class) {
            if(props == null) {
                try {
                    setDB();
                } catch(IOException e) {}
            }
        }
        return props;
    }

    public static Connection createConnection() throws SQLException {
        try {
            String driver = getDriver();
            if(firstTime) {
                logger.debug("Using " + driver + " on " + getURL());
                lastDriverForConnection = driver;
                firstTime = false;
            }
            if(!driver.equals(lastDriverForConnection)) {
                logger.warn("Previous connections were created with "
                        + lastDriverForConnection + " but now " + driver
                        + " is being used");
                lastDriverForConnection = driver;
            }
            Class.forName(getDriver()).newInstance();
        } catch(Exception e) {
            SQLException ee = new SQLException("Unable to instantiate driver");
            ee.initCause(e);
            throw ee;
        }
        Connection conn = DriverManager.getConnection(getURL(), getUser(), getPass());
        if (firstConnection && getURL().startsWith("jdbc:hsql")) {
            Statement stmt = conn.createStatement();
            stmt.execute("SET PROPERTY \"hsqldb.default_table_type\" 'CACHED'");
            stmt.execute("CHECKPOINT");
            firstConnection = false;
        }
        return conn;
    }
    
    private static boolean firstConnection = true;

    private static String lastDriverForConnection;

    private static boolean firstTime = true;

    private static Connection createPSQLConn() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql:anhingatest",
                                           "anhingatest",
                                           "");
    }

    public static void installDbProperties(Properties sysProperties,
                                           Properties dbProperties) {
        if(dbProperties.containsKey(DB_SERVER_PORT)) {
            if(dbProperties.containsKey(DBURL_KEY)) {
                logger.error("-hsql properties and SOD properties are both specifying the db connection.  Using -hsql properties");
            }
            // Use hsqldb properties specified in
            // http://hsqldb.sourceforge.net/doc/guide/ch04.html
            String url = "jdbc:hsqldb:hsql://localhost";
            if(dbProperties.containsKey(DB_SERVER_PORT)) {
                url += ":" + dbProperties.getProperty(DB_SERVER_PORT);
            }
            url += "/";
            if(dbProperties.containsKey("server.dbname.0")) {
                url += dbProperties.getProperty("server.dbname.0");
            }
            logger.debug("Setting db url to " + url);
            setURL(url);
        } else if(sysProperties.containsKey(DBURL_KEY)) {
            logger.debug("Setting db url to "
                    + sysProperties.getProperty(DBURL_KEY));
            setURL(sysProperties.getProperty(DBURL_KEY));
        } else {
            logger.debug("using default url of " + getURL());
        }
    }

    public static Properties readDbProperties(String[] args) {
        Properties dbProperties = new Properties();
        boolean loadedFromArg = false;
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-hsql")) {
                System.out.println("Loading db props");
                try {
                    Initializer.loadProps(new FileInputStream(args[i + 1]),
                                          dbProperties);
                } catch(FileNotFoundException e) {
                    logger.error("Unable to find file " + args[i + 1]
                            + " specified by -hsql");
                }
                loadedFromArg = true;
            }
        }
        if(!loadedFromArg) {
            try {
                logger.debug("No -hsql argument found, trying to load from server.properties in current working directory");
                Initializer.loadProps(new FileInputStream("server.properties"),
                                      dbProperties);
                logger.debug("loaded props from server.properties in working directory");
            } catch(FileNotFoundException e) {
                logger.debug("Didn't find default server.properties file");
            }
        }
        return dbProperties;
    }

    public static void installDbProperties(Properties sysProperties,
                                           String[] args) {
        installDbProperties(sysProperties, readDbProperties(args));
    }

    private static final String DEFAULT_LOC = "edu/sc/seis/fissuresUtil/database/props/";

    public static final String DEFAULT = "default";

    private static String DEFAULT_PROPS = "default.props";

    public static final String HSQL = "HSQL";

    private static String HSQL_PROPS = "HSQL.props";

    public static final String MCKOI = "MCKOI";

    private static String MCKOI_PROPS = "MCKOI.props";

    public static final String POSTGRES = "POSTGRES";

    public static final String POSTGRES_PROPS = "Postgres.props";

    public static final String DB_SERVER_PORT = "server.port";

    public static final String DBURL_KEY = "fissuresUtil.database.url";

    private static String DB_NAME = HSQL;

    private static Properties props;

    private static List propLocs = new ArrayList();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConnMgr.class);

    private static String url;
    static {
        propLocs.add(DEFAULT_LOC);
    }
}// ConnMgr
