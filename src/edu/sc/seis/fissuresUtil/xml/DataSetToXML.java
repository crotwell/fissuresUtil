/**
 * DataSetToXML.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataSetToXML
{

    public Document createDocument(DataSet dataset) throws ParserConfigurationException {
        DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element element = doc.createElement("dataset");
        insert(element, dataset);
        doc.appendChild(element);
        return doc;
    }

    public void insert(Element element, DataSet dataset) {
        Document doc = element.getOwnerDocument();
        Attr id = doc.createAttribute("datasetid");
        id.setValue(dataset.getId());
        element.appendChild(id);
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      dataset.getName()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "owner",
                                                      dataset.getOwner()));
        String[] childDataSets = dataset.getDataSetNames();
        for (int i = 0; i < childDataSets.length; i++) {
            Element child = doc.createElement("datasetRef");
            insertRef(child, dataset.getDataSet(childDataSets[i]));
            element.appendChild(child);
        }

        String[] childDSS = dataset.getDataSetSeismogramNames();
        for (int i = 0; i < childDSS.length; i++) {
            if (saveLocally) {
                Element child = doc.createElement("urlDataSetSeismogram");
                DataSetSeismogram dss = dataset.getDataSetSeismogram(childDSS[i]);
                URLDataSetSeismogram urlDSS;
                if (dss instanceof URLDataSetSeismogram) {
                    urlDSS = (URLDataSetSeismogram)dss;
                } else {
                }
            }
        }

        String[] paramNames = dataset.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            Element parameter =
                doc.createElement("parameter");
            XMLParameter.insert(parameter,
                                paramNames[i],
                                dataset.getParameter(paramNames[i]));
            element.appendChild(parameter);
        }


    }

    /** inserts the child dataset as a datasetRef element. The URL is assumed
     *  to be in a subdirectory relative to the current dataset.
     */
    public void insertRef(Element element, DataSet dataset) {
    }

    protected boolean saveLocally = true;

    class URLDataSetSeismogramSaver implements SeisDataChangeListener {
        URLDataSetSeismogramSaver(URLDataSetSeismogram dss) {
            this.dss = dss;
            dss.retrieveData(this);
        }

        public void error(SeisDataErrorEvent sdce) {
            // TODO
        }

        public void finished(SeisDataChangeEvent sdce) {
            // TODO
        }

        public void pushData(SeisDataChangeEvent sdce) {
            // TODO
        }

        URLDataSetSeismogram dss;
    }
}

