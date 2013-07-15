package edu.sc.seis.fissuresUtil.xml;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.mseed.FissuresConvert;
import edu.sc.seis.seisFile.SeisFileException;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQuerier;
import edu.sc.seis.seisFile.fdsnws.FDSNDataSelectQueryParams;
import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.DataRecordIterator;


public class FDSNWSDataSetSeismogram extends DataSetSeismogram {

    public FDSNWSDataSetSeismogram(RequestFilter requestFilter) {
        super(null, "", requestFilter);
    }

    public FDSNWSDataSetSeismogram(DataSet ds, String name, RequestFilter requestFilter, String userAgent) {
        super(ds, name, requestFilter);
        this.userAgent = userAgent;
    }

    @Override
    public void retrieveData(final SeisDataChangeListener dataListener) {

        WorkerThreadPool.getDefaultPool().invokeLater(new Runnable() {

            public void run() {
                RequestFilter request = getRequestFilter();
                try {
                    FDSNDataSelectQueryParams qp = new FDSNDataSelectQueryParams();
                    qp.appendToNetwork(request.channel_id.network_id.network_code)
                    .appendToStation(request.channel_id.station_code)
                    .appendToLocation(request.channel_id.site_code)
                    .appendToChannel(request.channel_id.channel_code)
                    .setStartTime(new MicroSecondDate(request.start_time))
                    .setEndTime(new MicroSecondDate(request.end_time));
                    FDSNDataSelectQuerier querier = new FDSNDataSelectQuerier(qp);

                    if (userAgent != null) {
                        querier.setUserAgent(userAgent);
                    }
                    List<DataRecord> drList = new ArrayList<DataRecord>();
                    try {
                        DataRecordIterator drIt = querier.getDataRecordIterator();
                        while (drIt.hasNext()) {
                            drList.add(drIt.next());
                        }
                    } catch(FDSNWSException e) {
                        if (querier.getResponseCode() == 401 || querier.getResponseCode() == 403) {
                            error(dataListener, new Exception("Authorization failure to " + e.getTargetURI(), e));
                        } else {
                            error(dataListener, e);
                        }
                    } catch(SeisFileException e) {
                        error(dataListener, e);
                    } catch(SocketTimeoutException e) {
                        error(dataListener, e);
                    } catch(IOException e) {
                        error(dataListener, e);
                    }
                    
                    
                    List<LocalSeismogramImpl> seisList = FissuresConvert.toFissures(drList);
                        if(seisList != null) {
                            pushData(seisList.toArray(new LocalSeismogramImpl[0]), dataListener);
                        }
                    } catch(Exception e) {
                        error(dataListener, e);
                    }
                
                // logger.debug("finished urlDSS.retrieveData");
                finished(dataListener);
            }
        });
    }
    
    String userAgent = null;

    
    public String getUserAgent() {
        return userAgent;
    }

    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
}
