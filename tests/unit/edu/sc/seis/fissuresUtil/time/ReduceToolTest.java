package edu.sc.seis.fissuresUtil.time;

import java.util.Date;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
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

    public void testContiguousSeismograms() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike();
        LocalSeismogramImpl second = createContiguous(first);
        assertEquals(1,
                     ReduceTool.merge(new LocalSeismogramImpl[] {second, first}).length);
    }

    private LocalSeismogramImpl createContiguous(LocalSeismogramImpl first) {
        return SimplePlotUtil.createSpike(first.getEndTime()
                .add(first.getSampling().getPeriod()));
    }

    public void testEqualSeismograms() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(start);
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(start);
        LocalSeismogramImpl[] result = ReduceTool.merge(new LocalSeismogramImpl[] {first,
                                                                                   second});
        assertEquals(1, result.length);
        assertEquals(first.getBeginTime(), result[0].getBeginTime());
        assertEquals(first.getEndTime(), result[0].getEndTime());
        assertEquals(first.getNumPoints(), result[0].getNumPoints());
    }

    public void testOverlappingSeismograms() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike();
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(first.getBeginTime()
                .add((TimeInterval)first.getTimeInterval().divideBy(2)));
        assertEquals(2,
                     ReduceTool.merge(new LocalSeismogramImpl[] {second, first}).length);
    }

    public void testContiguousEqualAndOverlappingSeismograms() {
        LocalSeismogramImpl base = SimplePlotUtil.createSpike(start);
        LocalSeismogramImpl overlap = SimplePlotUtil.createSpike(base.getBeginTime()
                .add((TimeInterval)base.getTimeInterval().divideBy(2)));
        LocalSeismogramImpl equal = SimplePlotUtil.createSpike(start);
        LocalSeismogramImpl contig = createContiguous(base);
        assertEquals(2,
                     ReduceTool.merge(new LocalSeismogramImpl[] {base,
                                                                 overlap,
                                                                 equal,
                                                                 contig}).length);
    }

    public void testOverlappingMSTR() {
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[2])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[3])}).length);
    }
    
    public void testContainedMSTR(){
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[3])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[2])}).length);
    }
    
    public void testSplitMSTR(){
        assertEquals(2,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[1]),
                                                                  new MicroSecondTimeRange(dates[2],
                                                                                           dates[3])}).length);
        assertEquals(2,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[2],
                                                                                           dates[3]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[1])}).length);
    }
    
    public void testTouchingEndBeginMSTR(){
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[0],
                                                                                           dates[1]),
                                                                  new MicroSecondTimeRange(dates[1],
                                                                                           dates[2])}).length);
        assertEquals(1,
                     ReduceTool.merge(new MicroSecondTimeRange[] {new MicroSecondTimeRange(dates[1],
                                                                                           dates[2]),
                                                                  new MicroSecondTimeRange(dates[0],
                                                                                           dates[1])}).length);
    }
static MicroSecondDate[] dates = new MicroSecondDate[4];
    static {
        BasicConfigurator.configure(new NullAppender());
        for(int i = 0; i < dates.length; i++) {
            dates[i] = new MicroSecondDate(new Date(i));
        }
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
