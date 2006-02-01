package edu.sc.seis.fissuresUtil.flow.extractor.model;

import junit.framework.TestCase;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class TimeExtractorTest extends TestCase {

    public void testNull() {
        assertNull(new TimeExtractor().extract(null));
    }

    public void testTime() {
        Time t = new MicroSecondDate().getFissuresTime();
        assertEquals(t, new TimeExtractor().extract(t));
    }

    public void testEvent() {
        EventAccessOperations ea = MockEventAccessOperations.createEvent();
        assertEquals(EventUtil.extractOrigin(ea).origin_time,
                     new TimeExtractor().extract(ea));
    }
}
