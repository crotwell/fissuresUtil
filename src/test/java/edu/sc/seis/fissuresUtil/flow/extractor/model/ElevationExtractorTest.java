package edu.sc.seis.fissuresUtil.flow.extractor.model;

import junit.framework.TestCase;
import edu.iris.Fissures.Quantity;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class ElevationExtractorTest extends TestCase {

    public void testOnKnownTypes() {
        QuantityExtractor e = createExtractor();
        Quantity q = e.extract(MockLocation.create());
        assertEquals(q, e.extract(MockEventAccessOperations.createEvent()));
        assertEquals(q, e.extract(MockLocation.create()));
        assertEquals(q, e.extract(MockStation.createStation()));
        assertEquals(q, e.extract(MockChannel.createChannel()));
        assertNull(e.extract(null));
    }

    public void testExtractFromQuantity() {
        assertEquals(MockLocation.create().elevation,
                     new ElevationExtractor().extract(MockLocation.create().elevation));
    }

    public QuantityExtractor createExtractor() {
        return new ElevationExtractor();
    }
}
