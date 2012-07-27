package edu.sc.seis.fissuresUtil.flow.extractor.model;

import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class DepthExtractorTest extends ElevationExtractorTest {

    public void testExtractFromQuantity() {
        assertEquals(MockLocation.create().depth,
                     new DepthExtractor().extract(MockLocation.create().depth));
    }

    public QuantityExtractor createExtractor() {
        return new DepthExtractor();
    }
}
