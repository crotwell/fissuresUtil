/**
 * DataSetToXML.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataSetToXML
{

    public void save(DataSet dataset, File saveDirectory)
    throws IOException, ParserConfigurationException, MalformedURLException {
        File dataDir = new File(saveDirectory, "data");
        Document doc = createDocument(dataset, saveDirectory);
        Writer xmlWriter = new Writer(true);
        BufferedWriter buf =
            new BufferedWriter(new FileWriter(new File(saveDirectory, dataset.getName()+".dsml")));
        xmlWriter.setOutput(buf);
        xmlWriter.write(doc);
        buf.close();
    }

    public Document createDocument(DataSet dataset, File dataDirectory)
        throws ParserConfigurationException, MalformedURLException {
        DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element element = doc.createElement("dataset");
        insert(element, dataset, dataDirectory);
        doc.appendChild(element);
        return doc;
    }

    /** inserts the dataset, and all child datasets recursively, into the
     document, along with dataset seismograms and parameters if they can be
     stored. Note that all dataSetSeismograms are converted to
     URLDataSetSeismograms and stored in a directory structure that
     mirrors the dataset structure under the given directory. */
    public void insert(Element element, DataSet dataset, File directory)
        throws MalformedURLException {
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
            Element child = doc.createElement("dataset");
            insert(child, dataset.getDataSet(childDataSets[i]), directory);
            element.appendChild(child);
        }

        String[] childDSS = dataset.getDataSetSeismogramNames();
        for (int i = 0; i < childDSS.length; i++) {
            DataSetSeismogram dss = dataset.getDataSetSeismogram(childDSS[i]);
            URLDataSetSeismogram urlDSS;
            if (saveLocally || ! (dss instanceof URLDataSetSeismogram)) {
                urlDSS = URLDataSetSeismogram.localize(dss, directory);
            } else {
                urlDSS = (URLDataSetSeismogram)dss;
            }
            Element child = doc.createElement("urlDataSetSeismogram");
            urlDSS.insertInto(child);
            element.appendChild(child);
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

}

