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
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    
    protected boolean saveLocally = true;
    
    /** If true, then each dataset is put into a separate dsml file. Otherwise
     the child datasets are embedded in the parent dsml file. */
    protected boolean useDataSetRef = true;
    
    static Logger logger = Logger.getLogger(DataSetToXML.class);
}

