package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCProblemComponentTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblemComponent problemComponent = new JDBCProblemComponent();
        String comp = "Z";
        int dbid = problemComponent.put(comp);
        assertEquals(problemComponent.put(comp), dbid);
        assertEquals(problemComponent.getName(dbid), comp);
    }
}
