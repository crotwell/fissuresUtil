package edu.sc.seis.fissuresUtil.database.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

/**
 * @author groves Created on Nov 23, 2004
 */
public class SQLLoader {

    public SQLLoader(String template) throws Exception{
        this(template, new VelocityContext());
    }
    
    public SQLLoader(String template, Context ctx) throws Exception {
        VelocityEngine ve = new VelocityEngine();
        Properties props = new Properties();
        ClassLoader cl = getClass().getClassLoader();
        props.load(cl.getResourceAsStream(propsLoc));
        ve.init(props);
        StringWriter velWriter = new StringWriter();
        ve.mergeTemplate(template, ctx, velWriter);
        String velOutput = velWriter.toString();
        InputStream sqlInput = new ByteArrayInputStream(velOutput.getBytes());
        sqlProps.load(sqlInput);
    }
    
    public boolean has(String propName){
        return sqlProps.getProperty(propName) != null;
    }
    
    public String get(String propName){
        return sqlProps.getProperty(propName);
    }
    
    private Properties sqlProps = new Properties();

    private static final String propsLoc = "edu/sc/seis/fissuresUtil/database/util/SQLLoader.prop";
}