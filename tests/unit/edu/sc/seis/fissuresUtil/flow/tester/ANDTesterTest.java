package edu.sc.seis.fissuresUtil.flow.tester;

import edu.sc.seis.fissuresUtil.flow.tester.model.DepthTester;
import edu.sc.seis.fissuresUtil.flow.tester.model.TimeTester;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;

public class ANDTesterTest extends TesterTesterTesterTest {

    public Tester getTester() {
        return new ANDTester(new Tester[] {new TimeTester(), new DepthTester()});
    }

    public void testEvent() {
        assertTrue(getTester().test(MockEventAccessOperations.createEvent())
                .passed());
    }
}
