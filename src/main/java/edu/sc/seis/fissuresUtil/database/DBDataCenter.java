package edu.sc.seis.fissuresUtil.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * DBDataCenter.java Created: Tue Feb 4 10:55:16 2003
 * 
 * @author <a href="mailto:"> Srinivasa Telukutla </a>
 * @version
 */
public class DBDataCenter implements DataCenterOperations, LocalDCOperations {

    private DBDataCenter(String directoryName, String databaseName,
            DataCenterOperations dataCenterRouter) throws SQLException {
        this.dataCenter = dataCenterRouter;
        hsqlRequestFilterDb = new HSQLRequestFilterDb(directoryName,
                                                      databaseName,
                                                      dataCenterRouter);
    }

    private DBDataCenter(String directoryName, String databaseName)
            throws SQLException {
        hsqlRequestFilterDb = new HSQLRequestFilterDb(directoryName,
                                                      databaseName);
    }

    public static DBDataCenter getDataCenter(String directoryName,
                                             String databaseName,
                                             DataCenterOperations dataCenterRouter)
            throws SQLException {
        if(dbDataCenter == null) {
            dbDataCenter = new DBDataCenter(directoryName,
                                            databaseName,
                                            dataCenterRouter);
        }
        return dbDataCenter;
    }

    public static DBDataCenter getDataCenter(String directoryName,
                                             String databaseName)
            throws SQLException {
        if(dbDataCenter == null) {
            dbDataCenter = new DBDataCenter(directoryName, databaseName);
        }
        return dbDataCenter;
    }

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        return dataCenter.available_data(a_filterseq);
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      edu.iris.Fissures.Time expiration_time) {
        return getNextRequestId();
    }

    /**
     * if long_lived is true then the request is "sticky" in that the client
     * wants the data center to return not just the data that it has in its
     * archive currently, but also any data that it receives up to the
     * expiration_time. For instance if a station sends its data by mailing
     * tapes, then a researcher could issue a request for data that is expected
     * to be delivered from a recent earthquake, even thought the data center
     * does not yet have the data. Note that expiration_time is ignored if
     * long_lived is false.
     */
    public String request_seismograms(RequestFilter[] a_filterseq,
                                      LocalDataCenterCallBack a_client,
                                      SeisDataChangeListener initiator,
                                      boolean long_lived,
                                      edu.iris.Fissures.Time expiration_time) {
        DataCenterThread dcThread = (DataCenterThread)clientToThread.get(a_client);
        if(dcThread == null || !dcThread.getData(initiator, a_filterseq)) {
            dcThread = new DataCenterThread(a_filterseq,//missing_seq,
                                            a_client, initiator, this);
            clientToThread.put(a_client, dcThread);
            dcThreadPool.invokeLater(dcThread);
            DataCenterThread.incrementWaiters();
        }
        return getNextRequestId();
    }

    private WorkerThreadPool dcThreadPool = new WorkerThreadPool("DBDataCenter",
                                                                 5);

    private Map clientToThread = new HashMap();

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_seismograms:1.0
    //
    /***/
    public synchronized LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq)
            throws edu.iris.Fissures.FissuresException {
        //here first check with the database to see if
        //if the seismogram is already in the cache.
        //if it is the case .. return it..
        //else use the dataCenter Router to get the seismograms.
        LocalSeismogramImpl[] localSeismograms = new LocalSeismogramImpl[0];
        try {
            localSeismograms = hsqlRequestFilterDb.getSeismograms(a_filterseq);
        } catch(SQLException e) {
            logger.error("Problem retriieving from DBDataCenter, using remote datacenter instead: request is:\n"
                                 + requestToString(a_filterseq),
                         e);
        } catch(java.io.IOException e) {
            logger.error("Problem retriieving from DBDataCenter, using remote datacenter instead: request is:\n"
                                 + requestToString(a_filterseq),
                         e);
        } // end of catch
        RequestFilter[] uncovered = CoverageTool.notCovered(a_filterseq,
                                                            localSeismograms);
        if(uncovered.length == 0) return localSeismograms;
        List seisToReturn = new ArrayList(localSeismograms.length);
        insertIntoList(seisToReturn, localSeismograms);
        if(dataCenter != null) {
            localSeismograms = (LocalSeismogramImpl[])dataCenter.retrieve_seismograms(uncovered);
            try {
                hsqlRequestFilterDb.addSeismogram(localSeismograms);
            } catch(SQLException e) {
                logger.error("Problem storing seismograms in local cache database.",
                             e);
            } catch(CodecException e) {
                logger.error("Problem storing seismograms in local cache database.",
                             e);
            } catch(IOException e) {
                logger.error("Problem storing seismograms in local cache database.",
                             e);
            }
        }
        if(localSeismograms.length > 0) insertIntoList(seisToReturn,
                                                       localSeismograms);
        LocalSeismogram[] rtnValues = new LocalSeismogram[seisToReturn.size()];
        rtnValues = (LocalSeismogram[])seisToReturn.toArray(rtnValues);
        return rtnValues;
    }
    
    public static String requestToString(RequestFilter[] a_filterseq) {
        String request = "";
        for(int i = 0; i < a_filterseq.length; i++) {
            request+="\n"+ChannelIdUtil.toString(a_filterseq[i].channel_id)+" from "+a_filterseq[i].start_time.date_time+" to "+a_filterseq[i].end_time.date_time;
        }
        return request;
    }

    private void insertIntoList(List list, LocalSeismogram[] localSeismograms) {
        for(int counter = 0; counter < localSeismograms.length; counter++) {
            list.add(localSeismograms[counter]);
        }
    }

    public String queue_seismograms(RequestFilter[] a_filterseq) {
        return getNextRequestId();
    }

    public LocalSeismogram[] retrieve_queue(String a_request) {
        return new LocalSeismogram[0];
    }

    public void cancel_request(String a_request) {}

    public String request_status(String a_request) {
        return new String();
    }

    public String getFileIds(ChannelId channelId,
                             MicroSecondDate beginDate,
                             MicroSecondDate endDate) throws SQLException {
        return hsqlRequestFilterDb.getFileIds(channelId, beginDate, endDate);
    }

    public LocalSeismogram getSeismogram(String fileIds) throws SQLException,
            java.io.IOException, edu.iris.Fissures.FissuresException {
        return hsqlRequestFilterDb.getSeismogram(fileIds);
    }

    private DataCenterOperations dataCenter;

    private HSQLRequestFilterDb hsqlRequestFilterDb;

    private static DBDataCenter dbDataCenter;

    /**
     * used to generate unique return ids for requests. May not be used yet, but
     * at least the strings will be unique.
     */
    String getNextRequestId() {
        return "requestId" + requestNumber;
    }

    private int requestNumber = 1;

    private static Logger logger = LoggerFactory.getLogger(DBDataCenter.class);
}// DBDataCenter
