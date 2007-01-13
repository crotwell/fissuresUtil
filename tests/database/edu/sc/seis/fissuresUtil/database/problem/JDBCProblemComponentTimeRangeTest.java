package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCProblemComponentTimeRangeTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblemComponentTimeRange comptr = new JDBCProblemComponentTimeRange();
        String station = "SNP12";
        String type = "flatline";
        String status = "automatically generated";
        String components = "Z:N";
        String start = "2006:352:00:00:00.000";
        String end = "2006:352:02:00:00.000";
        int[] dbids = comptr.put(station, type, status, components, start, end);
        assertEquals(comptr.put(station, type, status, components, start, end)[0],
                     dbids[0]);
        assertEquals(comptr.getProblemComponentTimeRange(dbids[0]),
                     new ProblemComponentTimeRange(station,
                                                   type,
                                                   status,
                                                   "Z",
                                                   start,
                                                   end));
    }
}
