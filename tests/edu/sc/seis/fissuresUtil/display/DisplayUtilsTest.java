/**
 * DisplayUtilsTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

public class DisplayUtilsTest extends TestCase{
    public DisplayUtilsTest(String name){
        super(name);
    }

    public void testSortByDateSorted(){
        LocalSeismogramImpl[] seisArray = createSortedThreeSeisArray();
        ArrayAssert.assertEquals(seisArray, DisplayUtils.sortByDate(seisArray));
    }

    public void testSortByDateUnsorted(){
        LocalSeismogramImpl[] originalSeis = createSortedThreeSeisArray();
        LocalSeismogramImpl[] unsortedSeis = createUnsortedThreeSeisArray(originalSeis);
        LocalSeismogramImpl[] sortedSeis = DisplayUtils.sortByDate(unsortedSeis);
        ArrayAssert.assertEquals(originalSeis, sortedSeis);
    }

    public void testGetFullTimeRange(){
        LocalSeismogramImpl[] seis = createSortedThreeSeisArray();
        MicroSecondTimeRange fullTime = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                 new MicroSecondDate(seis[2].getEndTime()));
        assertEquals(fullTime,DisplayUtils.getFullTime(createUnsortedThreeSeisArray(seis)));
    }

    private LocalSeismogramImpl[] createSortedThreeSeisArray(){
        LocalSeismogramImpl firstSeis = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl secondSeis = SimplePlotUtil.createSpike(new MicroSecondDate(5000));
        LocalSeismogramImpl thirdSeis = SimplePlotUtil.createSpike(new MicroSecondDate(100000));
        LocalSeismogramImpl[] seis = {firstSeis,secondSeis,thirdSeis};
        return seis;
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(){
        return createUnsortedThreeSeisArray(createSortedThreeSeisArray());
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(LocalSeismogramImpl[] seis){
        LocalSeismogramImpl[] unsorted = { seis[2], seis[1], seis[0] };
        return unsorted;
    }
}
