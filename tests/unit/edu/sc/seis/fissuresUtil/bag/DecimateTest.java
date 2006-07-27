package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;

public class DecimateTest extends TestCase {

    public void testSimpleDecimate() throws Exception {
        int factor = 5;
        LocalSeismogramImpl seis = SimplePlotUtil.createTestData("test",
                                                                           new int[100],
                                                                           ClockUtil.now().getFissuresTime(),
                                                                           MockChannelId.createVerticalChanId(),
                                                                           new SamplingImpl(20,
                                                                                            new TimeInterval(1,
                                                                                                             UnitImpl.SECOND)));
        Decimate decimate = new Decimate(factor);
        LocalSeismogramImpl out = decimate.apply(seis);
        assertEquals("seis length", seis.getNumPoints()/factor, out.getNumPoints());
        assertEquals("sampling period", seis.getSampling().getPeriod().getValue(UnitImpl.SECOND),
                     out.getSampling().getPeriod().getValue(UnitImpl.SECOND)/factor, 0.000001);
    }
}
