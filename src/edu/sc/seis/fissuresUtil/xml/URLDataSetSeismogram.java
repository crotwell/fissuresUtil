package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.lang.ref.SoftReference;
import java.net.URL;
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
        super(dataset, name);
        this.url = url;
        this.fileType = fileType;
    }

    public URLDataSetSeismogram (URL url, SeismogramFileTypes fileType, DataSet dataSet){
        this(url, fileType, dataSet, "");
    }

    public URLDataSetSeismogram(URL url, SeismogramFileTypes fileType) {
        this(url, fileType, null);
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
        SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        if(fileType == SeismogramFileTypes.MSEED) {
                            finished(dataListener);
                            return;
                        }
                        LocalSeismogramImpl seis = getSeismogram();
                        LocalSeismogramImpl[] seismos;
                        if(seis != null) {
                            seismos = new LocalSeismogramImpl[1];
                            seismos[0] = seis;
                            pushData(seismos, dataListener);
                        } else {
                            seismos = new LocalSeismogramImpl[0];
                        }
                        finished(dataListener);
                    }
                });
    }

    public String getName() {
        if(super.getName() == null || super.getName().length()==0 ) {
            String name = url.getFile();
            int index = name.lastIndexOf(File.separatorChar);
            setName(name.substring(index)+2);
        }
        return super.getName();
    }


    public RequestFilter getRequestFilter() {
        if(super.getRequestFilter() == null) {
            setRequestFilter(getSeismogram());
        }
        return requestFilter;
    }

    private void setRequestFilter(LocalSeismogramImpl seis){
        requestFilter = new RequestFilter(seis.getChannelID(),
                                          seis.getBeginTime().getFissuresTime(),
                                          seis.getEndTime().getFissuresTime());
    }

    private LocalSeismogramImpl getSeismogram() {
        if(!seisCache.isEmpty() &&
           seisCache.get(0) != null &&
               ((SoftReference)seisCache.get(0)).get() != null){
            return (LocalSeismogramImpl)((SoftReference)seisCache.get(0)).get();
        }
        try {
            SacTimeSeries sac = new SacTimeSeries();
            sac.read(new DataInputStream(new BufferedInputStream(url.openStream())));
            LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
            setRequestFilter(seis);
            addToCache(seis);
            return seis;
        } catch(Exception e) {
            return null;
        }
    }

    private URL url;

    private SeismogramFileTypes fileType;

}// URLDataSetSeismogram
