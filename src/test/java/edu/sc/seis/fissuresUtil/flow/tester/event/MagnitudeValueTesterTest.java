package edu.sc.seis.fissuresUtil.flow.tester.event;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.TesterTesterTesterTest;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockMagnitude;

public class MagnitudeValueTesterTest extends TesterTesterTesterTest {

    public Tester getTester() {
        return new MagnitudeValueTester(1, 9);
    }
    
    public void testAgainstNoArg(){
        assertTrue(getTester().test(MockMagnitude.createMagnitude()).passed());
    }
    
    public void testUnder(){
        Magnitude m = MockMagnitude.createMagnitude();
        m.value = 0;
        assertFalse(getTester().test(m).passed());
    }
    
    public void testOver(){
        Magnitude m = MockMagnitude.createMagnitude();
        m.value = 10;
        assertFalse(getTester().test(m).passed());
    }

    
    public void testModded(){
        Magnitude m = MockMagnitude.createMagnitude();
        m.value = 5;
        assertTrue(getTester().test(m).passed());
    }
}
