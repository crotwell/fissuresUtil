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
     * it will be added to the existing properties, and if one of the
     * <TYPE OF DB>.props exists, it will also be loaded.
     */
    public static void addPropsLocation(String loc) {
        synchronized(propLocs){
            if(!propLocs.contains(loc))propLocs.add(loc);
        }
    }
    
    /**
     * Sets the ConnMgr to use the default db, which as of now is an in-memory HSQLDb
     */
    public static void setDB() throws IOException{ setDB(HSQL); }
    
    /**
     *Sets the DB to be used based on the default values for the name.
     *Names fissuresUtil knows are ConnMgr.MCKOI, ConnMgr.HSQL, and
     * ConnMgr.POSTGRES
     * @throws IOException if some of the props don't load
     */
    public static void setDB(String dbName) throws IOException{
        Properties props = new Properties();
        synchronized(propLocs){
            Iterator it = propLocs.iterator();
            while(it.hasNext())load((String)it.next(), dbName, props);
        }
        setDB(props);
    }
    
    private static void load(String loc, String type, Properties existing) throws IOException{
        ClassLoader cl = ConnMgr.class.getClassLoader();
        load(cl, loc + DEFAULT_PROPS, existing);
        if(type == HSQL)load(cl, loc + HSQL_PROPS, existing);
        else if(type == MCKOI)load(cl, loc + MCKOI_PROPS, existing);
    }
    
    private static void load(ClassLoader cl, String loc, Properties existing) throws IOException{
        InputStream in = cl.getResourceAsStream(loc);
        if(in != null)existing.load(in);
    }
    
    public static void setDB(Properties props){ ConnMgr.props = props; }
    
    public static String getSQL(String key){
        String SQL = props.getProperty(key);
        if(SQL == null){
            throw new IllegalArgumentException("No such sql entry " + key + " Make sure the properties files are in the jars and are being loaded");
        }
        return props.getProperty(key);
    }
    
    private static String getDriver(){ return props.getProperty("driver"); }
    
    private static String getURL(){ return  props.getProperty("URL"); }
    
    private static String getPass() { return props.getProperty("password"); }
    
    private static String getUser() { return props.getProperty("user"); }
    
    public static Connection getConnection() throws SQLException {
        synchronized(ConnMgr.class){
            if(props == null){
                try {
                    setDB();
                } catch (IOException e) {}
            }
        }
        try {
            Class.forName(getDriver()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Unable to instantiate DBDriver");
        }
        return DriverManager.getConnection(getURL(), getUser(), getPass());
    }
    
    private static Connection createPSQLConn() throws SQLException{
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
    
    public static final String POSTGRES = "";//TODO
    
    private static Properties props;
    
    private static List propLocs = new ArrayList();
    
    static{
        propLocs.add(DEFAULT_LOC);
    }
    
}// ConnMgr

