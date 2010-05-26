package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;

public class OriginExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new OriginExtractor().extract(null));
    }

    public void testOrigin() {
        Origin o = MockOrigin.createOrigin();
        assertEquals(o, new OriginExtractor().extract(o));
    }

    public void testEvent() {
        EventAccessOperations ev = MockEventAccessOperations.createEvent();
        assertEquals(EventUtil.extractOrigin(ev),
                     new OriginExtractor().extract(ev));
    }
}
