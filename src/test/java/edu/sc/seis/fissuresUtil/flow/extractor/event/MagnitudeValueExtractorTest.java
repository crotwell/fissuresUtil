package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;

public class MagnitudeValueExtractorTest extends TestCase {

    public void testNull() {
        assertEquals(-1, new MagnitudeValueExtractor().extract(null), 0);
    }

    public void testMagnitude() {
        Magnitude m = MockMagnitude.createMagnitude();
        assertEquals(m.value, new MagnitudeValueExtractor().extract(m), 0);
    }
}
