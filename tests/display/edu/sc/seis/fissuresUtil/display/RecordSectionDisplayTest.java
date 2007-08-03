package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfigurationTest;
import edu.sc.seis.fissuresUtil.display.registrar.CustomLayOutConfig;
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
        // BasicConfigurator.configure();
    }

    public void testOutputToPNG() throws IOException {
        sd.outputToPNG(getFile(date, "png"), new Dimension(600, 300));
    }

    public void testOutputToPDF() throws IOException {
        sd.outputToPDF(getFile(date, "pdf"), true, false);
    }

    private OutputStream getFile(Date date, String extension)
            throws FileNotFoundException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss");
        File out = new File("./recSecTestOutput/" + extension + '/'
                + sdf.format(date) + '.' + extension);
        out.getParentFile().mkdirs();
        // out.deleteOnExit();
        return new BufferedOutputStream(new FileOutputStream(out));
    }

    public void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        sd = SeismogramDisplayConfigurationTest.create("recsec")
                .createDisplay();
        // sd = SeismogramDisplayConfigurationTest.create("recsec_noswap")
        // .createDisplay();
        ((RecordSectionDisplay)sd).setLayout(new CustomLayOutConfig(0, 180, 10));
        MemoryDataSet ds = new MemoryDataSet("id",
                                             "Test Data set",
                                             "None",
                                             null);
        EventAccessOperations event = MockEventAccessOperations.createEvent();
        ds.addParameter(StdDataSetParamNames.EVENT, event, null);
        Station[] stations = MockStation.createMultiSplendoredStations(3, 9);
        // Station[] stations = MockStation.createMultiSplendoredStations(1, 1);
        Channel[] channels = new Channel[stations.length];
        for(int i = 0; i < channels.length; i++) {
            channels[i] = MockChannel.createMotionVector(stations[i])[0];
            DistAz daz = new DistAz(channels[i], event);
            logger.debug("distance between station and event: "
                    + daz.getDelta() + " degrees");
        }
        for(int i = 0; i < channels.length; i++) {
            LocalSeismogramImpl lsi = SimplePlotUtil.createSpike();
            if(i % 5 == 0) {
                lsi = SimplePlotUtil.createTestData();
            } else if(i % 2 == 0) {
                lsi = SimplePlotUtil.createSineWave();
            }
            MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(lsi);
            memDSS.getRequestFilter().channel_id = channels[i].get_id();
            ds.addDataSetSeismogram(memDSS, null);
            ds.addParameter(StdDataSetParamNames.CHANNEL
                                    + ChannelIdUtil.toString(channels[i].get_id()),
                            channels[i],
                            null);
            sd.add(new MemoryDataSetSeismogram[] {memDSS});
        }
        // sd.add(new Flag(memDSS.getBeginMicroSecondDate()
        // .add(new TimeInterval(1, UnitImpl.SECOND)), "P-Wave"));
    }

    private SeismogramDisplay sd;

    private Date date = ClockUtil.now();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RecordSectionDisplayTest.class);
}