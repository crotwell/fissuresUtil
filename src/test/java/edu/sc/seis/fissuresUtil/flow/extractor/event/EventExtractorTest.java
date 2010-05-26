package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class EventExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new EventExtractor().extract(null));
    }

    public void testEvent() {
        EventAccessOperations ev = MockEventAccessOperations.createEvent();
        assertEquals(ev, new EventExtractor().extract(ev));
    }

    public void testNonsense() {
        assertNull(new EventExtractor().extract(MockChannel.createChannel()));
    }
}
