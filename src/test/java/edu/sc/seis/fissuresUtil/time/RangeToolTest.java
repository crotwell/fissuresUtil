/**
 * DisplayUtilsTest.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.time;

import junit.framework.TestCase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.TestUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

public class RangeToolTest extends TestCase {

    public void testGetFullTimeRange() {
        LocalSeismogramImpl[] seis = TestUtils.createThreeSeisArray();
        MicroSecondTimeRange fullTime = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                 new MicroSecondDate(seis[2].getEndTime()));
        assertEquals(fullTime,
                     RangeTool.getFullTime(TestUtils.createUnsortedThreeSeisArray(seis)));
    }

    public void testAreContiguous() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(first.getEndTime());
        assertTrue("Touching end times should be contiguous",
                   RangeTool.areContiguous(first, second));
        assertTrue("Touching end times should be contiguous",
                   RangeTool.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl third = SimplePlotUtil.createSpike(first.getEndTime()
                .add(sampleInterval));
        assertTrue(RangeTool.areContiguous(first, third));
        assertTrue(RangeTool.areContiguous(third, first));
    }

    public void testAreNotContiguous() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(new MicroSecondDate(5));
        assertFalse(RangeTool.areContiguous(first, second));
        assertFalse(RangeTool.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        TimeInterval tripleInterval = (TimeInterval)sampleInterval.multiplyBy(3.0);
        LocalSeismogramImpl third = SimplePlotUtil.createSpike(first.getEndTime()
                .add(tripleInterval));
        assertFalse(RangeTool.areContiguous(first, third));
        assertFalse(RangeTool.areContiguous(third, first));
    }

    public void testAreOverlapping() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(new MicroSecondDate(5));
        assertTrue(RangeTool.areOverlapping(first, second));
        assertTrue(RangeTool.areOverlapping(second, first));
    }

    public void testAreNotOverlapping() {
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(first.getEndTime()
                .add(sampleInterval));
        assertFalse(RangeTool.areOverlapping(first, second));
        assertFalse(RangeTool.areOverlapping(second, first));
    }
}