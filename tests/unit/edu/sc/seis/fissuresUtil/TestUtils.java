package edu.sc.seis.fissuresUtil;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Nov 2, 2004
 */
public class TestUtils {

    private static LocalSeismogramImpl firstSeis = SimplePlotUtil.createSpike(new MicroSecondDate(0));

    private static LocalSeismogramImpl secondSeis = SimplePlotUtil.createSpike(new MicroSecondDate(5000));

    private static LocalSeismogramImpl thirdSeis = SimplePlotUtil.createSpike(new MicroSecondDate(100000));

    private static LocalSeismogramImpl fourthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(2500));

    private static LocalSeismogramImpl fifthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(1000000));

    private static LocalSeismogramImpl sixthSeis = SimplePlotUtil.createSpike(new MicroSecondDate(9000));

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