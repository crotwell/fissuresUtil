/**
 * BasicSeismogramDisplayTest.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;

public class BasicSeismogramDisplayTest extends TestCase {

    public BasicSeismogramDisplayTest(String name) {
        super(name);
        BasicConfigurator.configure(new NullAppender());
    }

    public void testOutputToPNG() throws IOException {
        File outPNG = new File("./testOutput.png");
        outPNG.deleteOnExit();
        sd.outputToPNG(outPNG);
    }

    public void testOutputToPDF() throws FileNotFoundException {
        File outPDF = new File("./testOutput.pdf");
        outPDF.deleteOnExit();
        SeismogramPDFBuilder.createPDF(sd, outPDF, 1);
    }

    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        LocalSeismogramImpl lsi = SimplePlotUtil.createTestData();
        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(lsi);
        sd = new BasicSeismogramDisplay();
        sd.add(new MemoryDataSetSeismogram[] {memDSS});
    }

    private SeismogramDisplay sd;
}