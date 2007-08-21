/**
 * JDBCQuantityTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import junit.framework.TestCase;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class JDBCQuantityTest extends JDBCTearDown {
    public void testDoublePut() throws SQLException, NotFound{
        JDBCQuantity table = new JDBCQuantity();
        QuantityImpl twelveCubicMeters = new QuantityImpl(12, UnitImpl.CUBIC_METER);
        int id = table.put(twelveCubicMeters);
        int id2 = table.put(twelveCubicMeters);
        assertEquals(id, id2);
        assertEquals(twelveCubicMeters, table.get(id));
    }

    public void testCrazyDoublePut() throws SQLException, NotFound{
        JDBCQuantity table = new JDBCQuantity();
        // this value caused problems in postgres, the value
        // extracted from the database was slightly different
        // from the value put in
        long l = Long.parseLong("4619454727891976192");
        double d = Double.longBitsToDouble(l);
        QuantityImpl crazyMeters = new QuantityImpl(d, UnitImpl.METER);
        int id = table.put(crazyMeters);
        int id2 = table.put(crazyMeters);
        assertEquals(id, id2);
        QuantityImpl dbCrazyMeters = table.get(id);
        assertEquals(crazyMeters, dbCrazyMeters);
        int id3 = table.put(dbCrazyMeters);
        assertEquals("put value extracted from db", id, id3);
    }
}

