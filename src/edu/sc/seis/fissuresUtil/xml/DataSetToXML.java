/**
 * DataSetToXML.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class DataSetToXML
{

    public static Document createDocument(DataSet dataset) throws ParserConfigurationException {
        DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.createElement("dataset");

        // todo -- add actual dataset...

        return doc;
    }
}

