package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * URLDataSetSeismogram.java
 *
 *
 * Created: Tue Mar 18 15:37:07 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class URLDataSetSeismogram extends DataSetSeismogram {
    public URLDataSetSeismogram (URL url,
                                 SeismogramFileTypes fileType,
                                 DataSet dataset,
                                 String name){
        this(new URL[] { url }, fileType, dataset, name);
    }

    public URLDataSetSeismogram (URL[] url,
                                 SeismogramFileTypes fileType,
                                 DataSet dataset,
                                 String name){
        super(dataset, name);
        this.url = url;
        this.fileType = fileType;
    }

    public URLDataSetSeismogram (URL url, SeismogramFileTypes fileType, DataSet dataset){
        this(new URL[] { url }, fileType, dataset);
    }

    public URLDataSetSeismogram (URL[] url, SeismogramFileTypes fileType, DataSet dataset){
        this(url, fileType, dataset, "");
        if (url.length > 0) {
            String tmpName = url[0].getFile();
            int index = tmpName.lastIndexOf(File.separatorChar);
            setName(tmpName.substring(index)+2);
        }
    }

    public URLDataSetSeismogram(URL url, SeismogramFileTypes fileType) {
        this(new URL[] { url }, fileType);
    }

    public URLDataSetSeismogram(URL[] url, SeismogramFileTypes fileType) {
        this(url, fileType, null);
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
        SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        if(fileType == SeismogramFileTypes.MSEED) {
                            finished(dataListener);
                            return;
                        }

                        LocalSeismogramImpl[] seismos;
                        for (int i = 0; i < url.length; i++) {
                            try {
                                LocalSeismogramImpl seis = getSeismogram(url[i]);
                                if (seis != null) {
                                    seismos = new LocalSeismogramImpl[1];
                                    seismos[0] = seis;
                                    pushData(seismos, dataListener);
                                } else {
                                    seismos = new LocalSeismogramImpl[0];
                                }
                            } catch(IOException e) {
                                error(dataListener, e);
                            } catch(FissuresException e) {
                                error(dataListener, e);
                            }
                        }
                        finished(dataListener);
                    }
                });
    }


    public RequestFilter getRequestFilter() {
        if(super.getRequestFilter() == null) {
            for (int i = 0; i < url.length; i++) {
                try {
                    // this updates the request filter internally as a side effect
                    LocalSeismogramImpl seis = getSeismogram(url[i]);
                } catch(IOException e) {
                    GlobalExceptionHandler.handleStatic("Cannot get seismogram for "+url[i].toString(),
                                                        e);
                } catch(FissuresException e) {
                    GlobalExceptionHandler.handleStatic("Cannot get seismogram for "+url[i].toString(),
                                                        e);
                }
            }
        }
        return requestFilter;
    }

    public URL[] getURLs() {
        return url;
    }

    public static URLDataSetSeismogram localize(DataSetSeismogram dss,
                                                File directory) throws MalformedURLException {
        URLDataSetSeismogram urlDSS;
        URL fileURL = directory.toURL();
        if (dss instanceof URLDataSetSeismogram) {
            // check for seismograms already in directory
            urlDSS = (URLDataSetSeismogram)dss;
            URL[] url = urlDSS.getURLs();
            boolean isLocal = true;
            for (int i = 0; i < url.length; i++) {
                if (url[i].getProtocol().equals("file") || url[i].getProtocol().equals("")) {
                    // file onlocal system, but may be in different directory
                    if ( ! url[i].getPath().startsWith(fileURL.getPath())) {
                        // paths don't match
                        isLocal = false;
                    }
                } else {
                    // not local
                    isLocal = false;
                }
            }
            if (isLocal) {
                // all ok with this URLDataSetSeismogram
                return urlDSS;
            }
        }
        // either isLocal is false, or not a URLDataSetSeismogram, so must localize
        URLDataSetSeismogramSaver saver = new URLDataSetSeismogramSaver(dss,
                                                                        directory);
        URLDataSetSeismogram out = saver.getURLDataSetSeismogram();
        while ( ! saver.isFinished()) {
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
            }
        }
        if (saver.isError()) {
            // uh oh
            // probably should throw something instead of this, but the error may be ok?
            GlobalExceptionHandler.handleStatic("A problem occured trying to localize the "+
                                                    dss.getName()+" dataset seismogram.",
                                                saver.getError());
        }
        return out;
    }

    public static URLDataSetSeismogram saveLocally(DataSet dataset,
                                                   File directory,
                                                   LocalSeismogramImpl[] seismograms,
                                                   Channel channel,
                                                   EventAccessOperations event,
                                                   AuditInfo[] audit)
        throws  CodecException,
        IOException,
        NoPreferredOrigin {
        URL[] seisURL = new URL[seismograms.length];
        for (int i=0; i<seismograms.length; i++) {
            seisURL[i] = saveAsSac(seismograms[i], directory);
        }
        URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(seisURL,
                                                               SeismogramFileTypes.SAC,
                                                               dataset);
        dataset.addDataSetSeismogram(urlDSS, audit);
        dataset.addParameter(DataSet.CHANNEL+ChannelIdUtil.toString(channel.get_id()),
                             channel,
                             audit);

        urlDSS.addAuxillaryData(NETWORK_BEGIN,
                                channel.get_id().network_id.begin_time.date_time);
        urlDSS.addAuxillaryData(CHANNEL_BEGIN,
                                channel.get_id().begin_time.date_time);
        for (int i = 0; i < seisURL.length; i++) {
            urlDSS.addToCache(seisURL[i], seismograms[i]);
        }
        return urlDSS;
    }

    public static URL saveAsSac(LocalSeismogramImpl seis,
                                File directory)
        throws IOException, CodecException {
        try {
            return saveAsSac(seis, directory, null, null);
        } catch (NoPreferredOrigin e) {
            // cant happen as we are sending null
        }
        return null;
    }

    public static URL saveAsSac(LocalSeismogramImpl seis,
                                File directory,
                                Channel channel,
                                EventAccessOperations event)
        throws IOException, NoPreferredOrigin, CodecException {

        SacTimeSeries sac;
        String seisFilename = "";
        seisFilename = ChannelIdUtil.toStringNoDates(seis.channel_id);
        seisFilename.replace(' ', '.'); // check for space-space site
        File seisFile = new File(directory, seisFilename);
        int n =0;
        while (seisFile.exists()) {
            n++;

            seisFilename =
                ChannelIdUtil.toStringNoDates(seis.channel_id)+"."+n;
            seisFilename.replace(' ', '.'); // check for space-space site
            seisFile = new File(directory, seisFilename);
        } // end of while (seisFile.exists())

        if (channel != null) {
            if (event != null) {
                sac = FissuresToSac.getSAC(seis,
                                           channel,
                                           event.get_preferred_origin());
            } else {
                sac = FissuresToSac.getSAC(seis,
                                           channel);
            }
        } else {
            if (event != null) {
                sac = FissuresToSac.getSAC(seis,
                                           event.get_preferred_origin());
            } else {
                sac = FissuresToSac.getSAC(seis);
            }
        }
        sac.write(seisFile);
        return seisFile.toURL();
    }

    private void setRequestFilter(LocalSeismogramImpl seis){
        MicroSecondDate begin = seis.getBeginTime();
        MicroSecondDate end = seis.getEndTime();
        if (requestFilter != null) {
            MicroSecondDate tmp = new MicroSecondDate(requestFilter.start_time);
            if (tmp.before(begin)) begin = tmp;
            tmp = new MicroSecondDate(requestFilter.end_time);
            if (tmp.after(end)) end = tmp;
        }
        requestFilter = new RequestFilter(seis.getChannelID(),
                                          begin.getFissuresTime(),
                                          end.getFissuresTime());
    }

    private LocalSeismogramImpl getSeismogram(URL seisurl)
        throws IOException, FissuresException {

        Object obj = urlToLSMap.get(seisurl);
        if (obj != null) {
            return (LocalSeismogramImpl)obj;
        }
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(new DataInputStream(new BufferedInputStream(seisurl.openStream())));
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);

        // set channel id correctly if extra info stored in Aux data
        Object netBegin = getAuxillaryData(NETWORK_BEGIN);
        if (netBegin != null && netBegin instanceof String) {
            seis.channel_id.network_id.begin_time.date_time = (String)netBegin;
        }
        Object chanBegin = getAuxillaryData(CHANNEL_BEGIN);
        if (chanBegin != null && chanBegin instanceof String) {
            seis.channel_id.begin_time.date_time = (String)chanBegin;
        }

        addToCache(seisurl, seis);
        return seis;
    }

    public void addToCache(URL seisurl, LocalSeismogramImpl seis) {
        setRequestFilter(seis);
        addToCache(seis);
        urlToLSMap.put(seisurl, seis);
    }

    /** allows the saving of a URLDataSetSeismogram in XML format. The
    actual waveform data is not saved, just the URLs to it. If local
     saving is needed, localize should be used before calling insertInto. */
    public void insertInto(Element element) {
        Document doc = element.getOwnerDocument();
        element.appendChild(XMLUtil.createTextElement(doc,
                                                      "name",
                                                      getName()));

        Element rf = doc.createElement("requestFilter");
        XMLRequestFilter.insert(rf, getRequestFilter());
        element.appendChild(rf);

        Iterator it = getAuxillaryDataKeys().iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (next instanceof String && getAuxillaryData(next) instanceof String) {
                Element prop = doc.createElement("property");
                XMLProperty.insert(prop, (String)next, (String)getAuxillaryData(next));
            } else {
                System.out.println("try to save aux data "+
                                       next+" "+
                                       getAuxillaryData(next).getClass()+": "+getAuxillaryData(next)+
                                       " but only know how to save strings.");
            }
        }
    }

    private URL[] url;

    private SeismogramFileTypes fileType;

    /** This allows a map from URL to LocalSeismogram, but does not prevent
     garbage collection. */
    private WeakHashMap urlToLSMap = new WeakHashMap();

}// URLDataSetSeismogram
