package edu.sc.seis.fissuresUtil.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.sc.seis.fissuresUtil.simple.Initializer;

/**
 * @author oliverpa
 * 
 * Created on Jun 27, 2005
 */
public class ConnectionCreator {

    public ConnectionCreator(String[] args) {
        this(loadDbProperties(args));
    }

    public ConnectionCreator(Properties props) {
        this(props.getProperty(DB_URL_KEY, "jdbc:hsqldb:."),
             props.getProperty(DB_USER_KEY, "SA"),
             props.getProperty(DB_PASS_KEY, ""),
             props);
    }

    public ConnectionCreator(String url,
                             String username,
                             String password,
                             Properties extraProps) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.extraProps = extraProps;
        if (url.startsWith("jdbc:hsql")) {
            type = HSQL;
        } else if (url.startsWith("jdbc:postgresql")) {
            type = POSTGRES;
        }
    }

    public Connection createConnection() throws SQLException {
            getDriverClass();
        return DriverManager.getConnection(url, username, password);
    }
    
    public Class getDriverClass() {
        try {
            return Class.forName(jdbcDrivers.getProperty(JDBC_DRIVER_PREFIX + type));
        } catch(Throwable e) {
            RuntimeException ee = new RuntimeException("Unable to instantiate DBDriver. type="
                    + type);
            ee.initCause(e);
            throw ee;
        }
    }

    public String getPassword() {
        return password;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
    
    public Properties getExtraProps() {
        return extraProps;
    }

    public static Properties loadDbProperties(String[] args) {
        return loadDbProperties(loadDbPropsFromArgProps(args));
    }

    public static Properties loadDbProperties(Properties propsFromArgs) {
        Properties dbProps = loadDbPropsFromHSQLProps();
        dbProps.putAll(propsFromArgs);
        dbProps.putAll(loadDbPropsFromSystemProps());
        return dbProps;
    }

    public static Properties loadDbPropsFromHSQLProps() {
        Properties dbProperties = new Properties();
        try {
            dbProperties.load(new FileInputStream("server.properties"));
            if(dbProperties.containsKey(DB_SERVER_PORT)) {
                if(dbProperties.containsKey(DB_URL_KEY)) {
                    logger.error("hsql configuration mismatch.  using local hsql config.");
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
                dbProperties.setProperty(DB_URL_KEY, url);
            }
            dbProperties.setProperty(DB_TYPE_KEY, HSQL);
        } catch(FileNotFoundException e) {
            logger.debug("didn't find default server.properties file");
        } catch(IOException e) {
            logger.debug("had trouble reading server.properties file");
        }
        return dbProperties;
    }

    public static Properties loadDbPropsFromArgProps(String[] args) {
        Properties dbProperties = new Properties();
        System.out.println("Loading db props");
        try {
            Initializer.loadProperties(args, dbProperties);
        } catch(FileNotFoundException e) {
            logger.error("Unable to find specified props file: "
                    + e.getMessage());
        } catch(IOException e) {
            logger.error("Trouble reading from props file: " + e.getMessage());
        }
        return dbProperties;
    }

    public static Properties loadDbPropsFromOtherProps(Properties props) {
        Properties dbProperties = new Properties();
        for(int i = 0; i < DB_PROP_KEYS.length; i++) {
            String curKey = DB_PROP_KEYS[i];
            if(props.containsKey(curKey)) {
                dbProperties.put(curKey, props.get(curKey));
            }
        }
        return dbProperties;
    }

    public static Properties loadDbPropsFromSystemProps() {
        return loadDbPropsFromOtherProps(System.getProperties());
    }

    private String url, type, username, password;
    
    private Properties extraProps;
    
    private static Logger logger = LoggerFactory.getLogger(ConnectionCreator.class);

    public static final String DB_PROP_PREFIX = "fissuresUtil.database.";

    public static final String DB_URL_KEY = DB_PROP_PREFIX + "url";

    public static final String DB_TYPE_KEY = DB_PROP_PREFIX + "type";

    public static final String DB_USER_KEY = DB_PROP_PREFIX + "username";

    public static final String DB_PASS_KEY = DB_PROP_PREFIX + "password";

    public static final String[] DB_PROP_KEYS = {DB_URL_KEY,
                                                 DB_TYPE_KEY,
                                                 DB_USER_KEY,
                                                 DB_PASS_KEY};

    public static final String DB_SERVER_PORT = "server.port";

    public static final String HSQL = "HSQL";

    public static final String MCKOI = "MCKOI";

    public static final String POSTGRES = "POSTGRES";

    public static final String JDBC_DRIVER_PREFIX = DB_PROP_PREFIX + "driver.";

    private static Properties jdbcDrivers = new Properties();
    
    static {
        jdbcDrivers.put("fissuresUtil.database.driver.HSQL", "org.hsqldb.jdbcDriver");
        jdbcDrivers.put("fissuresUtil.database.driver.MCKOI", "com.mckoi.JDBCDriver");
        jdbcDrivers.put("fissuresUtil.database.driver.POSTGRES", "org.postgresql.Driver");
    }
}
