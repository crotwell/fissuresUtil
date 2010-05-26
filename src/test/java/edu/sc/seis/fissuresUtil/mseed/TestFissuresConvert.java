package edu.sc.seis.fissuresUtil.mseed;

import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import junit.framework.TestCase;

public class TestFissuresConvert extends TestCase {

    public void testSamplingToMSeed() {
        TimeInterval sec = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl samp = new SamplingImpl(1, sec);
        short[] out = FissuresConvert.calcSeedMultipilerFactor(samp);
        assertEquals(samp + " factor", (short)32766, out[0]);
        assertEquals(samp + " multi", (short)-32766, out[1]);
        samp = new SamplingImpl(1, (TimeInterval)sec.multiplyBy(1800));
        SamplingImpl[] sampleList = new SamplingImpl[] {new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(1800)),
                                                        new SamplingImpl(20,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(0.05)),
                                                        new SamplingImpl(10,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(0.05001)),
                                                        new SamplingImpl(100,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(100))};
        for(int i = 0; i < sampleList.length; i++) {
            out = FissuresConvert.calcSeedMultipilerFactor(sampleList[i]);
            SamplingImpl converted = FissuresConvert.convertSampleRate(out[1],
                                                                       out[0]);
            assertEquals(sampleList[i] + " sampling converted=" + converted,
                         sampleList[i].getPeriod().getValue(UnitImpl.SECOND),
                         converted.getPeriod().getValue(UnitImpl.SECOND),
                         0.00001);
        }
        samp = new SamplingImpl(1, (TimeInterval)sec.multiplyBy(1800));
        out = FissuresConvert.calcSeedMultipilerFactor(samp);
        assertEquals(samp + " factor", (short)-32400, out[0]);
        assertEquals(samp + " multi", (short)18, out[1]);
        SamplingImpl converted = FissuresConvert.convertSampleRate(out[1], out[0]);
        assertEquals(samp + " sampling converted=" + converted,
                     samp.getPeriod().getValue(UnitImpl.SECOND),
                     converted.getPeriod().getValue(UnitImpl.SECOND),
                     0.00001);
    }
}
