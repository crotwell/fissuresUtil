

package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;
import org.apache.log4j.*;

/**
 * Access to a dataset stored as an XML file.
 *
 * @version $Id: XMLDataSetAccess.java 1682 2002-05-24 01:48:06Z crotwell $
 */
public class XMLDataSetAccess implements DataSetAccess, Serializable {

    /** Creates an empty dataset. */
    public XMLDataSetAccess(DocumentBuilder docBuilder, 
			    String id, 
			    String name,
			    String Owner) {
	Document doc = docBuilder.newDocument();
	config = doc.createElement("dataset");
	Element nameE = doc.createElement("name");
	Element ownerE = doc.createElement("owner");
	config.setAttribute("datasetid", id);
	config.appendChild(nameE);
	config.appendChild(ownerE);
    }

    public XMLDataSetAccess(DocumentBuilder docBuilder, Element config) {
	this.docBuilder = docBuilder;
	this.config = config;
    }

    public String getId() {
	return evalString(config, "@datasetid");
    }

    public String getName() {
	return evalString(config, "name/text()");
    }

    public String[] getParameterRefNames() {
	return getAllAsStrings("parameterRef/text()");
    }

    public Element getParamter(String name) {
	NodeList nList = evalNodeList(config, 
				      "dataset/parameter[name/text()="+
				      dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		return (Element)n;
	    }
	}

	// not a parameter, try parameterRef
	nList = evalNodeList(config, 
			     "dataset/parameterRef[text()="+dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n);
		try {
		    return sl.retrieve();
		} catch (Exception e) {
		    logger.error("can't get paramterRef", e);
		} // end of try-catch
	    }
	}

	//can't find that name???
	return null;
    }

    public String[] getDataSetIds() {
	return getAllAsStrings("*/@datasetid");
    }

    public DataSetAccess getDataSet(String id) {
	NodeList nList = 
	    evalNodeList(config, "//dataset[@datasetid="+dquote+id+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		return new XMLDataSetAccess(docBuilder, (Element)n);
	    }
	}

	// not an embedded dataset, try datasetRef
	nList = 
	    evalNodeList(config, "datasetRef[@datasetid="+dquote+id+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		try {
		    SimpleXLink sl = new SimpleXLink(docBuilder, (Element)n);
		    return new XMLDataSetAccess(docBuilder, sl.retrieve());
		} catch (Exception e) {
		    logger.error("Couldn't get datasetRef", e);
		} // end of try-catch
		
	    }
	}

	// can't find it
	return null;
    }

    public String[] getSeismogramNames() {
	return getAllAsStrings("SacSeismogram/name/text()");
    }

    public LocalSeismogramImpl getSeismogram(String name) {
	NodeList nList = 
	    evalNodeList(config, "SacSeismogram[name="+dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    try {
		Node n = nList.item(0); 
		if (n instanceof Element) {
		    Element e = (Element)n;
		    URL sacURL = 
			new URL(e.getAttribute("xlink:href"));
		    DataInputStream dis = new DataInputStream(new BufferedInputStream(sacURL.openStream())); 
		    SacTimeSeries sac = new SacTimeSeries();
		    sac.read(dis);
		    LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
		    
		    NodeList propList = evalNodeList(e, "property");
		    if (propList != null && propList.getLength() != 0) {
			Property[] props = seis.getProperties();
			Property[] newProps = 
			    new Property[1+props.length+nList.getLength()];
			System.arraycopy(props, 0, newProps, 0, props.length);
			for (int i=0; i<propList.getLength(); i++) {
			    Element propElement = (Element)propList.item(i);
			    newProps[props.length+i] = 
				new Property(xpath.eval(propElement, "name/text()").str(),
					     xpath.eval(propElement, "value/text()").str());
			} // end of for
			newProps[newProps.length-1] = new Property("name",
								   name);
			seis.setProperties(newProps);
		    }
		    return seis;
		}
		
	    } catch (Exception e) {
		logger.error("Couldn't get seismogram", e);
	    } // end of try-catch
	    
	}
	return null;
    }

    protected String[] getAllAsStrings(String path) {
	NodeList nodes = evalNodeList(config, path);
	String[] out = new String[nodes.getLength()];
	for (int i=0; i<out.length; i++) {
	    out[i] = nodes.item(i).getNodeValue();
	} // end of for (int i=0; i++; i<out.length)
	return out;
    }

    protected NodeList evalNodeList(Node context, String path) {
	try {
	    XObject xobj = xpath.eval(context, path);
	    if (xobj != null && xobj.getType() == XObject.CLASS_NODESET) {
		return xobj.nodelist();
	    }
 	} catch (javax.xml.transform.TransformerException e) {
	    logger.error("Couldn't get NodeList", e);
 	} // end of try-catch
	return null;
    }

    protected String evalString(Node context, String path) {
	try {
	    return xpath.eval(config, "@datasetid").str();
 	} catch (javax.xml.transform.TransformerException e) {
	    logger.error("Couldn't get String", e);
 	} // end of try-catch
	return null;
    }

    private CachedXPathAPI xpath = new CachedXPathAPI();

    protected Element config;

    protected DocumentBuilder docBuilder;

    private static final String dquote = ""+'"';

    static Category logger = 
	Category.getInstance(XMLDataSetAccess.class.getName());

    static void testDataSet(DataSetAccess dataset, String indent) {
	indent = indent+"  ";
	String[] names = dataset.getSeismogramNames();
	System.out.println(indent+" has "+names.length+" seismograms.");
	for (int num=0; num<names.length; num++) {
	    System.out.println(indent+" Seismogram name="+names[num]);
	    LocalSeismogramImpl seis = dataset.getSeismogram(names[num]);
	    System.out.println(seis.getNumPoints());

	}
	names = dataset.getDataSetIds();
	System.out.println(indent+" has "+names.length+" datasets.");
	for (int num=0; num<names.length; num++) {
	    System.out.println(indent+" Dataset name="+names[num]);
	    testDataSet(dataset.getDataSet(names[num]), indent);
	}
    }

    public static void main (String[] args) {
	try {
	    BasicConfigurator.configure();

	    System.out.println("Starting..");
	    DocumentBuilderFactory factory
		= DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();


	    // just for testing
	    Document doc = docBuilder.parse(args[0]);
	    Element docElement = doc.getDocumentElement();
	    NodeList nList = docElement.getChildNodes();
	    XMLDataSetAccess dataset = null;

	    if (docElement.getTagName().equals("dataset")) {
		System.out.println("dataset yes");
		dataset = new XMLDataSet(docBuilder, docElement);
		System.out.println(docElement.getTagName()+" {"+docElement.getAttribute("datasetid")+"}");
		testDataSet(dataset, " ");
	    }


	} catch (Exception e) {
	    e.printStackTrace();	    
	} // end of try-catch
    } // end of main ()
    
}
