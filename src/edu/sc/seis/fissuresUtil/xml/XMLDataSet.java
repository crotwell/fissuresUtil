

package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.sac.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import org.apache.log4j.*;

/**
 * Access to a dataset stored as an XML file.
 *
 * @version $Id: XMLDataSet.java 1711 2002-05-28 13:33:10Z crotwell $
 */
public class XMLDataSet implements DataSet, Serializable {

    public static XMLDataSet load(InputStream inStream) {
	XMLDataSet dataset = null;
	try {
	    DocumentBuilderFactory factory
		= DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = factory.newDocumentBuilder();
	    
	    Document doc = docBuilder.parse(inStream);
	    Element docElement = doc.getDocumentElement();

	    if (docElement.getTagName().equals("dataset")) {
		System.out.println("dataset yes");
		dataset = new XMLDataSet(docBuilder, docElement);
		System.out.println(docElement.getTagName()+" {"+docElement.getAttribute("datasetid")+"}");
	    }
	} catch (java.io.IOException e) {
	    logger.error("Error loading XMLDataSet",e);
	} catch (org.xml.sax.SAXException e) {
	    logger.error("Error loading XMLDataSet",e);
	} catch (javax.xml.parsers.ParserConfigurationException e) {
	    logger.error("Error loading XMLDataSet",e);
	} // end of try-catch
	return dataset;
	
    }

    /** Creates an empty dataset. */
    public XMLDataSet(DocumentBuilder docBuilder, 
		      String id, 
		      String name,
		      String owner) {
	this.docBuilder = docBuilder;
	Document doc = docBuilder.newDocument();
	config = doc.createElement("dataset");
	Element nameE = doc.createElement("name");
	nameE.setNodeValue(name);
	Element ownerE = doc.createElement("owner");
	ownerE.setNodeValue(owner);
	config.setAttribute("datasetid", id);
	config.appendChild(nameE);
	config.appendChild(ownerE);
    }

    public XMLDataSet(DocumentBuilder docBuilder, Element config) {
	this.docBuilder = docBuilder;
	this.config = config;
    }

    public String getId() {
	return evalString(config, "@datasetid");
    }

    public String getName() {
	return evalString(config, "name/text()");
    }

    public void setName(String name) {
	Element nameElement = evalElement(config, "name");
	nameElement.setNodeValue(name);
    }

    public String[] getParameterNames() {
	String[] params = getAllAsStrings("parameter/name/text()");
	String[] paramRefs = getAllAsStrings("parameterRef/text()");
	String[] all = new String[params.length+paramRefs.length];
	System.arraycopy(params, 0, all, 0, params.length);
	System.arraycopy(paramRefs, 0, all, params.length, paramRefs.length);
	return all;
    }

    public Object getParamter(String name) {
	if (parameterCache.containsKey(name)) {
	    return parameterCache.get(name);
	} // end of if (parameterCache.containsKey(name))
	
	NodeList nList = evalNodeList(config, 
				      "dataset/parameter[name/text()="+
				      dquote+name+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		parameterCache.put(name, n);
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
		    Element e = sl.retrieve();
		    parameterCache.put(name, e);
		    return e; 
		} catch (Exception e) {
		    logger.error("can't get paramterRef", e);
		} // end of try-catch
	    }
	}

	//can't find that name???
	return null;
    }

    public void addParameter(String name, 
			     Object param,
			     AuditInfo[] audit) {
	parameterCache.put(name, param);
	if (param instanceof Element) {
	    config.appendChild((Element)param);
	} else {
	    logger.warn("Parameter is only stored in memory.");
	} // end of else
	
    }

    public String[] getDataSetIds() {
	return getAllAsStrings("*/@datasetid");
    }

    public String[] getDataSetNames() {
	String[] ids = getDataSetIds();
	String[] names = new String[ids.length];
	for (int i=0; i<ids.length; i++) {
	    DataSet ds = getDataSetById(ids[i]);
	    names[i] = ds.getName();
	} // end of for (int i=0; i<ids.length; i++)
	return names;
    }

    public DataSet getDataSet(String name) {
	String[] ids = getDataSetIds();
	for (int i=0; i<ids.length; i++) {
	    DataSet ds = getDataSetById(ids[i]);
	    if (name == ds.getName()) {
		return ds;
	    }
	} // end of for (int i=0; i<ids.length; i++)
	return null;
    }

    public void addDataSet(DataSet dataset,
			   AuditInfo[] audit) {
	if (dataset instanceof XMLDataSet) {
	    config.appendChild(((XMLDataSet)dataset).config);
	} // end of if (dataset instanceof XMLDataSet)
    }

    public DataSet createChildDataSet(String id, String name, String owner,
				      AuditInfo[] audit) {
	XMLDataSet dataset = new XMLDataSet(docBuilder, id, name, owner);
	addDataSet(dataset, audit);
	return dataset;
    }

    public DataSet getDataSetById(String id) {
	if (dataSetCache.containsKey(id)) {
	    return (DataSet)dataSetCache.get(id);
	}

	NodeList nList = 
	    evalNodeList(config, "//dataset[@datasetid="+dquote+id+dquote+"]");
	if (nList != null && nList.getLength() != 0) {
	    Node n = nList.item(0); 
	    if (n instanceof Element) {
		XMLDataSet dataset = new XMLDataSet(docBuilder, (Element)n);
		dataSetCache.put(id, dataset);
		return dataset;
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
		    return new XMLDataSet(docBuilder, sl.retrieve());
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
	if (seismogramCache.containsKey(name)) {
	    return (LocalSeismogramImpl)seismogramCache.get(name);
	} // end of if (seismogramCache.containsKey(name))
	
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
		    seismogramCache.put(name, seis);
		    return seis;
		}
		
	    } catch (Exception e) {
		logger.error("Couldn't get seismogram", e);
	    } // end of try-catch
	    
	}
	return null;
    }

    public void addSeismogram(LocalSeismogramImpl seis,
			      AuditInfo[] audit) {

	// Note this does not set the xlink, as the seis has not been saved anywhere yet.

	Document doc = config.getOwnerDocument();
	Element sac = doc.createElement("SacSeismogram");

	String name =seis.getProperty("name");
	if (name != null && name.length() != 0) {
	    Element nameE = doc.createElement("name");
	    nameE.setNodeValue(seis.getProperty("name"));
	    sac.appendChild(nameE);
	}
	
	Property[] props = seis.getProperties();
	Element propE, propNameE, propValueE;
	for (int i=0; i<props.length; i++) {
	    if (props[i].name != "name") {
		propE = doc.createElement("property");
		propNameE = doc.createElement("name");
		propNameE.setNodeValue(props[i].name);
		propValueE = doc.createElement("value");
		propValueE.setNodeValue(props[i].value);
		propE.appendChild(propNameE);
		propE.appendChild(propValueE);
		sac.appendChild(propE);
	    }
	}
	config.appendChild(sac);

	seismogramCache.put(seis.getProperty("name"), seis);
    }

    public void addSeismogramRef(URL seisURL, 
				 String name, 
				 Property[] props, 
				 ParameterRef[] parm_ids,
				 AuditInfo[] audit) {

	// Note this does not set the xlink, as the seis has not been saved anywhere yet.

	Document doc = config.getOwnerDocument();
	Element sac = doc.createElement("SacSeismogram");
	Element nameE = doc.createElement("name");
	nameE.setNodeValue(name);
	sac.appendChild(nameE);

	Element propE, propNameE, propValueE;
	for (int i=0; i<props.length; i++) {
	    if (props[i].name != "name") {
		propE = doc.createElement("property");
		propNameE = doc.createElement("name");
		propNameE.setNodeValue(props[i].name);
		propValueE = doc.createElement("value");
		propValueE.setNodeValue(props[i].value);
		propE.appendChild(propNameE);
		propE.appendChild(propValueE);
		sac.appendChild(propE);
	    }
	}
	config.appendChild(sac);
    }

    public void write(OutputStream out) throws Exception {
	DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
	org.w3c.dom.Document outNode = docBuilder.newDocument();

	javax.xml.transform.TransformerFactory tfactory = 
	    javax.xml.transform.TransformerFactory.newInstance(); 
    
	// This creates a transformer that does a simple identity transform, 
	// and thus can be used for all intents and purposes as a serializer.
	javax.xml.transform.Transformer serializer = tfactory.newTransformer();
    
	java.util.Properties oprops = new java.util.Properties();
	oprops.put("method", "xml");
	oprops.put("indent-amount", "2");
	serializer.setOutputProperties(oprops);
	serializer.transform(new javax.xml.transform.dom.DOMSource(config), 
			     new javax.xml.transform.stream.StreamResult(out));

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

    protected Element evalElement(Node context, String path) {
	NodeList nList = evalNodeList(context, path);
	if (nList != null && nList.getLength() != 0) {
	    return (Element)nList.item(0);
	}
	logger.error("Couldn't get NodeList "+path);
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

    protected HashMap dataSetCache = new HashMap();

    protected HashMap seismogramCache = new HashMap();

    protected HashMap parameterCache = new HashMap();

    private static final String dquote = ""+'"';

    static Category logger = 
	Category.getInstance(XMLDataSet.class.getName());

    static void testDataSet(DataSet dataset, String indent) {
	indent = indent+"  ";
	String[] names = dataset.getSeismogramNames();
	System.out.println(indent+" has "+names.length+" seismograms.");
	for (int num=0; num<names.length; num++) {
	    System.out.println(indent+" Seismogram name="+names[num]);
	    LocalSeismogramImpl seis = dataset.getSeismogram(names[num]);
	    System.out.println(seis.getNumPoints());

	}
	names = dataset.getDataSetNames();
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
	    XMLDataSet dataset = load(new BufferedInputStream(new FileInputStream(args[0])));
	    String[] names = dataset.getDataSetNames();
	    for (int i=0; i<names.length; i++) {
		FileOutputStream out = new FileOutputStream("test_"+names[i]);
		XMLDataSet sub = (XMLDataSet)dataset.getDataSet(names[i]);
		sub.write(out);
		out.flush();
		out.close();
	    } // end of for (int i=0; i<names.length; i++)
	    

	    // testDataSet(dataset, " ");

	} catch (Exception e) {
	    e.printStackTrace();	    
	} // end of try-catch
    } // end of main ()
    
}
