package edu.sc.seis.fissuresUtil.display.configuration;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author groves Created on Feb 17, 2005
 */
public class SeismogramDisplayConfigurationTest extends TestCase {

    public void testSimplest() throws Exception {
        SeismogramDisplayConfiguration sdc = create("recsec");
        assertEquals("recordSection", sdc.getType());
    }

    public void testRecord() throws Exception {
        SeismogramDisplayConfiguration sdc = create("simplest");
        assertEquals("basic", sdc.getType());
    }

    public static SeismogramDisplayConfiguration create(String name)
            throws Exception {
        return SeismogramDisplayConfiguration.create(createElement("edu/sc/seis/fissuresUtil/display/configuration/"
                + name + ".xml"));
    }

    public static Element createElement(String loc) throws Exception {
        ClassLoader cl = SeismogramDisplayConfigurationTest.class.getClassLoader();
        InputStream source = cl.getResourceAsStream(loc);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(source);
        return doc.getDocumentElement();
    }
}