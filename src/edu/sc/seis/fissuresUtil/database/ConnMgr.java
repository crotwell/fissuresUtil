package edu.sc.seis.fissuresUtil.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ConnMgr {

    /**
     * Use this method to add loacations for sql property files to be stored
     * When setDB is loaded, if there is a default.props file at that location,
     * it will be added to the existing properties, and if one of the <TYPE OF
     * DB>.props exists, it will also be loaded.
     */
    public static void addPropsLocation(String loc) {
        synchronized(propLocs) {
            if(!propLocs.contains(loc)) propLocs.add(loc);
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
        if(DB_NAME == HSQL) load(cl, loc + HSQL_PROPS, existing);
        else if(DB_NAME == MCKOI) load(cl, loc + MCKOI_PROPS, existing);
        else if(DB_NAME == POSTGRES) load(cl, loc + POSTGRES_PROPS, existing);
    }

    private static void load(ClassLoader cl, String loc, Properties existing)
            throws IOException {
        InputStream in = cl.getResourceAsStream(loc);
        if(in != null) existing.load(in);
    }

    public static void setDB(Properties newprops) {
        props = newprops;
    }

    public static boolean hasSQL(String key) {
        return getProps().containsKey(key);
    }

    public static String getSQL(String key) {
        String SQL = getProps().getProperty(key);
        if(SQL == null) { throw new IllegalArgumentException("No such sql entry "
                + key
                + " Make sure the properties files are in the jars and are being loaded"); }
        return SQL;
    }

    private static String getDriver() {
        return getProps().getProperty("driver");
    }

    public static void setURL(String url) {
        ConnMgr.url = url;
    }

    public static void setURL(String url, String databaseUser, String databasePassword) {
        setURL(url);
        if (databaseUser != null) {
            getProps().setProperty("user", databaseUser);
        }
        if (databasePassword != null) {
            getProps().setProperty("password", databasePassword);
        }
    }
    
    public static String getURL() {
        if(url == null) { url = getProps().getProperty("URL"); }
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
            Class.forName(getDriver()).newInstance();
        } catch(Exception e) {
            e.printStackTrace();
            throw new SQLException("Unable to instantiate DBDriver");
        }
        return DriverManager.getConnection(getURL(), getUser(), getPass());
    }

    private static Connection createPSQLConn() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql:anhingatest",
                                           "anhingatest",
                                           "");
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

    private static String DB_NAME = HSQL;

    private static Properties props;

    private static List propLocs = new ArrayList();

    private static String url;
    static {
        propLocs.add(DEFAULT_LOC);
    }
}// ConnMgr
