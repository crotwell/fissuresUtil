package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;

public class XMLDataSet extends XMLDataSetAccess implements DataSet, Serializable {

    public XMLDataSet(DocumentBuilder doc, Element config) {
	super(doc, config);
    }

}
