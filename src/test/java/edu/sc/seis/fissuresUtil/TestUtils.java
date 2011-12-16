package edu.sc.seis.fissuresUtil;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;

/**
 * @author groves Created on Nov 2, 2004
 */
public class TestUtils {

    private static LocalSeismogramImpl firstSeis = MockSeismogram.createSpike(new MicroSecondDate(0));

    private static LocalSeismogramImpl secondSeis = MockSeismogram.createSpike(new MicroSecondDate(5000));

    private static LocalSeismogramImpl thirdSeis = MockSeismogram.createSpike(new MicroSecondDate(100000));

    private static LocalSeismogramImpl fourthSeis = MockSeismogram.createSpike(new MicroSecondDate(2500));

    private static LocalSeismogramImpl fifthSeis = MockSeismogram.createSpike(new MicroSecondDate(1000000));

    private static LocalSeismogramImpl sixthSeis = MockSeismogram.createSpike(new MicroSecondDate(9000));

    public static LocalSeismogramImpl[] createUnsortedThreeSeisArray(LocalSeismogramImpl[] seis) {
        LocalSeismogramImpl[] unsorted = {seis[2], seis[1], seis[0]};
        return unsorted;
    }

    public static LocalSeismogramImpl[] createOtherSeisArray() {
        LocalSeismogramImpl[] seis = {fourthSeis, fifthSeis, sixthSeis};
        return seis;
    }

    public static LocalSeismogramImpl[] createThreeSeisArray() {
        LocalSeismogramImpl[] seis = {firstSeis, secondSeis, thirdSeis};
        return seis;
    }
}