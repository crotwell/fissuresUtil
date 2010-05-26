package edu.sc.seis.fissuresUtil.time;

import junit.framework.TestCase;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;

/**
 * @author groves Created on Nov 2, 2004
 */
public class CoverageToolTest extends TestCase {

    public void testCompleteCoverage() {
        MicroSecondTimeRange mstr = makeTimeRange(0, 10);
        RequestFilter[] uncovered = CoverageTool.notCovered(toArray(TEN_RF),
                                                            toArray(mstr));
        assertEquals(0, uncovered.length);
    }

    public void testIncompleteCoverage() {
        MicroSecondTimeRange mstr = makeTimeRange(0, 5);
        RequestFilter[] uncovered = CoverageTool.notCovered(toArray(TEN_RF),
                                                            toArray(mstr));
        assertEquals(1, uncovered.length);
        RequestFilter actualUncovered = makeRF(5, 10);
        assertTrue(RequestFilterUtil.areEqual(actualUncovered, uncovered[0]));
    }

    public void testPatchyCoverage() {
        MicroSecondTimeRange[] patches = {makeTimeRange(1, 2),
                                          makeTimeRange(6, 7)};
        RequestFilter[] uncovered = CoverageTool.notCovered(toArray(TEN_RF),
                                                            patches);
        assertEquals(3, uncovered.length);
        RequestFilter[] actualUncovered = {makeRF(0, 1),
                                           makeRF(2, 6),
                                           makeRF(7, 10)};
        for(int i = 0; i < actualUncovered.length; i++) {
            assertTrue(RequestFilterUtil.areEqual(actualUncovered[i],
                                                  uncovered[i]));
        }
    }

    public void testPatchyCoverageIgnoreGaps() {
        MicroSecondTimeRange[] patches = {makeTimeRange(1, 2),
                                          makeTimeRange(6, 7)};
        RequestFilter[] uncovered = CoverageTool.notCoveredIgnoreGaps(toArray(TEN_RF),
                                                                      patches);
        assertEquals(2, uncovered.length);
        RequestFilter[] actualUncovered = {makeRF(0, 1), makeRF(7, 10)};
        for(int i = 0; i < actualUncovered.length; i++) {
            assertTrue(RequestFilterUtil.areEqual(actualUncovered[i],
                                                  uncovered[i]));
        }
    }

    public void testOverCovered() {
        MicroSecondTimeRange[] times = {makeTimeRange(-1, 15),
                                        makeTimeRange(2, 27)};
        RequestFilter rf = makeRF(2, 23);
        RequestFilter[] uncovered = CoverageTool.notCovered(toArray(rf), times);
        assertEquals(0, uncovered.length);
    }

    private static RequestFilter makeRF(int start, int end) {
        return new RequestFilter(MockChannelId.createVerticalChanId(),
                                 makeTime(start),
                                 makeTime(end));
    }

    private static MicroSecondTimeRange makeTimeRange(int start, int end) {
        return new MicroSecondTimeRange(makeTime(start), makeTime(end));
    }

    private static Time makeTime(int i) {
        return new MicroSecondDate(i * 1000).getFissuresTime();
    }

    public RequestFilter[] toArray(RequestFilter rf) {
        return new RequestFilter[] {rf};
    }

    public MicroSecondTimeRange[] toArray(MicroSecondTimeRange mstr) {
        return new MicroSecondTimeRange[] {mstr};
    }

    private static RequestFilter TEN_RF = makeRF(0, 10);
}