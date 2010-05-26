package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;
import edu.sc.seis.fissuresUtil.flow.tester.TesterTesterTesterTest;

public class TimeTesterTest extends TesterTesterTesterTest{

    private static final MicroSecondDate NOW = new MicroSecondDate();

    private static final TimeInterval ONE_DAY = new TimeInterval(1,
                                                                 UnitImpl.DAY);

    private static final MicroSecondTimeRange NOW_TILL_TOMORROW = new MicroSecondTimeRange(NOW,
                                                                                           ONE_DAY);

    public void testBasic() {
        Time t = NOW.getFissuresTime();
        assertEquals(true, getTester().test(t).passed());
    }

    public void testBefore() {
        Time t = NOW.subtract(ONE_DAY).getFissuresTime();
        assertEquals(false, getTester().test(t).passed());
    }

    public void testAfter() {
        Time t = NOW_TILL_TOMORROW.getEndTime().add(ONE_DAY).getFissuresTime();
        assertEquals(false, getTester().test(t).passed());
    }

    public Tester getTester() {
        return new TimeTester(NOW_TILL_TOMORROW);
    }
}
