/**
 * DataSetToXML.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.AuditInfo;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataSetToXML {
    
    /** Saves the given dataset to an xml file in the given directory. The
     file is returned. */
    public File save(DataSet dataset, File saveDirectory)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Element doc = createDocument(dataset, saveDirectory);
        Writer xmlWriter = new Writer();
        String filename =  dataset.getName()+".dsml";
        filename = filename.replaceAll(" ","_");
        logger.debug("save to "+filename+" in "+saveDirectory.toString());
        saveDirectory.mkdirs();
        File outFile = new File(saveDirectory, filename);
        BufferedWriter buf =
            new BufferedWriter(new FileWriter(outFile));
        xmlWriter.setOutput(buf);
        xmlWriter.write(doc);
        buf.close();
        logger.debug("Done with save to "+saveDirectory.toString());
        return outFile;
    }
    
    public Element createDocument(DataSet dataset, File dataDirectory)
        throws IOException, ParserConfigurationException, MalformedURLException {
        
        if(!dataDirectory.exists() ) {
            dataDirectory.mkdirs();
        }
        
        DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element element = doc.createElement("dataset");
        element.setAttribute("xmlns:dataset", "http://www.seis.sc.edu/xschema/dataset/2.0");
        element.setAttribute("xmlns", "http://www.seis.sc.edu/xschema/dataset/2.0");
        element.setAttribute("xsi:schemaLocation",
                             "http://www.seis.sc.edu/xschema/dataset/2.0 http://www.seis.sc.edu/xschema/dataset/2.0/dataset.xsd");
        element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        element.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        doc.appendChild(element);
        insert(element, dataset, dataDirectory);
        return element;
    }
    
    /** inserts the dataset, and all child datasets recursively, into the
     document, along with dataset seismograms and parameters if they can be
     stored. Note that all dataSetSeismograms are converted to
     URLDataSetSeismograms and stored in a directory structure that
     mirrors the dataset structure under the given directory. */
    public void insert(Element element, DataSet dataset, File directory)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Document doc = element.getOwnerDocument();
        element.setAttribute("datasetid", dataset.getId());
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      dataset.getName()));
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "owner",
                                                      dataset.getOwner()));
        String[] childDataSets = dataset.getDataSetNames();
        for (int i = 0; i < childDataSets.length; i++) {
            String childDirName = childDataSets[i].replace(' ','_');
            File childDirectory = new File(directory, childDirName);
            if ( ! childDirectory.exists()) {
                childDirectory.mkdirs();
            }
            
            Element child;
            if (useDataSetRef) {
                child = doc.createElement("datasetRef");
                insertRef(child, dataset.getDataSet(childDataSets[i]), childDirectory);
            } else {
                child = doc.createElement("dataset");
                insert(child, dataset.getDataSet(childDataSets[i]), childDirectory);
            }
            element.appendChild(child);
        }
        
        String[] childDSS = dataset.getDataSetSeismogramNames();
        File dataDir = new File(directory, "data");
        dataDir.mkdirs();
        for (int i = 0; i < childDSS.length; i++) {
            DataSetSeismogram dss = dataset.getDataSetSeismogram(childDSS[i]);
            URLDataSetSeismogram urlDSS;
            if (saveLocally || ! (dss instanceof URLDataSetSeismogram)) {
                urlDSS = URLDataSetSeismogram.localize(dss, dataDir);
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
    public void insertRef(Element element, DataSet dataset, File directory)
        throws IOException, ParserConfigurationException, MalformedURLException {
        File dsFile = save(dataset, directory);
        element.setAttribute("xlink:href", dsFile.getPath());
        element.setAttribute("xlink:type", "simple");
        element.setAttribute("xlink:title", dataset.getName());
    }
    /**
     * Load a xml dataset from a URL.
     *
     * @param datasetURL an <code>URL</code> to an xml dataset
     * @return a <code>XMLDataSet</code> populated form the URL
     */
    public static DataSet load(URL datasetURL)
        throws IOException, ParserConfigurationException, SAXException {
        DataSet dataset = null;
        
        DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
        
        Document doc = docBuilder.parse(new BufferedInputStream(datasetURL.openStream()));
        Element docElement = doc.getDocumentElement();
        
        if (docElement.getTagName().equals("dataset")) {
            DataSetToXML dataSetToXML = new DataSetToXML();
            dataset = dataSetToXML.extract(datasetURL, docElement);
        }
        
        return dataset;
        
    }
    
    /** Extracts the dataset from the element, which is assumed to be a
     &lt;dataset&gt; element. */
    public DataSet extract(URL base, Element element) throws MalformedURLException{
        String name = "";
        String owner = "";
        String id = "";
        Node temp;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("name")) {
                name = child.getNodeValue();
            } else if (child.getNodeName().equals("id")) {
                name = child.getNodeValue();
            } else if (child.getNodeName().equals("owner")) {
                name = child.getNodeValue();
            }
        }
        // all 3 should be populated now
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo("loaded from "+base.toString(),
                                 System.getProperty("user.name"));
        MemoryDataSet dataset = new MemoryDataSet(id, name, owner, audit);
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeName().equals("dataset")) {
                dataset.addDataSet(extract(base, (Element)child), audit);
            } else if (child.getNodeName().equals("datasetRef")) {
                Element childElement = (Element)child;
                dataset.addDataSet(new URLDataSet(childElement.getAttribute("xlink:title)"),
                                   new URL(base, childElement.getAttribute("xlink:href"))),
                                   audit);
            } else if (child.getNodeName().equals("urlDataSetSeismogram")) {
                dataset.addDataSetSeismogram(URLDataSetSeismogram.getURLDataSetSeismogram(base, (Element)child), audit);
            } else if (child.getNodeName().equals("parameter")) {
                String paramName =
                    XMLUtil.getText(XMLUtil.getElement((Element)child, "name"));
                Object o = XMLParameter.getParameter((Element)child);
                dataset.addParameter(paramName, o, audit);
            }
        }
        return dataset;
    }
    
    protected boolean saveLocally = true;
    
    /** If true, then each dataset is put into a separate dsml file. Otherwise
     the child datasets are embedded in the parent dsml file. */
    protected boolean useDataSetRef = true;
    
    static Logger logger = Logger.getLogger(DataSetToXML.class);
}

