/**
 * LongShortStoNTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import junit.framework.TestCase;

public class LongShortStoNTest extends TestCase {

    public void testSimple() throws FissuresException {
        LongShortStoN ston = new LongShortStoN(new TimeInterval(4, UnitImpl.SECOND),
                                               new TimeInterval(1, UnitImpl.SECOND),
                                               2);
        int[] datadata = { 1, 2, 1, 2, 1, 1, 19, -6, 6, -2, 1, 3, 5, -3, -1, 1 };
        int[] data = new int[1000];
        System.arraycopy(datadata, 0, data, 800, datadata.length);
        LocalSeismogramImpl seis = SimplePlotUtil.createTestData("est", data);
        seis.sampling_info = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
        LongShortTrigger[] triggers = ston.calcTriggers(seis);
      //  System.out.println("Found "+triggers.length+" triggers");
      //  for (int i = 0; i < triggers.length; i++) {
      //      System.out.println(triggers[i].getIndex()+"  "+triggers[i].getWhen()+"  "+triggers[i].getValue());
      //  }
    }

    public void testConstant() throws Exception {
        LongShortStoN ston = new LongShortStoN(new TimeInterval(100, UnitImpl.SECOND),
                                               new TimeInterval(6, UnitImpl.SECOND),
                                               .2f);
        int[] data = new int[1200];
        for (int i = 0; i < data.length; i++) {
            if (i%2000 > 1000 && i%2000<1010) {
                data[i] = 5*(i%2);
            } else {
                data[i] = i%2;
            }
        }
        LocalSeismogramImpl seis = SimplePlotUtil.createTestData("est", data);
        seis.sampling_info = new SamplingImpl(1, new TimeInterval(.1f, UnitImpl.SECOND));
        LongShortTrigger[] triggers = ston.calcTriggers(seis);
        System.out.println("Found "+triggers.length+" triggers");
        for (int i = 0; i < triggers.length; i++) {
            System.out.println(triggers[i].getIndex()+"  "+triggers[i].getWhen()+"  "+triggers[i].getValue());
        }
    }


    /*
    pooh 4>reftrig -l 100 -s 6 -i 03.141.18.40.32.0551.6.m
reftrig:  Version Number  97.329
03.141.18.40.32.0551.6.m        2003:141:18:49:21.412   200     8854
03.141.18.40.32.0551.6.m        2003:141:18:49:43.562   1478    10211

     */
}


