package edu.sc.seis.fissuresUtil.time;

import junit.framework.TestCase;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;

public class ReduceToolTest extends TestCase {

    public void testDoubleRequest() {
        assertEquals(1,
                     ReduceTool.merge(new RequestFilter[] {fullRequest,
                                                           fullRequest}).length);
    }

    public void testDifferentChannelsWithOverlappingTimesAreLeftAloneByMinimizeRequest() {
        RequestFilter[] fullAndOther = new RequestFilter[] {fullRequest,
                                                            fullForOther};
        assertEquals(2, ReduceTool.merge(fullAndOther).length);
    }

    MicroSecondDate start = new MicroSecondDate();

    MicroSecondDate end = start.add(new TimeInterval(1, UnitImpl.HOUR));

    RequestFilter fullRequest = new RequestFilter(MockChannel.createChannel()
            .get_id(), start.getFissuresTime(), end.getFissuresTime());

    RequestFilter fullForOther = new RequestFilter(MockChannel.createNorthChannel()
                                                           .get_id(),
                                                   start.getFissuresTime(),
                                                   end.getFissuresTime());
}
