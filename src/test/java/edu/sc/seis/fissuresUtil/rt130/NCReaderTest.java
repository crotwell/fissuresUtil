package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;

public class NCReaderTest extends TestCase {

    public void testSimple() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(getStream("edu/sc/seis/fissuresUtil/rt130/simple.nc"));
        assertEquals(1, delux.getSites().size());
        Site site = (Site)delux.getSites().get(0);
        Station sta = site.getStation();
        assertEquals("SNP56", sta.get_code());
        assertEquals("XE", sta.getNetworkAttr().get_code());
        assertEquals(-119.1693, sta.getLocation().longitude, .01);
    }

    public void testStartHandler() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(new StringReader("START 2005:139:19"));
        assertEquals(0, delux.getNumUnhandledLines());
    }

    public void testHeaderHandler() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(new StringReader("name   DAS/chan         sensor/model    chan/dip/azi"));
        assertEquals(0, delux.getNumUnhandledLines());
    }

    public void testTwoStations() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(getStream("edu/sc/seis/fissuresUtil/rt130/twostation.nc"));
        assertEquals(2, delux.getSites().size());
        assertEnded(((Site)delux.getSites().get(1)).getEndTime());
    }

    public void testMovedSite() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(getStream("edu/sc/seis/fissuresUtil/rt130/newsite.nc"));
        Site[] sites = (Site[])delux.getSites().toArray(new Site[] {});
        assertEquals(4, sites.length);
        for(int i = 1; i < sites.length; i++) {
            assertEquals(sites[i - 1].getStation(), sites[i].getStation());
        }
        assertEnded(sites[0].getEndTime());
        assertEnded(sites[0].getStation().getEndTime());
    }

    public void testSensorSwap() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(getStream("edu/sc/seis/fissuresUtil/rt130/sensor_swap.nc"));
        Site[] sites = (Site[])delux.getSites().toArray(new Site[0]);
        assertEquals(2, sites.length);
    }

    public void testReadingNonstandardDirection() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        delux.load(getStream("edu/sc/seis/fissuresUtil/rt130/nonstandarddirection.nc"));
        assertEquals(1, delux.getSites().size());
        assertEquals(0, delux.getNumUnhandledLines());
    }

    public void assertEnded(Time t) {
        assertTrue(new MicroSecondDate(t).before(TimeUtils.future));
    }

    public void testBadLoads() throws IOException {
        NCReader delux = new NCReader(net, initialLocs);
        try {
            delux.load(new StringReader("END"));
        } catch(NCReader.FormatException fe) {
            assertEquals(1, fe.getLineNum());
        }
        try {
            delux.load(new StringReader("START 2005:123:23\nSTART 2005:123:13"));
        } catch(NCReader.FormatException fe) {
            assertEquals(2, fe.getLineNum());
        }
        try {
            delux.load(new StringReader("SNP56   9515/123        T3N37/CMG3T     default"));
        } catch(NCReader.FormatException fe) {
            assertEquals(1, fe.getLineNum());
        }
    }

    public static InputStream getStream(String path) {
        return NCReader.class.getClassLoader().getResourceAsStream(path);
    }

    public static final NetworkAttr net;

    static Map initialLocs;
    static {
        BasicConfigurator.configure(new NullAppender());
        Properties netProps = new Properties();
        try {
            netProps.load(getStream("edu/sc/seis/fissuresUtil/rt130/snep.netprops"));
            initialLocs = XYReaderTest.createMultiStationMap();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        net = PopulationProperties.getNetworkAttr(netProps);
    }
}
