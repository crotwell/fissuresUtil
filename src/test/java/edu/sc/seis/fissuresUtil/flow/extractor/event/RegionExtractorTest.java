package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class RegionExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new RegionExtractor().extract(null));
    }

    public void testEventAttr() {
        EventAttr attr = MockEventAccessOperations.createEvent()
                .get_attributes();
        assertEquals(attr.region, new RegionExtractor().extract(attr));
    }

    public void testRegion() {
        FlinnEngdahlRegion reg = MockEventAccessOperations.createEvent()
                .get_attributes().region;
        assertEquals(reg, new RegionExtractor().extract(reg));
    }

    public void testNonsense() {
        assertNull(new RegionExtractor().extract(MockChannel.createChannel()));
    }
}
