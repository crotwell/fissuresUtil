package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfigurationTest;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;

/**
 * @author groves Created on Feb 28, 2005
 */
public class RecordSectionDisplayTest extends TestCase {

    public RecordSectionDisplayTest(String name) {
        super(name);
        BasicConfigurator.configure(new NullAppender());
    }

    public void testOutputToPNG() throws IOException {
        File outPNG = new File("./recSecTestOutput.png");
        //outPNG.deleteOnExit();
        sd.outputToPNG(outPNG, new Dimension(600, 300));
    }

    public void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        sd = SeismogramDisplayConfigurationTest.create("recsec")
                .createDisplay();
        MemoryDataSet ds = new MemoryDataSet("id",
                                             "Test Data set",
                                             "None",
                                             null);
        ds.addParameter(StdDataSetParamNames.EVENT,
                        MockEventAccessOperations.createEvent(),
                        null);
        Station[] stations = MockStation.createMultiSplendoredStations(5, 2);
        Channel[] channels = new Channel[stations.length];
        for(int i = 0; i < stations.length; i++) {
            channels[i] = MockChannel.createMotionVector(stations[i])[0];
        }
        for(int i = 0; i < stations.length; i++) {
            LocalSeismogramImpl lsi = SimplePlotUtil.createSpike(ClockUtil.now()
                    .add(new TimeInterval(i, UnitImpl.MINUTE)));
            MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(lsi);
            memDSS.getRequestFilter().channel_id = channels[i].get_id();
            ds.addDataSetSeismogram(memDSS, null);
            ds.addParameter(StdDataSetParamNames.CHANNEL
                                    + ChannelIdUtil.toString(channels[i].get_id()),
                            channels[i],
                            null);
            sd.add(new MemoryDataSetSeismogram[] {memDSS});
        }
        //sd.add(new Flag(memDSS.getBeginMicroSecondDate()
        /// .add(new TimeInterval(1, UnitImpl.SECOND)), "P-Wave"));
    }

    private SeismogramDisplay sd;
}