package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;
import junit.framework.TestCase;

public class JDBCProblemTypeTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblemType problemComponent = new JDBCProblemType();
        String type = "flatline";
        int dbid = problemComponent.put(type);
        assertEquals(problemComponent.put(type), dbid);
        assertEquals(problemComponent.getName(dbid), type);
    }
}
