/**
 * DataSetToXMLStAX.java
 * 
 * @author whoever wrote the original DataSetToXML
 * @author Philip Oliver-Paull (StAXified)
 * 
 * note on method names: "insert" implies that the method inserts attributes or
 * tags that are part of a preexisting tag. "write" implies that the enclosing
 * tag is included in the writing.
 */
package edu.sc.seis.fissuresUtil.xml;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSetToXMLStAX {

    /**
     * Saves the given dataset to an xml file in the given directory. The file
     * is returned.
     */
    public File save(DataSet dataset, File saveDirectory) throws IOException,
            XMLStreamException {
        String filename = createFileName(dataset);
        logger.debug("save to " + filename + " in " + saveDirectory.toString());
        saveDirectory.mkdirs();
        File outFile = new File(saveDirectory, filename);
        createFile(dataset, saveDirectory, outFile);
        logger.debug("Done with save to " + saveDirectory.toString());
        return outFile;
    }

    public static String createFileName(DataSet dataset) {
        String filename = dataset.getName() + ".dsml";
        filename = filename.replaceAll(" ", "_");
        filename = filename.replaceAll(",", "_");
        filename = filename.replaceAll("/", "_");
        filename = filename.replaceAll(":", "_");
        // filename = filename.replaceAll("\\","_");
        return filename;
    }

    /**
     * creates an XML file from a Dataset. Remember that you'll only want to use
     * this if you don't plan on playing around with the innards very much...
     * unless you want to do such things, which is pretty nasty, if I say so
     * myself.
     */
    public void createFile(DataSet dataset, File dataDirectory, File outFile)
            throws IOException, XMLStreamException {
        StAXFileWriter staxWriter = new StAXFileWriter(outFile);
        staxWriter.getStreamWriter().writeStartDocument();
        writeDataSetWithNSInfo(staxWriter.getStreamWriter(),
                               dataset,
                               dataDirectory);
        staxWriter.close();
    }

    /**
     * inserts the dataset, and all child datasets recursively, into the
     * document, along with dataset seismograms and parameters if they can be
     * stored. Note that all dataSetSeismograms are converted to
     * URLDataSetSeismograms and stored in a directory structure that mirrors
     * the dataset structure under the given directory.
     */
    public void writeDataSet(XMLStreamWriter writer,
                             DataSet dataset,
                             File directory) throws XMLStreamException,
            IOException {
        writer.writeStartElement("dataset");
        insertDSInfo(writer, dataset, directory);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * writes the dataset with a namespace-attributed start element and then
     * does the same thing as the above method.
     */
    public void writeDataSetWithNSInfo(XMLStreamWriter writer,
                                       DataSet dataset,
                                       File directory)
            throws XMLStreamException, IOException {
        writeDataSetStartElement(writer);
        insertDSInfo(writer, dataset, directory);
        writer.writeEndElement();
    }

    public void writeDataSetStartElement(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeStartElement("dataset");
        writer.writeAttribute("xmlns:dataset",
                              "http://www.seis.sc.edu/xschema/dataset/2.0");
        writer.writeAttribute("xmlns",
                              "http://www.seis.sc.edu/xschema/dataset/2.0");
        writer.writeAttribute("xsi:schemaLocation",
                              "http://www.seis.sc.edu/xschema/dataset/2.0 http://www.seis.sc.edu/xschema/dataset/2.0/dataset.xsd");
        writer.writeAttribute("xmlns:xsi",
                              "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
    }

    /**
     * inserts the pertinent data into a dataSetElement. This should be called
     * directly after writeDataSetStartElement.
     */
    public void insertDSInfo(XMLStreamWriter writer,
                             DataSet dataset,
                             File directory) throws XMLStreamException,
            IOException {
        writer.writeAttribute("datasetid", dataset.getId());
        XMLUtil.writeTextElement(writer, "name", dataset.getName());
        XMLUtil.writeTextElement(writer, "owner", dataset.getOwner());
        String[] childDataSets = dataset.getDataSetNames();
        for(int i = 0; i < childDataSets.length; i++) {
            String childDirName = createFileName(dataset.getDataSet(childDataSets[i]));
            File childDirectory = new File(directory, childDirName);
            if(!childDirectory.exists()) {
                childDirectory.mkdirs();
            }
            if(useDataSetRef) {
                writeRef(writer,
                         dataset.getDataSet(childDataSets[i]),
                         childDirectory);
            } else {
                writeDataSet(writer,
                             dataset.getDataSet(childDataSets[i]),
                             childDirectory);
            }
        }
    }

    public void writeURLDataSetSeismogram(XMLStreamWriter writer,
                                          URLDataSetSeismogram urlDSS,
                                          URL base) throws XMLStreamException {
        writer.writeStartElement("urlDataSetSeismogram");
        urlDSS.insertInto(writer, base);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * inserts the child dataset as a datasetRef element. The URL is assumed to
     * be in a subdirectory relative to the current dataset.
     */
    public void writeRef(XMLStreamWriter writer, DataSet dataset, File directory)
            throws XMLStreamException, IOException {
        File dsFile = save(dataset, directory);
        writeRef(writer,
                 directory.getName() + "/" + dsFile.getName(),
                 dataset.getName());
    }

    /**
     * inserts the child dataset as a datasetRef element. The URL is assumed to
     * be in a subdirectory relative to the current dataset.
     */
    public void writeRef(XMLStreamWriter writer,
                         String datasetURL,
                         String linkTitle) throws XMLStreamException {
        writer.writeStartElement("datasetRef");
        writer.writeAttribute("xlink:href", datasetURL);
        writer.writeAttribute("xlink:type", "simple");
        writer.writeAttribute("xlink:title", linkTitle);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * inserts the parameter into the given element.
     */
    public void writeParameter(XMLStreamWriter writer,
                               String name,
                               Object parameter) throws XMLStreamException {
        writer.writeStartElement("parameter");
        XMLParameter.insert(writer, name, parameter);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    /**
     * inserts the parameter into the given element.
     */
    public void writeParameter(XMLStreamWriter writer,
                               String name,
                               String typeDef,
                               String typeName,
                               String value) throws XMLStreamException {
        writer.writeStartElement("parameter");
        XMLParameter.insert(writer, name, typeDef, typeName, value);
        XMLUtil.writeEndElementWithNewLine(writer);
    }

    protected boolean saveLocally = true;

    /**
     * If true, then each dataset is put into a separate dsml file. Otherwise
     * the child datasets are embedded in the parent dsml file.
     */
    protected boolean useDataSetRef = true;

    static Logger logger = LoggerFactory.getLogger(DataSetToXMLStAX.class);
}
