package edu.sc.seis.fissuresUtil.time;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.TestUtils;

/**
 * @author groves Created on Nov 2, 2004
 */
public class SortToolTest extends TestCase {

    public void testSortByDateSorted() {
        LocalSeismogramImpl[] seisArray = TestUtils.createThreeSeisArray();
        ArrayAssert.assertEquals(seisArray,
                                 SortTool.byBeginTimeAscending(seisArray));
    }

    public void testSortByDateUnsorted() {
        LocalSeismogramImpl[] originalSeis = TestUtils.createThreeSeisArray();
        LocalSeismogramImpl[] unsortedSeis = TestUtils.createUnsortedThreeSeisArray(originalSeis);
        LocalSeismogramImpl[] sortedSeis = SortTool.byBeginTimeAscending(unsortedSeis);
        ArrayAssert.assertEquals(originalSeis, sortedSeis);
    }
}