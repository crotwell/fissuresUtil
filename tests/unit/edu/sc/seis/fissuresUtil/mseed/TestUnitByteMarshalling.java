package edu.sc.seis.fissuresUtil.mseed;

import java.io.IOException;
import junit.framework.TestCase;
import edu.iris.Fissures.model.UnitImpl;

public class TestUnitByteMarshalling extends TestCase {

    public void testSimpleUnit() throws Exception {
        assertEquals(UnitImpl.METER,
                     FissuresConvert.fromBytes(FissuresConvert.toBytes(UnitImpl.METER)));
    }

    public void testCompositeUnit() throws IOException, ClassNotFoundException {
        assertEquals(UnitImpl.METER_PER_SECOND_PER_SECOND,
                     FissuresConvert.fromBytes(FissuresConvert.toBytes(UnitImpl.METER_PER_SECOND_PER_SECOND)));
    }
}
