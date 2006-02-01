package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;

public class MagnitudeTypeExtractorTest extends TestCase {

    public void testString() {
        assertEquals("dog", new MagnitudeTypeExtractor().extract("dog"));
    }

    public void testMagnitude() {
        Magnitude m = MockMagnitude.createMagnitude();
        assertEquals(m.type, new MagnitudeTypeExtractor().extract(m));
    }

    public void testEvent() {
        EventAccessOperations ev = MockEventAccessOperations.createEvent();
        assertEquals(EventUtil.extractOrigin(ev).magnitudes[0].type,
                     new MagnitudeTypeExtractor().extract(ev));
    }

    public void testNull() {
        assertNull(new MagnitudeTypeExtractor().extract(null));
    }
}
