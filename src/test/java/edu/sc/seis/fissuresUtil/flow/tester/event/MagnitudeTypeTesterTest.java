package edu.sc.seis.fissuresUtil.flow.tester.event;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.flow.extractor.event.MagnitudeExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.TesterTesterTesterTest;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class MagnitudeTypeTesterTest extends TesterTesterTesterTest {

    private static final CacheEvent EV = MockEventAccessOperations.createEvent();

    public Tester getTester() {
        return new MagnitudeTypeTester();
    }

    public void testAgainstPercent() {
        assertTrue(getTester().test(EV).passed());
    }

    public void testAgainstNonsenseType() {
        assertFalse(new MagnitudeTypeTester("Nonsense Type").test(EV).passed());
    }

    public void testAgainstEvMagType() {
        String type = new MagnitudeExtractor().extract(EV).type;
        assertTrue(new MagnitudeTypeTester(type).test(type).passed());
    }
}
