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
    public URLDataSetSeismogram (URL url, SeismogramFileTypes fileType, DataSet dataSet){
        this.url = url;
        this.fileType = fileType;
        this.dataSet = dataSet;
    }

    public URLDataSetSeismogram(URL url, SeismogramFileTypes fileType) {
        this(url, fileType, null);
    }
    
    public Object clone() {
        return super.clone();
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    
                    if(fileType == SeismogramFileTypes.MSEED) {
                        finished(dataListener);
                        return;
                    }
                    try {
                        SacTimeSeries sac = new SacTimeSeries();
                        sac.read(new DataInputStream(
                                                     new BufferedInputStream(url.openStream())
                                                     )
                                 );
                        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
                        
                        LocalSeismogramImpl[] seismos;
                        if(seis != null) {
                            seismos = new LocalSeismogramImpl[1];
                            seismos[0] = seis;
                        } else {
                            seismos = new LocalSeismogramImpl[0];
                        }
                        pushData(seismos, dataListener);
                        finished(dataListener);
                    } catch(Exception e) {
                        finished(dataListener);
                    }
                }
            });
    }

    
    

    private URL url;

    private SeismogramFileTypes fileType;

    DataSet dataSet;
    
}// URLDataSetSeismogram
