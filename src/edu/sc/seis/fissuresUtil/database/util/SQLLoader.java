package edu.sc.seis.fissuresUtil.database.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import edu.sc.seis.fissuresUtil.database.WrappedSQLException;

/**
 * @author groves Created on Nov 23, 2004
 */
public class SQLLoader {

    public SQLLoader(String template) throws SQLException {
        this(template, new VelocityContext());
    }

    public SQLLoader(String template, Context ctx) throws SQLException {
        try {
            this.context = ctx;
            StringWriter velWriter = new StringWriter();
            getEngine().mergeTemplate(template, ctx, velWriter);
            String velOutput = velWriter.toString();
            InputStream sqlInput = new ByteArrayInputStream(velOutput.getBytes());
            sqlProps.load(sqlInput);
        } catch(IOException e) {
            throw new WrappedSQLException("unable to load sql properties from "
                    + template, e);
        } catch(Exception e) {
            throw new WrappedSQLException("unable to merge template from "
                    + template, e);
        }
    }

    private synchronized static VelocityEngine getEngine() throws Exception {
        if(ve == null) {
            ve = new VelocityEngine();
            Properties props = new Properties();ClassLoader cl = SQLLoader.class.getClassLoader();
            props.load(cl.getResourceAsStream(propsLoc));
            setupVelocityLogger(props, logger);
            ve.init(props);
        }
        return ve;
    }

    public static void setupVelocityLogger(Properties velocityProps,  Logger velocityLogger) {
        velocityProps.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                          "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        velocityProps.setProperty("runtime.log.logsystem.log4j.category",
                          logger.getName());
        velocityLogger.setLevel(Level.WARN);
    }

    public boolean has(String propName) {
        return sqlProps.getProperty(propName) != null;
    }

    public String get(String propName) {
        return sqlProps.getProperty(propName);
    }

    public String[] getNamesForPrefix(String prefix) {
        ArrayList list = new ArrayList();
        Iterator it = sqlProps.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            if(key.startsWith(prefix)) {
                list.add(key);
            }
        }
        return (String[])list.toArray(new String[0]);
    }

    private Properties sqlProps = new Properties();

    private static final String propsLoc = "edu/sc/seis/fissuresUtil/database/util/SQLLoader.prop";

    private static final Logger logger = Logger.getLogger(SQLLoader.class);

    private Context context;

    private static VelocityEngine ve;

    public Context getContext() {
        return context;
    }
}