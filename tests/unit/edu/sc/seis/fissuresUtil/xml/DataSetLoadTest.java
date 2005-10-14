package edu.sc.seis.fissuresUtil.xml;

import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;

public class DataSetLoadTest extends TestCase {

    public void testRevDSML() throws IOException, ParserConfigurationException,
            IncomprehensibleDSMLException, UnsupportedFileTypeException {
        String fullFileName = "edu/sc/seis/fissuresUtil/xml/rev.dsml";
        URL dsurl = (DataSetLoadTest.class).getClassLoader()
                .getResource(fullFileName);
        DataSet ds = DataSetToXML.load(dsurl);
        String[] seisNames = ds.getDataSetSeismogramNames();
        for(int i = 0; i < seisNames.length; i++) {
            DataSetSeismogram seis = ds.getDataSetSeismogram(seisNames[i]);
            assertNotNull(seis.getChannelId());
            assertNotNull(seis.getChannel());
            assertNotNull(seis.getEvent());
        }
    }
}
