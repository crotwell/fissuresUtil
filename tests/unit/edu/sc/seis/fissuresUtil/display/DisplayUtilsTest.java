/**
 * DisplayUtilsTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class DisplayUtilsTest extends TestCase{
    public DisplayUtilsTest(String name){
        super(name);
    }

    public void testSortByDateSorted(){
        LocalSeismogramImpl[] seisArray = createThreeSeisArray();
        ArrayAssert.assertEquals(seisArray, DisplayUtils.sortByDate(seisArray));
    }

    public void testSortByDateUnsorted(){
        LocalSeismogramImpl[] originalSeis = createThreeSeisArray();
        LocalSeismogramImpl[] unsortedSeis = createUnsortedThreeSeisArray(originalSeis);
        LocalSeismogramImpl[] sortedSeis = DisplayUtils.sortByDate(unsortedSeis);
        ArrayAssert.assertEquals(originalSeis, sortedSeis);
    }

    public void testGetFullTimeRange(){
        LocalSeismogramImpl[] seis = createThreeSeisArray();
        MicroSecondTimeRange fullTime = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                 new MicroSecondDate(seis[2].getEndTime()));
        assertEquals(fullTime,DisplayUtils.getFullTime(createUnsortedThreeSeisArray(seis)));
    }

    public void testAreContiguous(){
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(first.getEndTime());
        assertTrue("Touching end times should be contiguous", DisplayUtils.areContiguous(first, second));
        assertTrue("Touching end times should be contiguous", DisplayUtils.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl third = SimplePlotUtil.createSpike(first.getEndTime().add(sampleInterval));
        assertTrue(DisplayUtils.areContiguous(first, third));
        assertTrue(DisplayUtils.areContiguous(third, first));
    }

    public void testAreNotContiguous(){
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(new MicroSecondDate(5));
        assertFalse(DisplayUtils.areContiguous(first, second));
        assertFalse(DisplayUtils.areContiguous(second, first));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        TimeInterval tripleInterval = (TimeInterval)sampleInterval.multiplyBy(3.0);
        LocalSeismogramImpl third = SimplePlotUtil.createSpike(first.getEndTime().add(tripleInterval));
        assertFalse(DisplayUtils.areContiguous(first, third));
        assertFalse(DisplayUtils.areContiguous(third, first));
    }

    public void testAreOverlapping(){
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(new MicroSecondDate(5));
        assertTrue(DisplayUtils.areOverlapping(first, second));
        assertTrue(DisplayUtils.areOverlapping(second, first));
    }

    public void testAreNotOverlapping(){
        LocalSeismogramImpl first = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        TimeInterval sampleInterval = first.getSampling().getPeriod();
        LocalSeismogramImpl second = SimplePlotUtil.createSpike(first.getEndTime().add(sampleInterval));
        assertFalse(DisplayUtils.areOverlapping(first, second));
        assertFalse(DisplayUtils.areOverlapping(second, first));
    }

    public static LocalSeismogramImpl[] createThreeSeisArray(){
        LocalSeismogramImpl[] seis = {firstSeis,secondSeis,thirdSeis};
        return seis;
    }

    public static LocalSeismogramImpl[] createOtherSeisArray(){
        LocalSeismogramImpl[] seis = {fourthSeis, fifthSeis, sixthSeis};
        return seis;
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(){
        return createUnsortedThreeSeisArray(createThreeSeisArray());
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(LocalSeismogramImpl[] seis){
        LocalSeismogramImpl[] unsorted = { seis[2], seis[1], seis[0] };
        return unsorted;
    }

    private static LocalSeismogramImpl firstSeis = SimplePlotUtil.createSpike(new MicroSecondDate(0));
    private static LocalSeismogramImpl secondSeis = SimplePlotUtil.createSpike(new MicroSecondDate(5000));
    private static LocalSeismogramImpl thirdSeis = SimplePlotUtil.createSpike(new MicroSecondDate(100000));

    private static LocalSeismogramImpl fourthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(2500));
    private static LocalSeismogramImpl fifthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(1000000));
    private static LocalSeismogramImpl sixthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(9000));


}
