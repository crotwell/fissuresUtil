/**
 * URLDataSetSeismogramSaver.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class URLDataSetSeismogramSaver implements SeisDataChangeListener {

    URLDataSetSeismogramSaver(DataSetSeismogram dss,
                              File directory,
                              SeismogramFileTypes fileType) {
        this.inDSS = dss;
        this.directory = directory;
        this.fileType = fileType;
        urlDSS = new URLDataSetSeismogram(new URL[0],
                                          new SeismogramFileTypes[0],
                                          inDSS.getDataSet(),
                                          inDSS.getName(),
                                          inDSS.getRequestFilter());
        logger.debug("req filter chanid="+ChannelIdUtil.toString(urlDSS.getRequestFilter().channel_id));
        dss.retrieveData(this);
        Iterator it = inDSS.getAuxillaryDataKeys().iterator();
        while(it.hasNext()) {
            Object next = it.next();
            urlDSS.addAuxillaryData(next, inDSS.getAuxillaryData(next));
        }
    }

    /** gets the url dataset seismogram that is being populated. This may
     not yet be completely populated due to download delays. */
    public URLDataSetSeismogram getURLDataSetSeismogram() {
        return urlDSS;
    }

    public boolean isError() {
        return (error != null);
    }

    public Throwable getError() {
        return error;
    }

    public boolean isFinished() {
        return finished;
    }

    public void error(SeisDataErrorEvent sdce) {
        if (sdce.getInitiator() != this) {
            // must not have been initiated by us, wait for "real" notifiy
            // from our request
            return;
        }
        logger.debug("Got error "+sdce.getCausalException());
        setError(sdce.getCausalException());
    }

    public void finished(SeisDataChangeEvent sdce) {
        if (sdce.getInitiator() != this) {
            // must not have been initiated by us, wait for "real" notifiy
            // from our request
            return;
        }
        logger.debug("Got finished");
        finished = true;
    }

    public void pushData(SeisDataChangeEvent sdce) {
        if (sdce.getInitiator() != this) {
            // must not have been initiated by us, wait for "real" notifiy
            // from our request
            return;
        }
        logger.debug("Got pushData for "+sdce.getSource().getName());
        LocalSeismogramImpl[] seis = sdce.getSeismograms();

        for (int i = 0; i < seis.length; i++) {
            try {
                File seisFile =
                    URLDataSetSeismogram.saveAs(seis[i],
                                                directory,
                                                inDSS.getDataSet().getChannel(inDSS.getRequestFilter().channel_id),
                                                inDSS.getDataSet().getEvent(),
                                                fileType);
                urlDSS.addToCache(seisFile.toURI().toURL(),
                                  fileType,
                                  seis[i]);
            } catch (Exception e) {
                setError(e);
            }
        }
    }

    private void setError(Throwable problem) {
        // only save the first error
        if (error == null) {
            error = problem;
        }
    }

    Throwable error;

    boolean finished = false;

    File directory;

    SeismogramFileTypes fileType;

    DataSetSeismogram inDSS;

    URLDataSetSeismogram urlDSS;

    private static Logger logger = LoggerFactory.getLogger(URLDataSetSeismogramSaver.class);

}

