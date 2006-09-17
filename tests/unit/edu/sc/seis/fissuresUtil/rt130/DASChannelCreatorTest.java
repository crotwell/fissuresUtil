package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import junit.framework.TestCase;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

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
}
