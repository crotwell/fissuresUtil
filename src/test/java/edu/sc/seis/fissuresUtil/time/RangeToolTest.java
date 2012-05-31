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
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;

public class RangeToolTest extends TestCase {

    public void testGetFullTimeRange() {
        LocalSeismogramImpl[] seis = TestUtils.createThreeSeisArray();
        MicroSecondTimeRange fullTime = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                 new MicroSecondDate(seis[2].getEndTime()));
        assertEquals(fullTime,
                     RangeTool.getFullTime(TestUtils.createUnsortedThreeSeisArray(seis)));
    }

    public void testAreContiguous() {
        LocalSeismogramImpl first = MockSeismogram.createSpike(new MicroSecondDate(0));
        TimeInterval halfSample = (TimeInterval)first.getSampling().getPeriod().divideBy(1.9); // do little more than 1/2 sample so within 1/2 sample
        LocalSeismogramImpl second = MockSeismogram.createSpike(first.getEndTime().add(halfSample));
        assertTrue("Touching end times should be contiguous "+first.getEndTime()+"  "+second.getBeginTime()+"  "+first.getSampling().getPeriod(),
                   RangeTool.areContiguous(first, second));
        assertTrue("Touching end times should be contiguous",
                   RangeTool.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl third = MockSeismogram.createSpike(first.getEndTime()
                .add(sampleInterval));
        assertTrue(RangeTool.areContiguous(first, third));
        assertTrue(RangeTool.areContiguous(third, first));
    }

    public void testAreNotContiguous() {
        LocalSeismogramImpl first = MockSeismogram.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = MockSeismogram.createSpike(new MicroSecondDate(5));
        assertFalse(RangeTool.areContiguous(first, second));
        assertFalse(RangeTool.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        TimeInterval tripleInterval = (TimeInterval)sampleInterval.multiplyBy(3.0);
        LocalSeismogramImpl third = MockSeismogram.createSpike(first.getEndTime()
                .add(tripleInterval));
        assertFalse(RangeTool.areContiguous(first, third));
        assertFalse(RangeTool.areContiguous(third, first));
    }

    public void testAreOverlapping() {
        LocalSeismogramImpl first = MockSeismogram.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = MockSeismogram.createSpike(new MicroSecondDate(5));
        assertTrue(RangeTool.areOverlapping(first, second));
        assertTrue(RangeTool.areOverlapping(second, first));
    }

    public void testAreNotOverlapping() {
        LocalSeismogramImpl first = MockSeismogram.createSpike(new MicroSecondDate(0));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl second = MockSeismogram.createSpike(first.getEndTime()
                .add(sampleInterval));
        assertFalse(RangeTool.areOverlapping(first, second));
        assertFalse(RangeTool.areOverlapping(second, first));
    }
}