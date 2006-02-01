package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;
import junit.framework.TestCase;

public class MagnitudeExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new MagnitudeExtractor().extract(null));
    }

    public void testMagnitude() {
        Magnitude m = MockMagnitude.createMagnitude();
        assertEquals(m, new MagnitudeExtractor().extract(m));
    }

    public void testEvent() {
        EventAccessOperations ev = MockEventAccessOperations.createEvent();
        assertEquals(EventUtil.extractOrigin(ev).magnitudes[0],
                     new MagnitudeExtractor().extract(ev));
    }
}
