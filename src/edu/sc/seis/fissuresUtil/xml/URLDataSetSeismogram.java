package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;

/**
 * URLDataSetSeismogram.java
 *
 *
 * Created: Tue Mar 18 15:37:07 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class URLDataSetSeismogram extends DataSetSeismogram{
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
        setRequestFilter(seis);
        addToCache(seis);
        urlToLSMap.put(seisurl, seis);
        return seis;
    }

    private URL[] url;

    private SeismogramFileTypes fileType;

    /** This allows a map from URL to LocalSeismogram, but does not prevent
     garbage collection. */
    private WeakHashMap urlToLSMap = new WeakHashMap();

}// URLDataSetSeismogram
