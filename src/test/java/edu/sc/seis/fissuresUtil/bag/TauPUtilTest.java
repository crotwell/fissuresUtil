/**
 * TauPUtilTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.bag;

import java.util.List;

import junit.framework.TestCase;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class TauPUtilTest extends TestCase {

    public void testCalcTravelTimes() throws Exception {
        TauPUtil taup = TauPUtil.getTauPUtil();
        List<Arrival> arrivals = taup.calcTravelTimes(MockStation.createStation(),
                             MockOrigin.create(),
                             new String[] { "ttp" });
        for (Arrival arrival : arrivals) {
            System.out.println(" "+arrival.toString());
        }
        assertTrue("num arrivals="+arrivals.size(), arrivals.size()>0);
    }
}

