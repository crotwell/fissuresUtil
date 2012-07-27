package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class RegionNameExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new RegionNameExtractor().extract(null));
    }

    public void testString() {
        assertEquals(name, new RegionNameExtractor().extract(name));
    }

    public void testRegion() {
        assertEquals(name, new RegionNameExtractor().extract(reg));
    }

    public void testNonsense() {
        assertNull(new RegionNameExtractor().extract(MockChannel.createChannel()));
    }

    private FlinnEngdahlRegion reg = MockEventAccessOperations.createEvent()
            .get_attributes().region;

    private String name = ParseRegions.getInstance().getRegionName(reg);
}
