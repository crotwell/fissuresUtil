/**
 * TauPUtilTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.mockFissures.IfNetwork.MockStation;

public class TauPUtilTest extends TestCase {

    public void testCalcTravelTimes() throws Exception {
        TauPUtil taup = new TauPUtil("prem");
        Arrival[] arrivals = taup.calcTravelTimes(MockStation.createStation(),
                             MockOrigin.create(),
                             new String[] { "ttp" });
        assertTrue("num arrivals="+arrivals.length, arrivals.length>0);
    }
}

