/**
 * BasicSeismogramDisplayTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramPDFBuilder;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

public class BasicSeismogramDisplayTest extends TestCase {
    public BasicSeismogramDisplayTest(String name){
        super(name);
        BasicConfigurator.configure(new NullAppender());
    }
    public void testOutputToPNG(){
        try{
            File outPNG = new File("./testOutput.png");
            outPNG.deleteOnExit();
            sd.outputToPNG(outPNG);
        }
        catch(IOException e){
            System.out.println("something unforseen has happened");
            e.printStackTrace();
        }
    }

    public void testOutputToPDF() throws FileNotFoundException{
        File outPDF = new File("./testOutput.pdf");
        outPDF.deleteOnExit();
        SeismogramPDFBuilder.createPDF(sd, outPDF, 1);
    }

    public void setUp(){
        LocalSeismogramImpl lsi = SimplePlotUtil.createTestData();
        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(lsi);
        sd = new BasicSeismogramDisplay();
        sd.add(new MemoryDataSetSeismogram[]{memDSS});
    }

    private SeismogramDisplay sd;
}

