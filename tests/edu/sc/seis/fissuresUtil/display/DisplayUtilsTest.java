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

    public static LocalSeismogramImpl[] createThreeSeisArray(){
        LocalSeismogramImpl firstSeis = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl secondSeis = SimplePlotUtil.createSpike(new MicroSecondDate(5000));
        LocalSeismogramImpl thirdSeis = SimplePlotUtil.createSpike(new MicroSecondDate(100000));
        LocalSeismogramImpl[] seis = {firstSeis,secondSeis,thirdSeis};
        return seis;
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(){
        return createUnsortedThreeSeisArray(createThreeSeisArray());
    }

    private LocalSeismogramImpl[] createUnsortedThreeSeisArray(LocalSeismogramImpl[] seis){
        LocalSeismogramImpl[] unsorted = { seis[2], seis[1], seis[0] };
        return unsorted;
    }
}
