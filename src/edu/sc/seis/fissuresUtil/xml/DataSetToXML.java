/**
 * DataSetToXML.java
 *
 * This is the DOM-based DataSetToXML
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
        return save(dataset, saveDirectory, SeismogramFileTypes.SAC);
    }

    /** Saves the given dataset to an xml file in the given directory. The
     file is returned. */
    public File save(DataSet dataset, File saveDirectory, SeismogramFileTypes fileType)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Element doc = createDocument(dataset, saveDirectory, fileType);
        String filename = createFileName(dataset);
        logger.debug("save to "+filename+" in "+saveDirectory.toString());
        saveDirectory.mkdirs();
        File outFile = new File(saveDirectory, filename);
        writeToFile(doc, outFile);
        logger.debug("Done with save to "+saveDirectory.toString());
        return outFile;
    }

    public static String createFileName(DataSet dataset) {
        String filename =  dataset.getName()+".dsml";
        filename = filename.replaceAll(" ","_");
        filename = filename.replaceAll(",","_");
        filename = filename.replaceAll("/","_");
        filename = filename.replaceAll(":","_");
        //  filename = filename.replaceAll("\\","_");
        return filename;
    }


    public void writeToFile(Element datasetElement, File outFile)
        throws IOException, MalformedURLException {
        File tempFile;
        if (outFile.exists()) {
            tempFile = File.createTempFile("Temp_"+outFile.getName(), "dsml", outFile.getParentFile());
        } else {
            tempFile = outFile;
        }
        BufferedWriter buf = new BufferedWriter(new FileWriter(tempFile));
        Writer xmlWriter = new Writer(false, true);
        xmlWriter.setOutput(buf);
        xmlWriter.write(datasetElement);
        buf.close();
        if (outFile != tempFile) {
            if ( ! tempFile.renameTo(outFile)) {
                //If unable to rename the tempfile, delete it and try again
                if(outFile.delete()){
                    tempFile.renameTo(outFile);
                }else{
                    throw new IOException("Unable to move temp file over old file");
                }
            }
        }
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return XMLDataSet.getDocumentBuilder();
    }

    public Element createDocument(DataSet dataset, File dataDirectory, SeismogramFileTypes fileType)
        throws IOException, ParserConfigurationException, MalformedURLException {

        if(!dataDirectory.exists() ) {
            dataDirectory.mkdirs();
        }

        DocumentBuilder docBuilder = getDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element element = insert(doc, dataset, dataDirectory, fileType);
        return element;
    }

    /**
     * DOM insert
     * inserts the dataset, and all child datasets recursively, into the
     * document, along with dataset seismograms and parameters if they can be
     * stored. Note that all dataSetSeismograms are converted to
     * URLDataSetSeismograms and stored in a directory structure that
     * mirrors the dataset structure under the given directory.
     */
    public Element insert(Element parent, DataSet dataset, File directory, SeismogramFileTypes fileType)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Element child = parent.getOwnerDocument().createElement("dataset");
        insertInto(child, dataset, directory, fileType);
        parent.appendChild(child);
        return child;
    }

    /**
     * DOM insert
     */
    public Element insert(Document doc, DataSet dataset, File directory, SeismogramFileTypes fileType)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Element element = doc.createElement("dataset");
        element.setAttribute("xmlns:dataset", "http://www.seis.sc.edu/xschema/dataset/2.0");
        element.setAttribute("xmlns", "http://www.seis.sc.edu/xschema/dataset/2.0");
        element.setAttribute("xsi:schemaLocation",
                             "http://www.seis.sc.edu/xschema/dataset/2.0 http://www.seis.sc.edu/xschema/dataset/2.0/dataset.xsd");
        element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        element.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        insertInto(element, dataset, directory, fileType);
        doc.appendChild(element);
        return element;
    }

    /**
     * DOM insertInto
     */
    public void insertInto(Element element, DataSet dataset, File directory, SeismogramFileTypes fileType)
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
            String childDirName = createFileName(dataset.getDataSet(childDataSets[i]));
            File childDirectory = new File(directory, childDirName);
            if ( ! childDirectory.exists()) {
                childDirectory.mkdirs();
            }

            if (useDataSetRef) {
                insertRef(element, dataset.getDataSet(childDataSets[i]), childDirectory);
            } else {
                insert(element, dataset.getDataSet(childDataSets[i]), childDirectory, fileType);
            }
        }

        String[] childDSS = dataset.getDataSetSeismogramNames();
        File dataDir = new File(directory, "data");
        dataDir.mkdirs();
        for (int i = 0; i < childDSS.length; i++) {
            DataSetSeismogram dss = dataset.getDataSetSeismogram(childDSS[i]);
            URLDataSetSeismogram urlDSS;
            if (saveLocally || ! (dss instanceof URLDataSetSeismogram)) {
                urlDSS = URLDataSetSeismogram.localize(dss, dataDir, fileType);
            } else {
                urlDSS = (URLDataSetSeismogram)dss;
            }
            insert(element, urlDSS, directory.toURI().toURL());
        }

        String[] paramNames = dataset.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            insert(element, paramNames[i], dataset.getParameter(paramNames[i]));
        }

    }

    /** inserts a URLDataSetSeismogram element into the parent. The URLDataSetSeismogram
     *  Element is returned. URLs are made relative to the given base.
     */
    public Element insert(Element parent, URLDataSetSeismogram urlDSS, URL base) {
        Element child = parent.getOwnerDocument().createElement("urlDataSetSeismogram");
        urlDSS.insertInto(child, base);
        parent.appendChild(child);
        return child;
    }

    /**
     * DOM insertRef
     * inserts the child dataset as a datasetRef element. The URL is assumed
     *  to be in a subdirectory relative to the current dataset.
     */
    public Element insertRef(Element element, DataSet dataset, File directory)
        throws IOException, ParserConfigurationException, MalformedURLException {
        File dsFile = save(dataset, directory);
        return insertRef(element, directory.getName()+"/"+dsFile.getName(), dataset.getName());
    }

    /**
     * DOM insertRef
     * inserts the child dataset as a datasetRef element. The URL is assumed
     *  to be in a subdirectory relative to the current dataset.
     */
    public Element insertRef(Element parent, String datasetURL, String linkTitle)
        throws IOException, ParserConfigurationException, MalformedURLException {
        Element element = parent.getOwnerDocument().createElement("datasetRef");
        element.setAttribute("xlink:href", datasetURL);
        element.setAttribute("xlink:type", "simple");
        element.setAttribute("xlink:title", linkTitle);
        parent.appendChild(element);
        return element;
    }

    /**
     * DOM insert
     * inserts the parameter into the given element.
     */
    public Element insert(Element parent, String name, Object parameter) {
        Element element =
            parent.getOwnerDocument().createElement("parameter");
        parent.appendChild(element);
        XMLParameter.insert(element,
                            name,
                            parameter);
        return element;
    }

    /**
     * DOM insertParameter
     * inserts the parameter into the given element.
     */
    public Element insertParameter(Element parent, String name, String typeDef, String typeName, String value) {
        Element element =
            parent.getOwnerDocument().createElement("parameter");
        parent.appendChild(element);
        XMLParameter.insert(element,
                            name,
                            typeDef,
                            typeName,
                            value);
        return element;
    }

    /**
     * Load a xml dataset from a URL.
     *
     * @param datasetURL an <code>URL</code> to an xml dataset
     * @return a <code>XMLDataSet</code> populated form the URL
     */
    public static DataSet load(URL datasetURL)
        throws IOException, ParserConfigurationException, IncomprehensibleDSMLException, UnsupportedFileTypeException {
        DataSet dataset = null;

        DocumentBuilder docBuilder = getDocumentBuilder();

        try {
            Document doc = docBuilder.parse(new BufferedInputStream(datasetURL.openStream()));
            Element docElement = doc.getDocumentElement();

            if (docElement.getTagName().equals("dataset")) {
                if (docElement.getAttribute("xsi:schemaLocation").equals(DSML_SCHEMA2_0)) {
                    DataSetToXML dataSetToXML = new DataSetToXML();
                    dataset = dataSetToXML.extract(datasetURL, docElement);
                } else {
                    logger.warn("Not a 2.0 dsml. "+docElement.getTagName()+"  "+docElement.getAttribute("xsi:schemaLocation"));
                    dataset = new XMLDataSet(docBuilder, datasetURL, docElement);
                    AuditInfo[] audit = new AuditInfo[1];
                    audit[0] = new AuditInfo("loaded from "+datasetURL.toString(),
                                             System.getProperty("user.name"));
                    dataset.addParameter("xml:base", datasetURL.toString(), audit);
                }
                return dataset;
            } else {
                throw new IncomprehensibleDSMLException("This does not appear to be a dsml file, starting tag is not dataset. "+datasetURL.toString());
            }
        } catch (SAXException e) {
            throw new IncomprehensibleDSMLException("This does not appear to be a dsml file."+datasetURL.toString(), e);
        }
    }

    public static final String DSML_SCHEMA2_0 = "http://www.seis.sc.edu/xschema/dataset/2.0 http://www.seis.sc.edu/xschema/dataset/2.0/dataset.xsd";

    /** Extracts the dataset from the element, which is assumed to be a
     &lt;dataset&gt; element. */
    public DataSet extract(URL base, Element element) throws MalformedURLException, UnsupportedFileTypeException{
        String name = "";
        String owner = "";
        String id = element.getAttribute("datasetid");
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element)children.item(i);
                if (child.getNodeName().equals("name")) {
                    name = XMLUtil.getText(child);
                } else if (child.getNodeName().equals("owner")) {
                    owner = XMLUtil.getText(child);
                }
            }
        }

        // all 3 should be populated now
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo("loaded from "+base.toString(),
                                 System.getProperty("user.name"));
        if (id == null || id.length() == 0) {
            id = "autogen_id-"+Math.random();
        }
        MemoryDataSet dataset = new MemoryDataSet(id, name, owner, audit);
        dataset.addParameter("xml:base", base.toString(), audit);
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("dataset")) {
                dataset.addDataSet(extract(base, (Element)child), audit);
            } else if (child.getNodeName().equals("datasetRef")) {
                Element childElement = (Element)child;
                dataset.addDataSet(new URLDataSet(childElement.getAttribute("xlink:title"),
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


