package edu.sc.seis.fissuresUtil.XMLToDataSet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.fissuresUtil.database.JDBCTearDown;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class XMLToDataSetTest extends JDBCTearDown {

    public XMLToDataSetTest() {
        BasicConfigurator.configure();
    }

    public void setUp() throws MalformedURLException {
        url = new URL(fileLoc1);        
    }

    public void testGeneral() throws IOException, ParserConfigurationException, IncomprehensibleDSMLException, UnsupportedFileTypeException {
        DataSet dataSet = DataSetToXML.load(url);
        assertTrue(dataSet.getName().equals("Event_2004_249_10_07_07"));
        assertTrue(dataSet.getOwner().equals("groves"));
        assertTrue(dataSet.getId().equals("2004-09-05T10:07:07.000Z"));
  
        String[] dataSetNames = dataSet.getDataSetNames();
        for(int i = 0; i < dataSetNames.length; i++){
            //System.out.println(dataSet.getDataSet(dataSetNames[i]).getName());
        }
        
        assertTrue(dataSet.getDataSet(dataSetNames[0]).getName().equals("Event_2004_249_10_07_07"));
        //System.out.println(dataSet.getDataSet(dataSetNames[0]).getOwner());
    }

    private URL url;
    
    private String fileLoc1 = "http://rev.seis.sc.edu/sod/seismograms/2004_249_10_07_07_+0000/Event_2004_249_10_07_07.dsml";
    
    private String fileLoc2 = "http://rev.seis.sc.edu/sod/seismograms/Master.dsml";
    
    private String fileLoc3 = "file:D:/src/seis/fissuresUtil/Event_2004_249_10_07_07.dsml";
    
    
}