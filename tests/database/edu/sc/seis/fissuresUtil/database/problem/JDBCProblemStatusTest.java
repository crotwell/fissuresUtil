package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;
import junit.framework.TestCase;

public class JDBCProblemStatusTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblemStatus problemComponent = new JDBCProblemStatus();
        String status = "automatically generated";
        int dbid = problemComponent.put(status);
        assertEquals(problemComponent.put(status), dbid);
        assertEquals(problemComponent.getName(dbid), status);
    }
}
