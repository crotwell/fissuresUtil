package edu.sc.seis.fissuresUtil.flow.extractor.event;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class EventAttrExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new EventAttrExtractor().extract(null));
    }

    public void testEventAttr() {
        EventAttr ev = MockEventAccessOperations.createEvent().get_attributes();
        assertEquals(ev, new EventAttrExtractor().extract(ev));
    }

    public void testEvent() {
        EventAccessOperations ev = MockEventAccessOperations.createEvent();
        assertEquals(ev.get_attributes(), new EventAttrExtractor().extract(ev));
    }

    public void testNonsense() {
        assertNull(new EventAttrExtractor().extract(MockChannel.createChannel()));
    }
}
