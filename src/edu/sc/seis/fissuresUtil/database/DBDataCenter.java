package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DBDataCenter.java
 *
 *
 * Created: Tue Feb  4 10:55:16 2003
 *
 * @author <a href="mailto:"> Srinivasa Telukutla</a>
 * @version
 */

public class DBDataCenter implements DataCenterOperations, LocalDCOperations {
    private DBDataCenter (DataCenterOperations dataCenterRouter) throws SQLException {
        this.dataCenter = dataCenterRouter;
        hsqlRequestFilterDb = new HSQLRequestFilterDb(dataCenterRouter);
    }

    private DBDataCenter () throws SQLException {

        hsqlRequestFilterDb = new HSQLRequestFilterDb();
    }

    public static DBDataCenter getDataCenter(DataCenterOperations dataCenterRouter) throws SQLException {
        if(dbDataCenter == null) {
            dbDataCenter = new DBDataCenter(dataCenterRouter);
        }
        return dbDataCenter;
    }

    public static DBDataCenter getDataCenter() throws SQLException {
        if(dbDataCenter == null) {
            dbDataCenter = new DBDataCenter();
        }
        return dbDataCenter;
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/available_data:1.0
    //
    /***/

    public RequestFilter[]
        available_data(RequestFilter[] a_filterseq) {
        return dataCenter.available_data(a_filterseq);
    }

    public String
        request_seismograms(RequestFilter[] a_filterseq,
                            DataCenterCallBack a_client,
                            boolean long_lived,
                            edu.iris.Fissures.Time expiration_time)
        throws edu.iris.Fissures.FissuresException {
        return getNextRequestId();
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/request_seismograms:1.0
    //
    /** if long_lived is true then the request is "sticky" in that
     *the client wants the data center to return not just the data
     *that it has in its archive currently, but also any data that it
     *receives up to the  expiration_time. For instance if a station
     *sends its data by mailing tapes, then a researcher could issue
     *a request for data that is expected to be delivered from a
     *recent earthquake, even thought the data center does not yet
     *have the data. Note that expiration_time is ignored if long_lived
     *is false.*/

    public String
        request_seismograms(RequestFilter[] a_filterseq,
                            LocalDataCenterCallBack a_client,
                            SeisDataChangeListener initiator,
                            boolean long_lived,
                            edu.iris.Fissures.Time expiration_time)
        throws edu.iris.Fissures.FissuresException {
        //first check the database to  see if there is any data for each
        //of the request filters if data present return data and
        //modify the requestFilters so that they just get only the
        //data missing in the database.
        //a separate thread must be spanned to get the missing data.

        //first see if there is data in the database for the requested request filters.
        //and build the new RequestFilterSequence.

        /*LocalSeismogramImpl[] seis = hsqlRequestFilterDb.getSeismograms(a_filterseq);
         if(seis.length != 0) {
         a_client.pushData(seis, initiator);
         }

         RequestFilter[] available_seq = hsqlRequestFilterDb.available_data(a_filterseq);
         RequestFilter[] missing_seq = RequestFilterUtil.leftOuterJoin(a_filterseq, available_seq);
         if(missing_seq.length == 0) {
         a_client.finished(initiator);
         return new String();
         }*/
        Thread t = new Thread(dbThreadGroup,
                              new DataCenterThread(a_filterseq,//missing_seq,
                                                   a_client,
                                                   initiator,
                                                   this),
                              "DBDataCenter"+getThreadNum());
        t.start();

        return getNextRequestId();

    }

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
        try {
            ArrayList arrayList = new ArrayList();
            LocalSeismogramImpl[] localSeismograms = hsqlRequestFilterDb.getSeismograms(a_filterseq);
            RequestFilter[] uncovered = notCovered(a_filterseq, localSeismograms);
            if(uncovered.length != 0 && dataCenter != null)  {
                localSeismograms = (LocalSeismogramImpl[])dataCenter.retrieve_seismograms(a_filterseq);
                hsqlRequestFilterDb.addSeismogram(localSeismograms);
            }
            insertIntoArrayList(arrayList, localSeismograms);
            //}
            LocalSeismogram[] rtnValues = new LocalSeismogram[arrayList.size()];
            rtnValues = (LocalSeismogram[]) arrayList.toArray(rtnValues);
            return rtnValues;
        } catch ( SQLException e) {
            throw new edu.iris.Fissures.FissuresException(new edu.iris.Fissures.Error(0,e.toString()));
        } catch ( java.io.IOException e) {
            throw new edu.iris.Fissures.FissuresException(new edu.iris.Fissures.Error(0,e.toString()));
        } catch ( edu.iris.dmc.seedcodec.CodecException e) {
            throw new edu.iris.Fissures.FissuresException(new edu.iris.Fissures.Error(0,e.toString()));
        } // end of catch

    }

    /**
     *@returns an array containing the request filters taken from the <code>filters</code>
     * array that are not completely covered by the given seismograms begin and
     * end.
     *
     */
    public RequestFilter[] notCovered(RequestFilter[] filters,
                                      LocalSeismogramImpl[] seismograms){
        if(seismograms.length == 0){
            return filters;
        }
        LocalSeismogramImpl[] sorted = DisplayUtils.sortByDate(seismograms);
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(sorted[0].getBeginTime(),
                                                                  sorted[sorted.length -1].getEndTime());
        List unsatisfied = new ArrayList();
        for (int i = 0; i < filters.length; i++){
            MicroSecondDate filterBegin = new MicroSecondDate(filters[i].start_time);
            MicroSecondDate filterEnd = new MicroSecondDate(filters[i].end_time);
            if(filterEnd.after(timeRange.getEndTime()) ||
               filterBegin.before(timeRange.getBeginTime())){
                unsatisfied.add(filters[i]);
            }
        }
        return (RequestFilter[])unsatisfied.toArray(new RequestFilter[unsatisfied.size()]);
    }

    private void insertIntoArrayList(ArrayList arrayList, LocalSeismogram[] localSeismograms) {
        for(int counter = 0; counter < localSeismograms.length; counter++) {
            arrayList.add(localSeismograms[counter]);
        }
    }


    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/queue_seismograms:1.0
    //
    /***/

    public String
        queue_seismograms(RequestFilter[] a_filterseq)
        throws edu.iris.Fissures.FissuresException {
        return getNextRequestId();
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_queue:1.0
    //
    /***/

    public LocalSeismogram[]
        retrieve_queue(String a_request)
        throws edu.iris.Fissures.FissuresException {

        return new LocalSeismogram[0];
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/cancel_request:1.0
    //
    /***/

    public void
        cancel_request(String a_request)
        throws edu.iris.Fissures.FissuresException {


    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/request_status:1.0
    //
    /***/

    public String
        request_status(String a_request)
        throws edu.iris.Fissures.FissuresException {

        return new String();
    }

    public String getFileIds(ChannelId channelId,
                             MicroSecondDate beginDate,
                             MicroSecondDate endDate) throws SQLException {
        return hsqlRequestFilterDb.getFileIds(channelId,
                                              beginDate,
                                              endDate);
    }

    public LocalSeismogram getSeismogram(String fileIds)
        throws SQLException, java.io.IOException, edu.iris.Fissures.FissuresException {
        return hsqlRequestFilterDb.getSeismogram(fileIds);
    }

    private DataCenterOperations dataCenter;

    private HSQLRequestFilterDb hsqlRequestFilterDb;

    private static DBDataCenter dbDataCenter;

    /** used to generate unique return ids for requests. May not be used yet,
     but at least the strings will be unique. */
    String getNextRequestId() {
        return "requestId"+requestNumber;
    }

    private int requestNumber = 1;

    private static int threadNum = 0;

    private synchronized static int getThreadNum() {
        return threadNum++;
    }

    private ThreadGroup dbThreadGroup = new ThreadGroup("DBDataCenter");

}// DBDataCenter

