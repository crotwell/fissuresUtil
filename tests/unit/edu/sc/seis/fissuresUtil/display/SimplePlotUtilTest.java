package edu.sc.seis.fissuresUtil.display;

import junit.framework.TestCase;
import junitx.framework.ArrayAssert;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;

/**
 * @author groves Created on Nov 4, 2004
 */
public class SimplePlotUtilTest extends TestCase {

    public void testMakePlottable() throws CodecException {
        LocalSeismogramImpl seis = SimplePlotUtil.createSpike(START_DATE,
                                                              ONE_HOUR,
                                                              SPS * 60);
        int numYSamples = (int)ONE_HOUR.convertTo(UnitImpl.SECOND).getValue() * 2;
        int[] yValues = new int[numYSamples];
        for(int i = 1; i < yValues.length; i += 2) {
            yValues[i] = (i - 1) % 120 == 0 ? 100 : 0;//Spike every minute
        }
        MicroSecondTimeRange tr = new MicroSecondTimeRange(START_DATE, ONE_HOUR);
        int[][] results = SimplePlotUtil.makePlottable(seis, tr, numYSamples * 24);
        ArrayAssert.assertEquals(yValues, results[1]);
    }

    public void testMakePlottableOnRaggedStart() throws CodecException {
        LocalSeismogramImpl seis = SimplePlotUtil.createRaggedSpike(START_DATE,
                                                                    ONE_HOUR,
                                                                    SPS * 60,
                                                                    1);
        int numYSamples = (int)ONE_HOUR.convertTo(UnitImpl.SECOND).getValue() * 2;
        int[] yValues = new int[numYSamples];
        for(int i = 3; i < yValues.length; i += 2) {
            yValues[i] = (i - 1) % 120 == 0 ? 100 : 0;//Spike every minute
        }
        MicroSecondTimeRange tr = new MicroSecondTimeRange(START_DATE, ONE_HOUR);
        int[][] results = SimplePlotUtil.makePlottable(seis, tr, numYSamples * 24);
        ArrayAssert.assertEquals(yValues, results[1]);
    }

    private static Time START_TIME = new Time("20000101T000000.000Z", 0);

    private static MicroSecondDate START_DATE = new MicroSecondDate(START_TIME);

    private static TimeInterval ONE_HOUR = new TimeInterval(1, UnitImpl.HOUR);

    private static int SPS = SimplePlotUtil.SPIKE_SAMPLES_PER_SECOND;
}