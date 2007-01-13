package edu.sc.seis.fissuresUtil.database.problem;

import java.sql.SQLException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class JDBCProblemStationTest extends TestCase {

    public void testPutAndGet() throws SQLException, NotFound {
        BasicConfigurator.configure();
        JDBCProblemStation stationTable = new JDBCProblemStation();
        String station1 = "SNP12";
        int dbid = stationTable.put(station1);
        assertEquals(stationTable.put(station1), dbid);
        assertEquals(stationTable.getName(dbid), station1);
    }
}
