package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.sc.seis.fissuresUtil.sac.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

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
            LocalSeismogramImpl seis = getSeismogram();
            requestFilter = new RequestFilter(seis.getChannelID(),
                                              seis.getBeginTime().getFissuresTime(),
                                              seis.getEndTime().getFissuresTime());
        }
        return requestFilter;

    }

    private LocalSeismogramImpl getSeismogram() {
        try {
            SacTimeSeries sac = new SacTimeSeries();
            sac.read(new DataInputStream(
                                         new BufferedInputStream(url.openStream())
                                         )
                     );
            LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
            return seis;

        } catch(Exception e) {
            return null;
        }
    }

    private URL url;

    private SeismogramFileTypes fileType;

}// URLDataSetSeismogram
