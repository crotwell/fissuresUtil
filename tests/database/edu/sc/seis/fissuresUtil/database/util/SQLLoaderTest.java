package edu.sc.seis.fissuresUtil.database.util;

import junit.framework.TestCase;
import org.apache.velocity.VelocityContext;

/**
 * @author groves Created on Nov 23, 2004
 */
public class SQLLoaderTest extends TestCase {

    public void testLoading() throws Exception {
        SQLLoader loader = new SQLLoader("edu/sc/seis/fissuresUtil/database/util/testSQL.vm");
        assertEquals("CREATE TABLE table ( field int, oField int)",
                     loader.get("table.create"));
    }
    
    public void testSuppliedContext() throws Exception{
        VelocityContext ctx = new VelocityContext();
        ctx.put("tablename", "suppliedTablename");
        SQLLoader loader = new SQLLoader("edu/sc/seis/fissuresUtil/database/util/testSQL.vm", ctx);
        assertEquals("CREATE TABLE suppliedTablename ( field int, oField int)",
                     loader.get("suppliedTablename.create"));
    }
}