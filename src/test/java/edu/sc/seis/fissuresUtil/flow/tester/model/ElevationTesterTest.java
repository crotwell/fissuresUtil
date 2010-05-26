package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.TesterTesterTesterTest;
import edu.sc.seis.fissuresUtil.mockFissures.MockLocation;

public class ElevationTesterTest extends TesterTesterTesterTest {

    public Tester getTester(){
        return new ElevationTester();
    }

    public void testNoArgTester() {
        assertEquals(true, getTester().test(MockLocation.create()).passed());
    }

    public void testAbove() {
        Location loc = MockLocation.create();
        loc.elevation = new QuantityImpl(100, UnitImpl.KILOMETER);
        assertEquals(false, new ElevationTester(200, 300).test(loc).passed());
    }

    public void testBelow() {
        Location loc = MockLocation.create();
        loc.elevation = new QuantityImpl(400, UnitImpl.KILOMETER);
        assertEquals(false, new ElevationTester(200, 300).test(loc).passed());
    }
}
