package edu.sc.seis.fissuresUtil.map;

import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;


/**
 * @author groves
 * Created on Nov 25, 2004
 */
public class OpenMapTest extends TestCase {
    public void testMapWriting() throws IOException{
        BasicConfigurator.configure();
        OpenMap om = new OpenMap();
        om.getStationLayer().stationDataChanged(new StationDataEvent(MockStation.createMultiSplendoredStations()));
        om.writeMapToPNG("test.png");
    }
}
