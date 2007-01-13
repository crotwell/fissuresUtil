package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCProblemTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblem problemTable = new JDBCProblem();
        Problem problem = new Problem("SNP12",
                                      "flatline",
                                      "automatically generated");
        int dbid = problemTable.put(problem);
        assertEquals(problemTable.put(problem), dbid);
        Problem problem2 = problemTable.getProblem(dbid);
        System.out.println(problem);
        System.out.println(problem2);
        assertEquals(problemTable.getProblem(dbid), problem);
    }
}
