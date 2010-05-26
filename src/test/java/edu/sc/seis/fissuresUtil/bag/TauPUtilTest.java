/**
 * TauPUtilTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class TauPUtilTest extends TestCase {

    public void testCalcTravelTimes() throws Exception {
        TauPUtil taup = TauPUtil.getTauPUtil();
        Arrival[] arrivals = taup.calcTravelTimes(MockStation.createStation(),
                             MockOrigin.create(),
                             new String[] { "ttp" });
        for (int i = 0; i < arrivals.length; i++) {
            System.out.println(i+" "+arrivals[i].toString());
        }
        assertTrue("num arrivals="+arrivals.length, arrivals.length>0);
    }
}

