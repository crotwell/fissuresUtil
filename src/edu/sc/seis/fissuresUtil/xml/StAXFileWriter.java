/**
 * StAXFileWriter.java
 *
 * @author Philip Oliver-Paull
 *
 * Convenience class for StAX stream writing
 */

package edu.sc.seis.fissuresUtil.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class StAXFileWriter {

    public StAXFileWriter(File file) throws IOException, XMLStreamException{
        outFile = file;
        if (outFile.exists()){
            tempFile = File.createTempFile("Temp_" + outFile.getName(), "xml", outFile.getParentFile());
            isTempFiled = true;
        }
        else {
            tempFile = outFile;
        }
        fileWriter = new FileWriter(tempFile);
        xmlWriter = XMLUtil.staxOutputFactory.createXMLStreamWriter(fileWriter);
    }

    public XMLStreamWriter getStreamWriter(){
        return xmlWriter;
    }

    public void close() throws XMLStreamException, IOException{
        if (!isClosed){
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
            fileWriter.flush();
            xmlWriter.close();
            fileWriter.close();
            if (isTempFiled){
                if ( ! tempFile.renameTo(outFile)) {
                    //If unable to rename the tempfile, delete it and try again
                    if(outFile.delete()){
                        tempFile.renameTo(outFile);
                    }else{
                        throw new IOException("Unable to move temp file over old file");
                    }
                }
            }
            isClosed = true;
        }
    }

    private File outFile, tempFile;
    private boolean isTempFiled = false;
    private boolean isClosed = false;
    private FileWriter fileWriter;
    private XMLStreamWriter xmlWriter;
}

