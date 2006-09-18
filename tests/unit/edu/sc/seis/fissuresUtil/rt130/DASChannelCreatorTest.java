package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import junit.framework.TestCase;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.bag.OrientationUtil;

public class DASChannelCreatorTest extends TestCase {

    public void testUnknownDasChannelCreation() {
        DASChannelCreator creator = new DASChannelCreator(NCReaderTest.net,
                                                          new SamplingFinder() {

                                                              public int find(String file)
                                                                      throws RT130FormatException,
                                                                      IOException {
                                                                  return 40;
                                                              }
                                                          });
        MicroSecondDate now = new MicroSecondDate();
        Channel[] chans = creator.create("1337", now, "1/00331133");
        assertEquals(3, chans.length);
        assertEquals("1337", chans[0].my_site.my_station.get_code());
        // We assume that the channels are BH[ZNE]
        assertEquals("BHZ", chans[0].get_code());
        Channel[] newChans = creator.create("1337",
                                            now.subtract(new TimeInterval(1,
                                                                          UnitImpl.MINUTE)),
                                            "1/00331133");
        // Dummy DAS channels just expand to fill whatever time is requsted of
        // that das
        for(int i = 0; i < newChans.length; i++) {
            assertEquals(chans[i], newChans[i]);
        }
    }

    public void testParseOrientations() {
        Orientation[] ors = DASChannelCreator.parseOrientations("1/-90/0:2/0/352:3/0/82");
        assertTrue(OrientationUtil.areEqual(new Orientation(0, -90), ors[0]));
        assertTrue(OrientationUtil.areEqual(new Orientation(352, 0), ors[1]));
        assertTrue(OrientationUtil.areEqual(new Orientation(82, 0), ors[2]));
        ors = DASChannelCreator.parseOrientations("default");
        assertTrue(OrientationUtil.areEqual(new Orientation(0, -90), ors[0]));
        assertTrue(OrientationUtil.areEqual(new Orientation(0, 0), ors[1]));
        assertTrue(OrientationUtil.areEqual(new Orientation(90, 0), ors[2]));
    }
}
