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

public class JDBCQuantityTest extends TestCase{
    public void testDoublePut() throws SQLException, NotFound{
        JDBCQuantity table = new JDBCQuantity();
        QuantityImpl twelveCubicMeters = new QuantityImpl(12, UnitImpl.CUBIC_METER);
        int id = table.put(twelveCubicMeters);
        int id2 = table.put(twelveCubicMeters);
        assertEquals(id, id2);
        assertEquals(twelveCubicMeters, table.get(id));
    }
}

