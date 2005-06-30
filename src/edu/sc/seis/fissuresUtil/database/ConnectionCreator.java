package edu.sc.seis.fissuresUtil.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;
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
        this(props.getProperty(DB_URL_KEY, "localhost:."),
             props.getProperty(DB_TYPE_KEY, "HSQL"),
             props.getProperty(DB_USER_KEY, "SA"),
             props.getProperty(DB_PASS_KEY, ""));
    }

    public ConnectionCreator(String url,
                             String type,
                             String username,
                             String password) {
        this.url = url;
        this.type = type;
        this.username = username;
        this.password = password;
        jdbcDrivers = new Properties();
        try {
            jdbcDrivers.load(getClass().getClassLoader()
                    .getResourceAsStream("edu/sc/seis/fissuresUtil/database/props/drivers.props"));
        } catch(Exception e) {
            throw new RuntimeException("unable to load database driver properties");
        }
    }

    public Connection createConnection() throws SQLException {
        try {
            Class.forName(jdbcDrivers.getProperty(JDBC_DRIVER_PREFIX + type));
        } catch(Exception e) {
            SQLException ee = new SQLException("Unable to instantiate DBDriver. type="
                    + type);
            ee.initCause(e);
            throw ee;
        }
        return DriverManager.getConnection(url, username, password);
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

    public static Properties loadDbProperties(String[] args) {
        Properties dbProps = loadDbPropsFromHSQLProps();
        dbProps.putAll(loadDbPropsFromArgProps(args));
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
        for(int i = 0; i < args.length - 1; i++) {
            if(args[i].equals("-props")) {
                System.out.println("Loading db props");
                try {
                    Initializer.loadProps(new FileInputStream(args[i + 1]),
                                          dbProperties);
                } catch(FileNotFoundException e) {
                    logger.error("Unable to find file " + args[i + 1]
                            + " specified by -props");
                }
            }
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

    private static Logger logger = Logger.getLogger(ConnectionCreator.class);

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

    private Properties jdbcDrivers;
}