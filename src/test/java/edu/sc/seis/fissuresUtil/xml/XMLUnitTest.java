package edu.sc.seis.fissuresUtil.xml;

import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import edu.iris.Fissures.model.UnitImpl;

public class XMLUnitTest extends TestCase {

    public void testGetUnitDOM() throws Exception {
        Document doc1 = getDocument(METERS_PER_SECOND_XML);
        Element unitElement = (Element)doc1.getFirstChild();
        UnitImpl unit = (UnitImpl)XMLUnit.getUnit(unitElement);
        assertEquals(UnitImpl.METER_PER_SECOND, unit);
    }

    public void testGetUnitSTAX() throws Exception {
        XMLInputFactory inFact = XMLInputFactory.newInstance();
        XMLStreamReader parser = inFact.createXMLStreamReader(getInputStream(METERS_PER_SECOND_XML));
        XMLUtil.gotoNextStartElement(parser, "unit");
        UnitImpl unit = (UnitImpl)XMLUnit.getUnit(parser);
        assertEquals(UnitImpl.METER_PER_SECOND, unit);
    }

    public static InputStream getInputStream(String filename) {
        return new BufferedInputStream(ClassLoader.getSystemResourceAsStream(filename));
    }

    public static Document getDocument(String filename) throws Exception {
        return getDocumentBuilder().parse(new InputSource(getInputStream(filename)));
    }

    public static DocumentBuilder getDocumentBuilder() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    public static final String METERS_PER_SECOND_XML = "edu/sc/seis/fissuresUtil/xml/meterpersecond.xml";
}
