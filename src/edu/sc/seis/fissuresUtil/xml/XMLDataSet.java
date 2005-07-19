package edu.sc.seis.fissuresUtil.xml;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Category;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfSeismogramDC.SeismogramAttr;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;

/**
 * Access to a dataset stored as an XML file.
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version $Id: XMLDataSet.java 14283 2005-07-19 17:08:02Z crotwell $
 */
/**
 * Describe class <code>XMLDataSet</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class XMLDataSet implements DataSet, Serializable {

    /**
     * Creates a new <code>XMLDataSet</code> instance.
     * 
     * @param docBuilder
     *            a <code>DocumentBuilder</code> value
     * @param datasetURL
     *            an <code>URL</code> to a dsml file
     */
    public XMLDataSet(DocumentBuilder docBuilder, URL datasetURL) {
        this.base = datasetURL;
        this.docBuilder = docBuilder;
        Document doc = docBuilder.newDocument();
        config = doc.createElement("dataset");
        config.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        prefixResolver = new org.apache.xml.utils.PrefixResolverDefault(config);
    }

    /**
     * Load a xml dataset from a URL.
     * 
     * @param datasetURL
     *            an <code>URL</code> to an xml dataset
     * @return a <code>XMLDataSet</code> populated form the URL
     */
    public static XMLDataSet load(URL datasetURL) {
        XMLDataSet dataset = null;
        try {
            DocumentBuilder docBuilder = getDocumentBuilder();
            Document doc = docBuilder.parse(new BufferedInputStream(datasetURL.openStream()));
            Element docElement = doc.getDocumentElement();
            if(docElement.getTagName().equals("dataset")) {
                dataset = new XMLDataSet(docBuilder, datasetURL, docElement);
            }
        } catch(java.io.IOException e) {
            logger.error("Error loading XMLDataSet", e);
        } catch(org.xml.sax.SAXException e) {
            logger.error("Error loading XMLDataSet", e);
        } catch(javax.xml.parsers.ParserConfigurationException e) {
            logger.error("Error loading XMLDataSet", e);
        } // end of try-catch
        return dataset;
    }

    public XMLDataSet(DocumentBuilder docBuilder, URL base, String id,
            String name, String owner) {
        this(docBuilder, base);
        Document doc = config.getOwnerDocument();
        Element nameE = doc.createElement("name");
        Text text = doc.createTextNode(name);
        nameE.appendChild(text);
        Element ownerE = doc.createElement("owner");
        text = doc.createTextNode(owner);
        ownerE.appendChild(text);
        config.setAttribute("datasetid", id);
        config.appendChild(nameE);
        config.appendChild(ownerE);
    }

    /**
     * Creates a new <code>XMLDataSet</code> instance.
     * 
     * @param docBuilder
     *            a <code>DocumentBuilder</code> to use to create the
     *            document.
     * @param base
     *            the <code>URL</code> other urls should be made relative to.
     * @param config
     *            the dataset contents as a DOM <code>Element</code>
     */
    public XMLDataSet(DocumentBuilder docBuilder, URL base, Element config) {
        this(docBuilder, base);
        this.config = config;
        checkForLegacySeismograms();
    }

    protected void checkForLegacySeismograms() {
        //this supports loading of classic seismogram datasets that were
        //created before dataset seismograms
        if(getDataSetSeismogramNames().length == 0) {
            String[] names = getSeismogramNames();
            logger.info("No DataSetSeismograms in dataset "
                    + getName()
                    + ", using legacy dataset option: creating DataSetSeismogram per LocalSeismogram. LS length="
                    + names.length);
            for(int i = 0; i < names.length; i++) {
                URL sacURL = getSeismogramURL(names[i]);
                if(sacURL != null) {
                    DataSetSeismogram dsstemp = new URLDataSetSeismogram(sacURL,
                                                                         SeismogramFileTypes.SAC,
                                                                         this,
                                                                         names[i]);
                    addDataSetSeismogram(dsstemp, new AuditInfo[0]);
                }
            }
        }
    }

    /**
     * Gets the dataset Id. This should be unique.
     * 
     * @return a <code>String</code> id
     */
    public String getId() {
        //logger.debug("In the method getId");
        return XMLUtil.evalString(config, "@datasetid");
    }

    /**
     * Gets the base URL that other URLs in this dataset are relative to.
     * 
     * @return the base <code>URL</code>
     */
    /**
     * Describe <code>getBase</code> method here.
     * 
     * @return an <code>URL</code> value
     */
    public URL getBase() {
        return base;
    }

    /**
     * Sets the base URL that other URLs in this dataset are relative to.
     * 
     * @param base
     *            an <code>URL</code>
     */
    public void setBase(URL base) {
        this.base = base;
    }

    /**
     * Gets the displayable name.
     * 
     * @return a <code>String</code> name
     */
    public String getName() {
        return XMLUtil.evalString(config, "name/text()");
    }

    /**
     * Sets the displayable name.
     * 
     * @param name
     *            a <code>String</code> name
     */
    public void setName(String name) {
        Element nameElement = XMLUtil.evalElement(config, "name");
        Text text = config.getOwnerDocument().createTextNode(name);
        nameElement.appendChild(text);
        config.appendChild(nameElement);
    }

    /**
     * Gets the displayable name.
     * 
     * @return a <code>String</code> name
     */
    public String getOwner() {
        return XMLUtil.evalString(config, "owner/text()");
    }

    /**
     * Sets the displayable name.
     * 
     * @param name
     *            a <code>String</code> name
     */
    public void setOwner(String owner) {
        Element nameElement = XMLUtil.evalElement(config, "owner");
        Text text = config.getOwnerDocument().createTextNode(owner);
        nameElement.appendChild(text);
        config.appendChild(nameElement);
    }

    /**
     * Gets the names of all parameters within this dataset.
     * 
     * @return an array of names.
     */
    public String[] getParameterNames() {
        if(parameterNameCache == null) {
            parameterNameCache = cacheParameterNames();
        } // end of if (parameterNameCache == null)
        return parameterNameCache;
    }

    /**
     * Describe <code>cacheParameterNames</code> method here.
     * 
     * @return a <code>String[]</code> value
     */
    public String[] cacheParameterNames() {
        String[] params = XMLUtil.getAllAsStrings(this.config,
                                                  "parameter/name/text()");
        ArrayList referenceNames = new ArrayList();
        NodeList paramRefsList = XMLUtil.evalNodeList(config, "parameterRef");
        if(paramRefsList != null && paramRefsList.getLength() != 0) {
            for(int counter = 0; counter < paramRefsList.getLength(); counter++) {
                Node n = paramRefsList.item(counter);
                if(n instanceof Element) {
                    String str = ((Element)n).getAttribute("name");
                    referenceNames.add(str);
                }
            }
        }
        String[] paramRefs = new String[referenceNames.size()];
        paramRefs = (String[])referenceNames.toArray(paramRefs);
        String[] all = new String[params.length + paramRefs.length];
        System.arraycopy(params, 0, all, 0, params.length);
        System.arraycopy(paramRefs, 0, all, params.length, paramRefs.length);
        return all;
    }

    /**
     * Gets the parameter with the given name. The returns null if the parameter
     * cannot be found.
     * 
     * @param name
     *            a <code>String</code> paramter name
     * @return the parameter with that name
     */
    public Object getParameter(String name) {
        if(parameterCache.containsKey(name)) {
            Object obj = parameterCache.get(name);
            if(obj instanceof SoftReference) {
                SoftReference softReference = (SoftReference)obj;
                if(softReference.get() != null) return softReference.get();
                else parameterCache.remove(name);
            } else return obj;
        } // end of if (parameterCache.containsKey(name))
        NodeList nList = XMLUtil.evalNodeList(config, "parameter[name/text()="
                + dquote + name + dquote + "]");
        if(nList != null && nList.getLength() != 0) {
            //logger.debug("getting the parameter "+name);
            Node n = nList.item(0);
            if(n instanceof Element) {
                Object r = XMLParameter.getParameter((Element)n);
                parameterCache.put(name, new SoftReference(r));
                return r;
            }
        } else {
            logger.debug("THE NODE LIST IS NULL for parameter " + name);
        }
        // not a parameter, try parameterRef
        nList = XMLUtil.evalNodeList(config, "parameterRef");//[text()="+dquote+name+dquote+"]");
        if(nList != null && nList.getLength() != 0) {
            for(int counter = 0; counter < nList.getLength(); counter++) {
                Node n = nList.item(counter);
                if(n instanceof Element) {
                    if(!((Element)n).getAttribute("name").equals(name)) continue;
                    SimpleXLink sl = new SimpleXLink(docBuilder,
                                                     (Element)n,
                                                     getBase());
                    try {
                        Element e = sl.retrieve();
                        //parameterCache.put(name, e);
                        Object obj = XMLParameter.getParameter(e);
                        parameterCache.put(name, new SoftReference(obj));
                        return obj;
                    } catch(Exception e) {
                        logger.error("can't get paramterRef for " + name, e);
                    } // end of try-catch
                }
            }
        }
        logger.warn("can't find paramter for " + name);
        //can't find that name???
        return null;
    }

    /**
     * Adds a new parameter. Currently objects that are not DOM Elements are
     * stored in memory, but cannot be premanantly saved in the xml file.
     * 
     * @param name
     *            a <code>String</code> name for this parameter
     * @param value
     *            an <code>Object</code> value
     * @param audit
     *            the audit related to this paramter
     */
    public void addParameter(String name, Object value, AuditInfo[] audit) {
        String[] oldNames = getParameterNames(); // make sure name cache is
                                                 // populated
        parameterCache.put(name, value);
        Element parameter = config.getOwnerDocument()
                .createElement("parameter");
        XMLParameter.insert(parameter, name, value);
        config.appendChild(parameter);
        updateParameterNameCache(name);
    }

    public void addParameterRef(URL paramURL,
                                String name,
                                Object object,
                                AuditInfo[] audit) {
        String baseStr = base.toString();
        String paramStr = paramURL.toString();
        if(paramStr.startsWith(baseStr)) {
            // use relative URL
            paramStr = paramStr.substring(baseStr.length());
        } // end of if (paramStr.startsWith(baseStr))
        Document doc = config.getOwnerDocument();
        Element param = doc.createElement("parameterRef");
        XMLParameter.insertParameterRef(param, name, paramStr, object);
        config.appendChild(param);
        updateParameterNameCache(name);
    }

    /**
     * Gets the Ids for all child datasets of this dataset.
     * 
     * @return a <code>String[]</code> id
     */
    public String[] getDataSetIds() {
        if(dataSetIdCache == null) {
            String[] internal = XMLUtil.getAllAsStrings(this.config,
                                                        "*/@datasetid");
            String[] external = getDataSetRefIds();
            String[] tmp = new String[internal.length + external.length];
            System.arraycopy(internal, 0, tmp, 0, internal.length);
            System.arraycopy(external, 0, tmp, internal.length, external.length);
            dataSetIdCache = tmp;
        } // end of if (dataSetIdCache == null)
        return dataSetIdCache;
    }

    String[] getDataSetRefIds() {
        String[] xlinktmps = XMLUtil.getAllAsStrings(this.config, "datasetRef");
        String xlinkNS = "http://www.w3.org/1999/xlink";
        NodeList nodes = XMLUtil.evalNodeList(config, "datasetRef");
        if(nodes == null) { return new String[0]; } // end of if (nodes == null)
        String[] xlinks = new String[nodes.getLength()];
        String[] ids = new String[nodes.getLength()];
        for(int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            NamedNodeMap map = n.getAttributes();
            for(int j = 0; j < map.getLength(); j++) {
                //logger.debug("attribute: "+map.item(j).getLocalName());
            } // end of for (int j=0; j<map.getLength(); j++)
            try {
                if(n instanceof Element) {
                    Element e = (Element)n;
                    String href = e.getAttribute("xlink:href");
                    SimpleXLink sl = new SimpleXLink(docBuilder, e, getBase());
                    Element referredElement = sl.retrieve();
                    //logger.debug("simpleLink element
                    // is"+referredElement.toString());
                    XMLDataSet ds = new XMLDataSet(docBuilder,
                                                   new URL(getBase(), href),
                                                   referredElement);
                    dataSetCache.put(ds.getId(), ds);
                    ids[i] = ds.getId();
                } else {
                    ids[i] = null;
                } // end of else
            } catch(Exception e) {
                logger.error("can't get dataset for " + xlinks[i], e);
                ids[i] = null;
            } // end of try-catch
        } // end of for (int i=0; i<xlinks.length; i++)
        //logger.debug("got "+xlinks.length+" datasetRef ids from
        // "+xlinktmps.length+" datasetRefs");
        return ids;
    }

    /**
     * Gets the names of all child datasets of this dataset.
     * 
     * @return a <code>String[]</code> value
     */
    public String[] getDataSetNames() {
        String[] ids = getDataSetIds();
        String[] names = new String[ids.length];
        for(int i = 0; i < ids.length; i++) {
            DataSet ds = getDataSetById(ids[i]);
            //caching here so that further caching wont be necessary when we
            // call
            //getDataSet.added by srinivasa.
            dataSetCache.put(ds.getId(), ds);
            names[i] = ds.getName();
        } // end of for (int i=0; i<ids.length; i++)
        return names;
    }

    /**
     * Gets the dataset with the given name.
     * 
     * @param name
     *            a <code>String</code> dataset name
     * @return a <code>DataSet</code>, or null if it cannot be found
     */
    /**
     * Describe <code>getDataSet</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSet(String name) {
        String[] ids = getDataSetIds();
        for(int i = 0; i < ids.length; i++) {
            DataSet ds = getDataSetById(ids[i]);
            //logger.debug("++++++++ name is "+name +" the datasetID name is
            // "+ds.getName());
            //logger.debug("returning as found in CACHE
            // "+((XMLDataSet)ds).getBase().toString());
            if(name.equals(ds.getName())) { return ds; }
        } // end of for (int i=0; i<ids.length; i++)
        return null;
    }

    /**
     * Adds a child dataset.
     * 
     * @param dataset
     *            a <code>DataSet</code>
     * @param audit
     *            the audit info for this dataset addition
     */
    /**
     * Describe <code>addDataSet</code> method here.
     * 
     * @param dataset
     *            an <code>edu.sc.seis.fissuresUtil.xml.DataSet</code> value
     * @param audit
     *            an <code>AuditInfo[]</code> value
     */
    public void addDataSet(edu.sc.seis.fissuresUtil.xml.DataSet dataset,
                           AuditInfo[] audit) {
        if(dataset instanceof XMLDataSet) {
            XMLDataSet xds = (XMLDataSet)dataset;
            Element element = xds.getElement();
            if(element.getOwnerDocument().equals(config.getOwnerDocument())) {
                config.appendChild(element);
                logger.debug("dataset append "
                        + config.getChildNodes().getLength());
            } else {
                // not from the same document, must clone
                Node copyNode = config.getOwnerDocument().importNode(element,
                                                                     true);
                config.appendChild(copyNode);
                logger.debug("dataset import "
                        + config.getChildNodes().getLength());
                NodeList nl = config.getChildNodes();
                for(int i = 0; i < nl.getLength(); i++) {
                    //logger.debug("node "+nl.item(i).getLocalName());
                } // end of for (int i=0; i<nl.getLenght(); i++)
            } // end of else
            dataSetCache.put(dataset.getId(), dataset);
            String[] ids = getDataSetIds();
            dataSetIdCache = null;
            String[] tmp = new String[ids.length + 1];
            System.arraycopy(ids, 0, tmp, 0, ids.length);
            tmp[tmp.length - 1] = dataset.getId();
            dataSetIdCache = tmp;
        } else {
            logger.warn("Attempt to add non-XML dataset");
        } // end of else
    }

    /**
     * Describe <code>addDataSetRef</code> method here.
     * 
     * @param datasetURL
     *            an <code>URL</code> value
     * @param audit
     *            an <code>AuditInfo[]</code> value
     */
    public void addDataSetRef(URL datasetURL, AuditInfo[] audit) {
        dataSetIdCache = null;
        String baseStr = base.toString();
        String datasetStr = datasetURL.toString();
        if(datasetStr.startsWith(baseStr)) {
            // use relative URL
            datasetStr = datasetStr.substring(baseStr.length());
        } // end of if
        Document doc = config.getOwnerDocument();
        Element datasetElement = doc.createElement("datasetRef");
        datasetElement.setAttributeNS(xlinkNS, "xlink:type", "simple");
        datasetElement.setAttributeNS(xlinkNS, "xlink:href", datasetStr);
        config.appendChild(datasetElement);
    }

    /**
     * Creates a new DataSet as a child of this one.
     */
    public DataSet createChildDataSet(String id,
                                      String name,
                                      String owner,
                                      AuditInfo[] audit) {
        name = XMLUtil.getUniqueName(getDataSetNames(), name);
        XMLDataSet dataset = new XMLDataSet(docBuilder, base, id, name, owner);
        addDataSet(dataset, audit);
        return dataset;
    }

    /**
     * Gets the dataset with the given id.
     * 
     * @param id
     *            a <code>String</code> id
     * @return a <code>DataSet</code>
     */
    /**
     * Describe <code>getDataSetById</code> method here.
     * 
     * @param id
     *            a <code>String</code> value
     * @return a <code>DataSet</code> value
     */
    public DataSet getDataSetById(String id) {
        if(dataSetCache.containsKey(id)) {
            //logger.debug("returning as found in CACHE
            // "+getBase().toString());
            return (DataSet)dataSetCache.get(id);
        }
        NodeList nList = XMLUtil.evalNodeList(config, "//dataset[@datasetid="
                + dquote + id + dquote + "]");
        if(nList != null && nList.getLength() != 0) {
            Node n = nList.item(0);
            if(n instanceof Element) {
                XMLDataSet dataset = new XMLDataSet(docBuilder,
                                                    base,
                                                    (Element)n);
                dataSetCache.put(id, dataset);
                return dataset;
            }
        }
        //try to get the dataset from the datasetRefs.
        //added by srinivasa
        NodeList nodes = XMLUtil.evalNodeList(config, "datasetRef");
        if(nodes == null) {
            //logger.debug("returning null as the nodes is null");
            return null;
        } // end of if (nodes == null)
        //logger.debug("*********** Before For the length is
        // "+nodes.getLength());
        for(int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            NamedNodeMap map = n.getAttributes();
            try {
                //logger.debug("*********** Before Checking for If");
                if(n instanceof Element) {
                    Element e = (Element)n;
                    String href = e.getAttribute("xlink:href");
                    SimpleXLink sl = new SimpleXLink(docBuilder, e, getBase());
                    Element referredElement = sl.retrieve();
                    //logger.debug("simpleLink element
                    // is"+referredElement.toString());
                    XMLDataSet ds = new XMLDataSet(docBuilder,
                                                   new URL(getBase(), href),
                                                   referredElement);
                    if(id.equals(ds.getId())) {
                        dataSetCache.put(ds.getId(), ds);
                        return ds;
                    }
                } else {
                    return null;
                } // end of else
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            } // end of try-catch
        } // end of for (int i=0; i<xlinks.length; i++)
        // not an embedded dataset, try datasetRef
        // getIds adds to cache
        String[] ids = getDataSetRefIds();
        if(dataSetCache.containsKey(id)) { return (DataSet)dataSetCache.get(id); }
        logger.error("Couldn't get datasetRef :" + id);
        // can't find it
        return null;
    }

    /**
     * Gets the names of the seismograms in this dataset.
     * 
     * @return the names.
     */
    public String[] getSeismogramNames() {
        if(seismogramNameCache == null) {
            seismogramNameCache = cacheSeismogramNames();
        } // end of if (seismogramNameCache == null)
        return seismogramNameCache;
    }

    /**
     * Describe <code>cacheSeismogramNames</code> method here.
     * 
     * @return a <code>String[]</code> value
     */
    protected String[] cacheSeismogramNames() {
        String[] names = XMLUtil.getAllAsStrings(this.config,
                                                 "localSeismogram/seismogramAttr/property[name="
                                                         + dquote + "Name"
                                                         + dquote + "]"
                                                         + "/value/text()");
        return names;
    }

    /**
     * Describe <code>getSeismogramAttrs</code> method here.
     * 
     * @return a <code>SeismogramAttr[]</code> value
     */
    public SeismogramAttr[] getSeismogramAttrs() {
        NodeList nList;
        nList = XMLUtil.evalNodeList(config, "localSeismogram/seismogramAttr");
        SeismogramAttr[] seismogramAttrs = new SeismogramAttr[0];
        if(nList != null && nList.getLength() != 0) {
            seismogramAttrs = new SeismogramAttr[nList.getLength()];
            for(int counter = 0; counter < nList.getLength(); counter++) {
                seismogramAttrs[counter] = XMLSeismogramAttr.getSeismogramAttr((Element)nList.item(counter));
            }//end of for.
        }//end of if.
        return seismogramAttrs;
    }

    /**
     * Describe <code>getChannelIds</code> method here.
     * 
     * @return a <code>ChannelId[]</code> value
     */
    public ChannelId[] getChannelIds() {
        String[] paramNames = getParameterNames();
        ArrayList arrayList = new ArrayList();
        for(int counter = 0; counter < paramNames.length; counter++) {
            if(paramNames[counter].startsWith(StdDataSetParamNames.CHANNEL)) {
                Channel channel = (Channel)getParameter(paramNames[counter]);
                arrayList.add(channel.get_id());
            }
        }
        ChannelId[] channelIds = new ChannelId[arrayList.size()];
        channelIds = (ChannelId[])arrayList.toArray(channelIds);
        return channelIds;
    }

    private void updateParameterNameCache(String paramName) {
        String[] temp = parameterNameCache;
        if(temp == null) {
            temp = new String[1];
            temp[0] = paramName;
        } else {
            temp = new String[parameterNameCache.length + 1];
            System.arraycopy(parameterNameCache,
                             0,
                             temp,
                             0,
                             parameterNameCache.length);
            temp[parameterNameCache.length] = paramName;
        }
        parameterNameCache = temp;
    }

    private URL getSeismogramURL(String name) {
        String urlString = "NONE";
        NodeList nList = XMLUtil.evalNodeList(config,
                                              "localSeismogram/seismogramAttr/property[name="
                                                      + dquote + "Name"
                                                      + dquote + "]"
                                                      + "[value=" + dquote
                                                      + name + dquote + "]"
                                                      + "/../../data");
        if(nList == null || (nList != null && nList.getLength() == 0)) {
            //    nList = getNoNameSeismogram(name);
        }
        if(nList != null && nList.getLength() != 0) {
            try {
                Node n = nList.item(0);
                if(n instanceof Element) {
                    Element e = (Element)n;
                    //logger.debug("**********************The name of the
                    // element is "+e.getTagName());
                    urlString = e.getAttribute("xlink:href");
                    if(urlString == null || urlString == "") { throw new MalformedURLException(name
                            + " does not have an xlink:href attribute"); } // end
                                                                           // of
                                                                           // if
                                                                           // (urlString
                                                                           // ==
                                                                           // null
                                                                           // ||
                                                                           // urlString
                                                                           // ==
                                                                           // "")
                    //logger.debug("IN GET SEISMOGRAM The base str is
                    // "+base.toString());
                    URL sacURL = new URL(base, urlString);
                    return sacURL;
                }
            } catch(MalformedURLException e) {
                logger.error("Couldn't get seismogram " + name, e);
                logger.error(urlString);
            } catch(Exception e) {
                logger.error("Couldn't get seismogram " + name, e);
                logger.error(urlString);
            } // end of try-catch
        }
        return null;
    }

    /**
     * Gets the seismogram for the given name, Null if it cannot be found.
     * 
     * @param name
     *            a <code>String</code> name
     * @return a <code>LocalSeismogramImpl</code>
     */
    public LocalSeismogramImpl getSeismogram(String name) {
        URL sacURL = getSeismogramURL(name);
        if(sacURL != null) {
            NodeList nList = XMLUtil.evalNodeList(config,
                                                  "localSeismogram/seismogramAttr/property[name="
                                                          + dquote + "Name"
                                                          + dquote + "]"
                                                          + "[value=" + dquote
                                                          + name + dquote + "]"
                                                          + "/../../data");
            Node n = nList.item(0);
            Element e = (Element)n;
            //logger.debug("The sacUrl is "+sacURL.toString());
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(sacURL.openStream()));
                SacTimeSeries sac = new SacTimeSeries();
                sac.read(dis);
                LocalSeismogramImpl seis;
                //get the Seismogram Attributes from the xml .. only the data
                // must
                // must be obtained fromt the SAC.
                NodeList seisAttrNode = XMLUtil.evalNodeList(e,
                                                             "../seismogramAttr");
                SeismogramAttr seisAttr = null;
                if(seisAttrNode != null && seisAttrNode.getLength() != 0) {
                    seisAttr = XMLSeismogramAttr.getSeismogramAttr((Element)seisAttrNode.item(0));
                }
                NodeList propList = XMLUtil.evalNodeList(e, "property");
                int numDSProps = 0;
                if(propList != null && propList.getLength() != 0) {
                    numDSProps = nList.getLength();
                } else {
                    // no properties in dataset
                    numDSProps = 0;
                } // end of else
                if(seisAttr != null) {
                    seis = SacToFissures.getSeismogram(sac, seisAttr);
                } else {
                    seis = SacToFissures.getSeismogram(sac);
                } // end of else
                return seis;
            } catch(Exception ex) {
                logger.error("Couldn't get seismogram " + name, ex);
            } // end of try-catch
        }
        return null;
    }

    /**
     * Adds a seismogram.
     * 
     * @param seis
     *            a <code>LocalSeismogramImpl</code> seismogram
     * @param audit
     *            the audit for this seismogram
     */
    public void addSeismogram(LocalSeismogramImpl seis, AuditInfo[] audit) {
        seismogramNameCache = null;
        // Note this does not set the xlink, as the seis has not been saved
        // anywhere yet.
        Document doc = config.getOwnerDocument();
        Element localSeismogram = doc.createElement("localSeismogram");//doc.createElement("SacSeismogram");
        String name = seis.getProperty(seisNameKey);
        if(name == null || name.length() == 0) {
            name = seis.channel_id.network_id.network_code + "."
                    + seis.channel_id.station_code + "."
                    + seis.channel_id.channel_code;
            //edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(seis.channel_id);
        }
        name = XMLUtil.getUniqueName(getSeismogramNames(), name);
        seis.setName(name);
        Element seismogramAttr = doc.createElement("seismogramAttr");
        XMLSeismogramAttr.insert(seismogramAttr, (LocalSeismogram)seis);
        localSeismogram.appendChild(seismogramAttr);
        config.appendChild(localSeismogram);
        //  seismogramCache.put(name, seis);
        seismogramNameCache = null;
    }

    /**
     * Adds a reference to a remote seismogram.
     * 
     * @param seisURL
     *            an <code>URL</code> to the seismogram
     * @param name
     *            a <code>String</code> name
     * @param props
     *            the properties for this seismogram to be stored in the dataset
     * @param parm_ids
     *            the Parameter References for this seismogram to be stored in
     *            the dataset
     * @param audit
     *            the audit for thie seismogram
     */
    public void addSeismogramRef(LocalSeismogramImpl seis,
                                 URL seisURL,
                                 String name,
                                 Property[] props,
                                 ParameterRef[] parm_ids,
                                 AuditInfo[] audit) {
        seismogramNameCache = null;
        String baseStr = base.toString();
        String seisStr = seisURL.toString();
        if(seisStr.startsWith(baseStr)) {
            // use relative URL
            seisStr = seisStr.substring(baseStr.length());
        } // end of if (seisStr.startsWith(baseStr))
        Document doc = config.getOwnerDocument();
        Element localSeismogram = doc.createElement("localSeismogram");
        if(name == null || name.length() == 0) {
            name = seis.getProperty(seisNameKey);
        }
        if(name == null || name.length() == 0) {
            name = edu.iris.Fissures.network.ChannelIdUtil.toStringNoDates(seis.channel_id);
        }
        name = XMLUtil.getUniqueName(getSeismogramNames(), name);
        seis.setName(name);
        Element seismogramAttr = doc.createElement("seismogramAttr");
        XMLSeismogramAttr.insert(seismogramAttr, (LocalSeismogram)seis);
        localSeismogram.appendChild(seismogramAttr);
        Element data = doc.createElement("data");
        data.setAttributeNS(xlinkNS, "xlink:type", "simple");
        data.setAttributeNS(xlinkNS, "xlink:href", seisStr);
        data.setAttribute("seisType", "sac");
        localSeismogram.appendChild(data);
        config.appendChild(localSeismogram);
    }

    public void addDataSetSeismogram(DataSetSeismogram dss, AuditInfo[] audit) {
        String name;
        name = dss.getName();
        if(name == null || name.length() == 0) {
            name = ChannelIdUtil.toStringNoDates(dss.getRequestFilter().channel_id);
        } // end of if ()
        name = XMLUtil.getUniqueName(getDataSetSeismogramNames(), name);
        if(!name.equals(dss.getName())) {
            dss.setName(name);
        } // end of if ()
        dssNames.add(name);
        dataSetSeismograms.put(name, dss);
        dss.setDataSet(this);
    }

    public DataSetSeismogram getDataSetSeismogram(String name) {
        return (DataSetSeismogram)dataSetSeismograms.get(name);
    }

    public String[] getDataSetSeismogramNames() {
        return ((String[])dssNames.toArray(new String[dssNames.size()]));
    }

    public void remove(DataSetSeismogram dss) {
        dataSetSeismograms.remove(dss.getName());
        dssNames.remove(dss.getName());
    }

    /**
     * Describe <code>toString</code> method here.
     * 
     * @return a <code>String</code> value
     */
    public String toString() {
        return getName();
    }

    /**
     * returns a DOM Element that represents this dataset.
     * 
     * @return an <code>Element</code> value
     */
    public Element getElement() {
        return config;
    }

    /**
     * Describe <code>getEvent</code> method here.
     * 
     * @return an <code>edu.sc.seis.fissuresUtil.cache.CacheEvent</code> value
     */
    public EventAccessOperations getEvent() {
        return (EventAccessOperations)getParameter(StdDataSetParamNames.EVENT);
    }

    /**
     * Describe <code>getChannel</code> method here.
     * 
     * @param channelId
     *            a <code>ChannelId</code> value
     * @return an <code>edu.iris.Fissures.IfNetwork.Channel</code> value
     */
    public Channel getChannel(ChannelId channelId) {
        Object obj = getParameter(StdDataSetParamNames.CHANNEL
                + ChannelIdUtil.toString(channelId));
        return (Channel)obj;
    }

    /**
     * Writes the xml version of this dataset to the output stream.
     * 
     * @param out
     *            an <code>OutputStream</code> value
     * @exception Exception
     *                if an error occurs
     */
    public void write(OutputStream out) throws Exception {
        write(out, getElement());
    }

    public static void write(OutputStream out, Element el) throws Exception {
        javax.xml.transform.TransformerFactory tfactory = javax.xml.transform.TransformerFactory.newInstance();
        // This creates a transformer that does a simple identity transform,
        // and thus can be used for all intents and purposes as a serializer.
        javax.xml.transform.Transformer serializer = tfactory.newTransformer();
        java.util.Properties oprops = new java.util.Properties();
        oprops.put("method", "xml");
        oprops.put("indent", "yes");
        //        oprops.put("xalan:indent-amount", "4");
        serializer.setOutputProperties(oprops);
        serializer.transform(new javax.xml.transform.dom.DOMSource(el),
                             new javax.xml.transform.stream.StreamResult(out));
    }

    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        if(factory == null) {
            factory = DocumentBuilderFactory.newInstance();
        }
        return factory;
    }

    public static DocumentBuilder getDocumentBuilder()
            throws ParserConfigurationException {
        return getDocumentBuilderFactory().newDocumentBuilder();
    }

    static DocumentBuilderFactory factory = null;

    static DocumentBuilder staticDocBuilder = null;

    private Map dataSetSeismograms = new HashMap();

    private List dssNames = new LinkedList();

    private XPathAPI xpath = new XPathAPI();

    private org.apache.xml.utils.PrefixResolver prefixResolver;

    /**
     * Describe variable <code>base</code> here.
     */
    protected URL base;

    /**
     * Describe variable <code>config</code> here.
     */
    protected Element config;

    /**
     * Describe variable <code>docBuilder</code> here.
     */
    protected DocumentBuilder docBuilder;

    /**
     * Describe variable <code>parameterCache</code> here.
     */
    protected HashMap parameterCache = new HashMap();

    /**
     * Describe variable <code>parameterNameCache</code> here.
     */
    protected String[] parameterNameCache = null;

    /**
     * Describe variable <code>dataSetIdCache</code> here.
     */
    protected String[] dataSetIdCache = null;

    /**
     * Describe variable <code>dataSetCache</code> here.
     */
    protected HashMap dataSetCache = new HashMap();

    /**
     * Describe variable <code>seismogramCache</code> here.
     */
    protected HashMap seismogramCache = new HashMap();

    /**
     * Describe variable <code>seismogramNameCache</code> here.
     */
    protected String[] seismogramNameCache = null;

    private static final String dquote = "" + '"';

    private static final String xlinkNS = "http://www.w3.org/1999/xlink";

    private static final String seisNameKey = "Name";

    static Category logger = Category.getInstance(XMLDataSet.class.getName());
} // XMLDataSet
