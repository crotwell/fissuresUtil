package edu.sc.seis.fissuresUtil.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.Property;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.fissuresUtil.psn.PSNToFissures;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.psn.PSNDataFile;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

/**
 * URLDataSetSeismogram.java Created: Tue Mar 18 15:37:07 2003
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version
 */
public class URLDataSetSeismogram extends DataSetSeismogram {

    public URLDataSetSeismogram(URL url,
                                SeismogramFileTypes fileType,
                                DataSet dataset,
                                String name) {
        this(new URL[] {url},
             new SeismogramFileTypes[] {fileType},
             dataset,
             name);
    }

    public URLDataSetSeismogram(URL[] url,
                                SeismogramFileTypes[] fileType,
                                DataSet dataset,
                                String name) {
        this(url, fileType, dataset, name, null);
    }

    public URLDataSetSeismogram(URL[] url,
                                SeismogramFileTypes[] fileType,
                                DataSet dataset,
                                String name,
                                RequestFilter requestFilter) {
        super(dataset, name, requestFilter);
        this.url = url;
        this.fileType = fileType;
    }

    public URLDataSetSeismogram(URL url,
                                SeismogramFileTypes fileType,
                                DataSet dataset) {
        this(new URL[] {url}, new SeismogramFileTypes[] {fileType}, dataset);
    }

    public URLDataSetSeismogram(URL[] url,
                                SeismogramFileTypes[] fileType,
                                DataSet dataset) {
        this(url, fileType, dataset, "");
    }

    public URLDataSetSeismogram(URL url, SeismogramFileTypes fileType) {
        this(new URL[] {url}, new SeismogramFileTypes[] {fileType});
    }

    public URLDataSetSeismogram(URL url,
                                SeismogramFileTypes fileType,
                                String name) {
        this(new URL[] {url}, new SeismogramFileTypes[] {fileType}, name);
    }

    public URLDataSetSeismogram(URL[] url, SeismogramFileTypes[] fileType) {
        this(url, fileType, (DataSet)null);
    }

    public URLDataSetSeismogram(URL[] url,
                                SeismogramFileTypes[] fileType,
                                String name) {
        this(url, fileType, null, name);
    }

    public URLDataSetSeismogram(URL[] url,
                                SeismogramFileTypes[] fileType,
                                String name,
                                RequestFilter requestFilter) {
        this(url, fileType, null, name, requestFilter);
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
        // logger.debug("adding " + url[0] + " to the pool");
        WorkerThreadPool.getDefaultPool().invokeLater(new Runnable() {

            public void run() {
                LocalSeismogramImpl[] seismos;
                for(int i = 0; i < url.length; i++) {
                    // logger.debug("retrieveData for " + url[i]);
                    try {
                        LocalSeismogramImpl seis = getSeismogram(i);
                        if(seis != null) {
                            seismos = new LocalSeismogramImpl[1];
                            seismos[0] = seis;
                            pushData(seismos, dataListener);
                        } else {
                            seismos = new LocalSeismogramImpl[0];
                        }
                    } catch(Exception e) {
                        error(dataListener, e);
                    }
                }
                // logger.debug("finished urlDSS.retrieveData");
                finished(dataListener);
            }
        });
    }

    public LocalSeismogramImpl[] getSeismograms() throws Exception {
        List seismos = new ArrayList();
        for(int i = 0; i < url.length; i++) {
            LocalSeismogramImpl seis = getSeismogram(i);
            if(seis != null) {
                seismos.add(seis);
            }
        }
        return (LocalSeismogramImpl[])seismos.toArray(new LocalSeismogramImpl[0]);
    }

    public RequestFilter getRequestFilter() {
        if(requestFilter == null) {
            for(int i = 0; i < url.length; i++) {
                try {
                    // this updates the request filter internally as a side
                    // effect
                    getSeismogram(i);
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Cannot get seismogram for "
                            + url[i].toString(), e);
                }
            }
        }
        return requestFilter;
    }

    public URL[] getURLs() {
        return url;
    }

    public static URLDataSetSeismogram localize(DataSetSeismogram dss,
                                                File directory,
                                                SeismogramFileTypes fileType)
            throws MalformedURLException {
        URLDataSetSeismogram urlDSS;
        URL fileURL = directory.toURL();
        if(dss instanceof URLDataSetSeismogram) {
            // check for seismograms already in directory
            urlDSS = (URLDataSetSeismogram)dss;
            URL[] url = urlDSS.getURLs();
            boolean isLocal = true;
            for(int i = 0; i < url.length; i++) {
                if(url[i].getProtocol().equals("file")
                        || url[i].getProtocol().equals("")) {
                    // file onlocal system, but may be in different directory
                    if(!url[i].getPath().startsWith(fileURL.getPath())) {
                        // paths don't match
                        isLocal = false;
                    }
                } else {
                    // not local
                    isLocal = false;
                }
            }
            if(isLocal) {
                // all ok with this URLDataSetSeismogram
                return urlDSS;
            }
        }
        // either isLocal is false, or not a URLDataSetSeismogram, so must
        // localize
        URLDataSetSeismogramSaver saver = new URLDataSetSeismogramSaver(dss,
                                                                        directory,
                                                                        fileType);
        URLDataSetSeismogram out = saver.getURLDataSetSeismogram();
        while(!saver.isFinished()) {
            logger.debug("Waiting for saver to finish");
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}
        }
        if(saver.isError()) {
            // uh oh
            // probably should throw something instead of this, but the error
            // may be ok?
            String dssName = "dss is null";
            if(dss != null)
                dssName = dss.getName();
            GlobalExceptionHandler.handle("A problem occured trying to localize the "
                                                  + dssName
                                                  + " dataset seismogram.",
                                          saver.getError());
        }
        return out;
    }

    public static URLDataSetSeismogram saveLocally(DataSet dataset,
                                                   File directory,
                                                   LocalSeismogramImpl[] seismograms,
                                                   Channel channel,
                                                   EventAccessOperations event,
                                                   AuditInfo[] audit,
                                                   SeismogramFileTypes seisFileType)
            throws CodecException, IOException, NoPreferredOrigin,
            SeedFormatException, UnsupportedFileTypeException {
        URL[] seisURL = new URL[seismograms.length];
        for(int i = 0; i < seismograms.length; i++) {
            seisURL[i] = saveAs(seismograms[i],
                                directory,
                                channel,
                                event,
                                seisFileType).toURI().toURL();
            logger.debug("Save as locally for " + seisURL[i]);
        }
        SeismogramFileTypes[] seisFileTypeArray = new SeismogramFileTypes[seismograms.length];
        for(int i = 0; i < seisFileTypeArray.length; i++) {
            seisFileTypeArray[i] = seisFileType;
        }
        URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(seisURL,
                                                               seisFileTypeArray,
                                                               dataset);
        dataset.addDataSetSeismogram(urlDSS, audit);
        dataset.addParameter(DataSet.CHANNEL
                + ChannelIdUtil.toString(channel.get_id()), channel, audit);
        urlDSS.addAuxillaryData(NETWORK_BEGIN,
                                channel.get_id().network_id.begin_time.date_time);
        urlDSS.addAuxillaryData(CHANNEL_BEGIN,
                                channel.get_id().begin_time.date_time);
        for(int i = 0; i < seisURL.length; i++) {
            urlDSS.addToCache(seisURL[i], seisFileType, seismograms[i]);
        }
        return urlDSS;
    }

    public static File saveAsSac(LocalSeismogramImpl seis, File directory)
            throws IOException, CodecException {
        try {
            return saveAsSac(seis, directory, null, null);
        } catch(NoPreferredOrigin e) {
            // cant happen as we are sending null
        }
        return null;
    }

    public static File saveAs(LocalSeismogramImpl seis,
                              File directory,
                              Channel channel,
                              EventAccessOperations event,
                              SeismogramFileTypes saveFileType)
            throws IOException, NoPreferredOrigin, CodecException,
            UnsupportedFileTypeException, SeedFormatException {
        if(saveFileType.equals(SeismogramFileTypes.SAC)) {
            return saveAsSac(seis, directory, channel, event);
        } else if(saveFileType.equals(SeismogramFileTypes.MSEED)) {
            return saveAsMSeed(seis, directory, channel, event);
        } else {
            throw new UnsupportedFileTypeException("Unsupported File Type "
                    + saveFileType.getName());
        }
    }

    public static File getUnusedFileName(File directory,
                                         Channel channel,
                                         String suffix) {
        File seisFile = getBaseFile(directory, channel, suffix);
        for(int n = 1; seisFile.exists(); n++) {
            seisFile = makeFile(directory, n, channel, suffix);
        }
        return seisFile;
    }

    public static File getBaseFile(File directory,
                                   Channel channel,
                                   String suffix) {
        return makeFile(directory, 0, channel, suffix);
    }

    public static File makeFile(File directory,
                                int count,
                                Channel channel,
                                String suffix) {
        String seisFilename = ChannelIdUtil.toStringNoDates(channel.get_id());
        seisFilename += count > 0 ? "" + count : "";
        seisFilename = seisFilename.replace(' ', '_');// make spacespace sites
        // '__'
        seisFilename += suffix;
        return new File(directory, seisFilename);
    }

    public static File saveAsMSeed(LocalSeismogramImpl seis,
                                   File directory,
                                   Channel channel,
                                   EventAccessOperations event)
            throws IOException, SeedFormatException {
        File seisFile = getUnusedFileName(directory, channel, ".mseed");
        return writeMSeed(seis, seisFile);
    }

    public static File writeMSeed(LocalSeismogramImpl seis, File seisFile)
            throws SeedFormatException, FileNotFoundException, IOException {
        DataRecord[] dr = FissuresConvert.toMSeed(seis);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(seisFile)));
        for(int i = 0; i < dr.length; i++) {
            dr[i].write(dos);
        }
        dos.close();
        return seisFile;
    }

    public static File saveAsSac(LocalSeismogramImpl seis,
                                 File directory,
                                 Channel channel,
                                 EventAccessOperations event)
            throws IOException, NoPreferredOrigin, CodecException {
        File seisFile = getUnusedFileName(directory, channel, ".sac");
        SacTimeSeries sac = FissuresToSac.getSAC(seis,
                                                 channel,
                                                 event != null ? event.get_preferred_origin()
                                                         : null);
        sac.write(seisFile);
        return seisFile;
    }

    private void setRequestFilter(LocalSeismogramImpl seis) {
        MicroSecondDate begin = seis.getBeginTime();
        MicroSecondDate end = seis.getEndTime();
        ChannelId chanId = seis.getChannelID();
        if(requestFilter != null) {
            MicroSecondDate tmp = new MicroSecondDate(requestFilter.start_time);
            if(tmp.before(begin)) {
                begin = tmp;
            }
            tmp = new MicroSecondDate(requestFilter.end_time);
            if(tmp.after(end)) {
                end = tmp;
            }
            chanId = requestFilter.channel_id;
            seis.channel_id = chanId;
        }
        requestFilter = new RequestFilter(chanId,
                                          begin.getFissuresTime(),
                                          end.getFissuresTime());
    }

    private LocalSeismogramImpl getSeismogram(int seisNum) throws IOException,
            FissuresException, SeedFormatException,
            UnsupportedFileTypeException {
        URL seisURL = url[seisNum];
        Object obj = urlToLSMap.get(seisURL);
        if(obj instanceof SoftReference) {
            Object ref = ((SoftReference)obj).get();
            if(ref != null) {
                obj = ref;
            } else {
                urlToLSMap.remove(seisURL);
                obj = null;
            }
        }
        if(obj != null) {
            return (LocalSeismogramImpl)obj;
        }
        LocalSeismogramImpl seis;
        if(isPSN(seisNum)) {
            URL psnURL = getURLfromPSNURL(seisURL);
            // Don't need to close this InputStream, the PSNDataFile constructor
            // does it.
            PSNDataFile psnDataFile = new PSNDataFile(new DataInputStream(new BufferedInputStream(psnURL.openStream())));
            int evRecIndex = getIndexFromPSNURL(seisURL);
            seis = PSNToFissures.getSeismograms(psnDataFile)[evRecIndex];
        } else if(isMSeed(seisNum)) {
            DataInputStream dis = null;
            List list = new ArrayList();
            try {
                dis = new DataInputStream(new BufferedInputStream(seisURL.openStream()));
                MiniSeedRead mseedRead = new MiniSeedRead(dis);
                while(true) {
                    list.add(mseedRead.getNextRecord(4096));
                }
            } catch(EOFException e) {
                // must be all
            } finally {
                if(dis != null) {
                    dis.close();
                }
            }
            seis = FissuresConvert.toFissures((DataRecord[])list.toArray(new DataRecord[0]), (byte)B1000Types.STEIM1, (byte)1);
        } else if(isSac(seisNum)) {
            SacTimeSeries sacTime = new SacTimeSeries();
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(seisURL.openStream()));
                sacTime.read(dis);
            } finally {
                if(dis != null) {
                    dis.close();
                }
            }
            seis = SacToFissures.getSeismogram(sacTime);
        } else {
            throw new UnsupportedFileTypeException("File type "
                    + fileType[seisNum].getName() + " is not supported");
        }
        // set channel id correctly if extra info stored in Aux data
        Object netBegin = getAuxillaryData(NETWORK_BEGIN);
        if(netBegin != null && netBegin instanceof String) {
            seis.channel_id.network_id.begin_time = new Time((String)netBegin,
                                                             -1);
        }
        Object chanBegin = getAuxillaryData(CHANNEL_BEGIN);
        if(chanBegin != null && chanBegin instanceof String) {
            seis.channel_id.begin_time = new Time((String)chanBegin, -1);
        }
        /*
         * for now, let's avoid setting the y_unit on the seismogram directly.
         * There's a line in UnitRangeImpl that HPC said could break gee, and it
         * certainly does if you set the unit on the seismograms here.
         */
        // if(y_unit != null) {
        // seis.y_unit = y_unit;
        // }
        addToCache(seisURL, fileType[seisNum], seis);
        return seis;
    }

    public boolean isSac(int seisNum) {
        return fileType[seisNum].equals(SeismogramFileTypes.SAC);
    }

    public boolean isMSeed(int seisNum) {
        return fileType[seisNum].equals(SeismogramFileTypes.MSEED);
    }

    public boolean isPSN(int seisNum) {
        return fileType[seisNum].equals(SeismogramFileTypes.PSN);
    }

    public void addToCache(URL seisurl,
                           SeismogramFileTypes seisFileType,
                           LocalSeismogramImpl seis) {
        setRequestFilter(seis);
        addToCache(seis);
        urlToLSMap.put(seisurl, new SoftReference(seis));
        // check to see if URL is already in array
        boolean found = false;
        for(int i = 0; i < url.length; i++) {
            if(seisurl.equals(url[i])) {
                found = true;
            }
        }
        if(!found) {
            // found, so no need to add
            URL[] tmp = new URL[url.length + 1];
            System.arraycopy(url, 0, tmp, 0, url.length);
            url = tmp;
            url[url.length - 1] = seisurl;
            SeismogramFileTypes[] tmpTypes = new SeismogramFileTypes[fileType.length + 1];
            System.arraycopy(fileType, 0, tmpTypes, 0, fileType.length);
            fileType = tmpTypes;
            fileType[fileType.length - 1] = seisFileType;
        }
    }

    /**
     * allows the saving of a URLDataSetSeismogram in XML format. The actual
     * waveform data is not saved, just the URLs to it. If local saving is
     * needed, localize should be used before calling insertInto. All URLs are
     * saved realtive to the base.
     */
    public void insertInto(XMLStreamWriter writer, URL base)
            throws XMLStreamException {
        XMLUtil.writeTextElement(writer, "name", getName());
        writer.writeStartElement("requestFilter");
        XMLRequestFilter.insert(writer, getRequestFilter());
        XMLUtil.writeEndElementWithNewLine(writer);
        logger.debug("Saving " + url.length + " urls for " + getName());
        String baseStr = base.toString();
        String outStr;
        for(int i = 0; i < url.length; i++) {
            outStr = url[i].toString();
            logger.debug("base=" + baseStr + " outStr=" + outStr);
            if(outStr.startsWith(baseStr)) {
                outStr = outStr.substring(baseStr.length());
            }
            writer.writeStartElement("url");
            writer.writeAttribute("xlink:type", "simple");
            writer.writeAttribute("xlink:href", outStr);
            writer.writeAttribute("xlink:role", fileType[i].getURLValue()
                    .toString());
            XMLUtil.writeEndElementWithNewLine(writer);
        }
        writer.writeStartElement("y_unit");
        XMLUnit.insert(writer, getYUnit());
        XMLUtil.writeEndElementWithNewLine(writer);
        Iterator it = getAuxillaryDataKeys().iterator();
        while(it.hasNext()) {
            Object next = it.next();
            if(next instanceof String) {
                if(getAuxillaryData(next) instanceof String) {
                    writer.writeStartElement(PROPERTY);
                    XMLProperty.insert(writer,
                                       (String)next,
                                       (String)getAuxillaryData(next));
                    XMLUtil.writeEndElementWithNewLine(writer);
                } else if(getAuxillaryData(next) instanceof Element) {
                    logger.debug(NAMED_VALUE + " insert placeholder");
                    // writer.writeStartElement(NAMED_VALUE);
                    // XMLUtil.writeTextElement(writer, "name", (String)next);
                    // writer.writeStartElement("value");
                    // XMLUtil.writeNode(writer,
                    // (Element)getAuxillaryData(next));
                    // XMLUtil.writeEndElementWithNewLine(writer);
                    // XMLUtil.writeEndElementWithNewLine(writer);
                }
            } else {
                logger.warn("try to save aux data " + next + " "
                        + getAuxillaryData(next).getClass() + ": "
                        + getAuxillaryData(next) + " but don't know how.");
            }
        }
    }

    /**
     * allows the saving of a URLDataSetSeismogram in XML format. The actual
     * waveform data is not saved, just the URLs to it. If local saving is
     * needed, localize should be used before calling insertInto. All URLs are
     * saved realtive to the base.
     */
    public void insertInto(Element element, URL base) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc, "name", getName()));
        Element rf = doc.createElement("requestFilter");
        XMLRequestFilter.insert(rf, getRequestFilter());
        element.appendChild(rf);
        logger.debug("Saving " + url.length + " urls for " + getName());
        String baseStr = base.toString();
        String outStr;
        for(int i = 0; i < url.length; i++) {
            outStr = url[i].toString();
            logger.debug("base=" + baseStr + " outStr=" + outStr);
            if(outStr.startsWith(baseStr)) {
                outStr = outStr.substring(baseStr.length());
            }
            Element urlElement = doc.createElement("url");
            urlElement.setAttribute("xlink:type", "simple");
            urlElement.setAttribute("xlink:href", outStr);
            urlElement.setAttribute("xlink:role", fileType[i].getURLValue()
                    .toString());
            element.appendChild(urlElement);
        }
        try {
            LocalSeismogramImpl[] seismos = getSeismograms();
            Element yUnit = doc.createElement("y_unit");
            XMLUnit.insert(yUnit, seismos[0].y_unit);
            element.appendChild(yUnit);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("problem getting y_unit from seismogram",
                                          e);
        }
        Iterator it = getAuxillaryDataKeys().iterator();
        while(it.hasNext()) {
            Object next = it.next();
            if(next instanceof String) {
                if(getAuxillaryData(next) instanceof String) {
                    Element prop = doc.createElement(PROPERTY);
                    XMLProperty.insert(prop,
                                       (String)next,
                                       (String)getAuxillaryData(next));
                } else if(getAuxillaryData(next) instanceof Element) {
                    Element namedValue = doc.createElement(NAMED_VALUE);
                    namedValue.appendChild(XMLUtil.createTextElement(doc,
                                                                     "name",
                                                                     (String)next));
                    Element valueElement = doc.createElement("value");
                    valueElement.appendChild(doc.importNode((Element)getAuxillaryData(next),
                                                            true));
                    namedValue.appendChild(valueElement);
                    element.appendChild(namedValue);
                }
            } else {
                logger.warn("try to save aux data " + next + " "
                        + getAuxillaryData(next).getClass() + ": "
                        + getAuxillaryData(next) + " but don't know how.");
            }
        }
    }

    public static URLDataSetSeismogram getURLDataSetSeismogram(URL base,
                                                               Element element)
            throws UnsupportedFileTypeException {
        String name = XMLUtil.getText(XMLUtil.getElement(element, "name"));
        RequestFilter request = XMLRequestFilter.getRequestFilter(XMLUtil.getElement(element,
                                                                                     "requestFilter"));
        NodeList children = element.getElementsByTagName("url");
        LinkedList fileTypeList = new LinkedList();
        LinkedList urlList = new LinkedList();
        for(int i = 0; i < children.getLength(); i++) {
            NamedNodeMap attrList = children.item(i).getAttributes();
            Node urlNode = attrList.getNamedItem("xlink:href");
            Node fileTypeNode = attrList.getNamedItem("xlink:role");
            if(urlNode != null && fileTypeNode != null) {
                try {
                    urlList.add(new URL(base, urlNode.getNodeValue()));
                    fileTypeList.add(SeismogramFileTypes.fromString(fileTypeNode.getNodeValue()));
                } catch(MalformedURLException e) {
                    // should never happen in a valid dataset xml doc
                    GlobalExceptionHandler.handle("MalformedURLException should never happen",
                                                  e);
                }
            } else {
                GlobalExceptionHandler.handle(new UnsupportedFileTypeException("File type for "
                        + urlNode.getNodeValue() + " is null, skipping."));
            }
        }
        Unit y_unit = null;
        if(DOMHelper.hasElement(element, "y_unit")) {
            y_unit = XMLUnit.getUnit(DOMHelper.getElement(element, "y_unit"));
        }
        URL[] urls = new URL[children.getLength()];
        urls = (URL[])urlList.toArray(urls);
        SeismogramFileTypes[] seisTypes = (SeismogramFileTypes[])fileTypeList.toArray(new SeismogramFileTypes[0]);
        URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(urls,
                                                               seisTypes,
                                                               name,
                                                               request);
        if(y_unit != null) {
            urlDSS.setYUnit(y_unit);
        }
        // children = element.getElementsByTagName(NAMED_VALUE);
        // for(int i = 0; i < children.getLength(); i++) {
        // Element nameElement =
        // (Element)((Element)children.item(i)).getElementsByTagName("name")
        // .item(0);
        // String auxName = XMLUtil.getText(nameElement);
        // urlDSS.addAuxillaryData(auxName,
        // ((Element)children.item(i)).getElementsByTagName("value")
        // .item(0)
        // .getFirstChild());
        // }
        children = element.getElementsByTagName(PROPERTY);
        for(int i = 0; i < children.getLength(); i++) {
            Property p = XMLProperty.getProperty((Element)children.item(i));
            urlDSS.addAuxillaryData(p.name, p.value);
        }
        return urlDSS;
    }

    public static URLDataSetSeismogram getURLDataSetSeismogram(URL base,
                                                               XMLStreamReader parser)
            throws XMLStreamException, MalformedURLException,
            UnsupportedFileTypeException {
        XMLUtil.gotoNextStartElement(parser, "name");
        String name = parser.getElementText();
        XMLUtil.gotoNextStartElement(parser, "requestFilter");
        RequestFilter request = XMLRequestFilter.getRequestFilter(parser);
        XMLUtil.gotoNextStartElement(parser, "url");
        List urlList = new ArrayList();
        List fileTypeList = new ArrayList();
        urlList.add(new URL(base, parser.getAttributeValue(null, "href")));
        fileTypeList.add(SeismogramFileTypes.fromString(parser.getAttributeValue(null,
                                                                                 "role")));
        XMLUtil.getNextStartElement(parser);
        while(parser.getLocalName().equals("url")) {
            urlList.add(new URL(base, parser.getAttributeValue(null, "href")));
            fileTypeList.add(SeismogramFileTypes.fromString(parser.getAttributeValue(null,
                                                                                     "role")));
            XMLUtil.getNextStartElement(parser);
        }
        URL[] urls = (URL[])urlList.toArray(new URL[0]);
        SeismogramFileTypes[] fileTypes = (SeismogramFileTypes[])fileTypeList.toArray(new SeismogramFileTypes[0]);
        URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(urls,
                                                               fileTypes,
                                                               name,
                                                               request);
        if(parser.getLocalName().equals("y_unit")) {
            urlDSS.setYUnit(XMLUnit.getUnit(parser));
        }
        while(parser.hasNext() && parser.getLocalName().equals("property")) {
            Property p = XMLProperty.getProperty(parser);
            urlDSS.addAuxillaryData(p.name, p.value);
            XMLUtil.getNextStartElement(parser);
        }
        return urlDSS;
    }

    public static final String PROPERTY = "property";

    public static final String NAMED_VALUE = "namedValue";

    public static URL createPSNURL(URL psnUrl, int index)
            throws MalformedURLException {
        return new URL(psnUrl.toString()
                + "#edu.sc.seis.fissuresUtil.psn.PSNEventRecord=" + index);
    }

    public static URL getURLfromPSNURL(URL psnURL) throws MalformedURLException {
        String urlString = psnURL.toString();
        return new URL(urlString.substring(0, urlString.indexOf('#')));
    }

    public static int getIndexFromPSNURL(URL psnURL) {
        String urlString = psnURL.toString();
        return Integer.parseInt(urlString.substring(urlString.indexOf('=') + 1));
    }

    private URL[] url;

    private SeismogramFileTypes[] fileType;

    /**
     * Uses SoftReferences, this allows a map from URL to LocalSeismogram, but
     * does not prevent garbage collection.
     */
    private HashMap urlToLSMap = new HashMap();
}// URLDataSetSeismogram
