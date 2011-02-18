package edu.sc.seis.fissuresUtil.xml.StAX;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import edu.iris.Fissures.AuditInfo;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.URLDataSet;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.fissuresUtil.xml.XMLParameter;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;

public class StAXToDataSet {

    public StAXToDataSet(XMLStreamReader parser, URL base) {
        this.base = base;
        this.parser = parser;
    }

    public void addDataSet(MemoryDataSet dataset, AuditInfo[] audit)
            throws MalformedURLException, XMLStreamException {
        dataset.addDataSet(new URLDataSet(parser.getAttributeValue(null, "title"),
                                          new URL(base,
                                                  parser.getAttributeValue(null, "href"))),
                           audit);
        XMLUtil.getNextStartElement(parser);
    }

    public void addDataSetSeismogram(MemoryDataSet dataset, AuditInfo[] audit)
            throws MalformedURLException, UnsupportedFileTypeException, XMLStreamException {
        dataset.addDataSetSeismogram(URLDataSetSeismogram.getURLDataSetSeismogram(base, parser), audit);
    }

    public void addParameter(MemoryDataSet dataset, AuditInfo[] audit) throws XMLStreamException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String paramName = parser.getElementText();
        Object o = XMLParameter.getParameter(parser, paramName);
        dataset.addParameter(paramName, o, audit);
    }

    private XMLStreamReader parser;

    private URL base;
}
