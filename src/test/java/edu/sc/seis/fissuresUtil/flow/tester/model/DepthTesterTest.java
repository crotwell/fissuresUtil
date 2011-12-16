package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.TesterTesterTesterTest;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class DepthTesterTest extends TesterTesterTesterTest {

    public Tester getTester(){
        return new DepthTester();
    }

    public void testNoArgTester() {
        assertEquals(true, getTester().test(MockLocation.create()).passed());
    }

    public void testAbove() {
        Location loc = MockLocation.create();
        loc.depth = new QuantityImpl(100, UnitImpl.KILOMETER);
        assertEquals(false, new DepthTester(200, 300).test(loc).passed());
    }

    public void testBelow() {
        Location loc = MockLocation.create();
        loc.depth = new QuantityImpl(400, UnitImpl.KILOMETER);
        assertEquals(false, new DepthTester(200, 300).test(loc).passed());
    }
}
