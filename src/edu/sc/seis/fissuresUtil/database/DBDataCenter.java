package edu.sc.seis.fissuresUtil.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * DBDataCenter.java
 * 
 * 
 * Created: Tue Feb 4 10:55:16 2003
 * 
 * @author <a href="mailto:"> Srinivasa Telukutla </a>
 * @version
 */

public class DBDataCenter implements DataCenterOperations, LocalDCOperations {
    private DBDataCenter(String directoryName, String databaseName,
            DataCenterOperations dataCenterRouter) throws SQLException {
        this.dataCenter = dataCenterRouter;
        hsqlRequestFilterDb = new HSQLRequestFilterDb(directoryName,
                databaseName, dataCenterRouter);
    }

    private DBDataCenter(String directoryName, String databaseName)
            throws SQLException {

        hsqlRequestFilterDb = new HSQLRequestFilterDb(directoryName,
                databaseName);
    }

    public static DBDataCenter getDataCenter(String directoryName,
            String databaseName, DataCenterOperations dataCenterRouter)
            throws SQLException {
        if (dbDataCenter == null) {
            dbDataCenter = new DBDataCenter(directoryName, databaseName,
                    dataCenterRouter);
        }
        return dbDataCenter;
    }

    public static DBDataCenter getDataCenter(String directoryName,
            String databaseName) throws SQLException {
        if (dbDataCenter == null) {
            dbDataCenter = new DBDataCenter(directoryName, databaseName);
        }
        return dbDataCenter;
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/available_data:1.0
    //
    /***/

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        return dataCenter.available_data(a_filterseq);
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
            DataCenterCallBack a_client, boolean long_lived,
            edu.iris.Fissures.Time expiration_time)
            throws edu.iris.Fissures.FissuresException {
        return getNextRequestId();
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/request_seismograms:1.0
    //
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
            LocalDataCenterCallBack a_client, SeisDataChangeListener initiator,
            boolean long_lived, edu.iris.Fissures.Time expiration_time) {
        DataCenterThread dcThread = (DataCenterThread) clientToThread.get(a_client);
        if (dcThread == null || !dcThread.getData(initiator, a_filterseq)) {
            dcThread = new DataCenterThread(a_filterseq,//missing_seq,
                    a_client, initiator, this);
            clientToThread.put(a_client, dcThread);
            dcThreadPool.invokeLater(dcThread);
            DataCenterThread.incrementWaiters();
        }
        return getNextRequestId();
    }

    private WorkerThreadPool dcThreadPool = new WorkerThreadPool(
            "DBDataCenter", 5);

    private Map clientToThread = new HashMap();

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_seismograms:1.0
    //
    /***/

    public synchronized LocalSeismogram[] retrieve_seismograms(
            RequestFilter[] a_filterseq)
            throws edu.iris.Fissures.FissuresException {

        //here first check with the database to see if
        //if the seismogram is already in the cache.
        //if it is the case .. return it..
        //else use the dataCenter Router to get the seismograms.
        try {
            LocalSeismogramImpl[] localSeismograms = hsqlRequestFilterDb.getSeismograms(a_filterseq);
            RequestFilter[] uncovered = notCovered(a_filterseq,
                    localSeismograms);
            if (uncovered.length == 0) return localSeismograms;
            List seisToReturn = new ArrayList(localSeismograms.length);
            insertIntoList(seisToReturn, localSeismograms);
            if (dataCenter != null) {
                localSeismograms = (LocalSeismogramImpl[]) dataCenter.retrieve_seismograms(uncovered);
                hsqlRequestFilterDb.addSeismogram(localSeismograms);
            }
            if (localSeismograms.length > 0)
                    insertIntoList(seisToReturn, localSeismograms);
            LocalSeismogram[] rtnValues = new LocalSeismogram[seisToReturn.size()];
            rtnValues = (LocalSeismogram[]) seisToReturn.toArray(rtnValues);
            return rtnValues;
        } catch (SQLException e) {
            throw new edu.iris.Fissures.FissuresException(
                    new edu.iris.Fissures.Error(0, e.toString()));
        } catch (java.io.IOException e) {
            throw new edu.iris.Fissures.FissuresException(
                    new edu.iris.Fissures.Error(0, e.toString()));
        } catch (edu.iris.dmc.seedcodec.CodecException e) {
            throw new edu.iris.Fissures.FissuresException(
                    new edu.iris.Fissures.Error(0, e.toString()));
        } // end of catch

    }

    /**
     * @returns an array containing the request filters taken from the
     *          <code>filters</code> array that are not completely covered by
     *          the given seismograms begin and end.
     *  
     */
    public static RequestFilter[] notCovered(RequestFilter[] neededFilters,
            LocalSeismogramImpl[] existingFilters) {
        if (existingFilters.length == 0) { return neededFilters; }
        LocalSeismogramImpl[] sorted = DisplayUtils.sortByDate(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i].getBeginTime(),
                    sorted[i].getEndTime());
        }
        return notCovered(neededFilters, ranges);
    }

    public static RequestFilter[] notCovered(RequestFilter[] existingFilters,
            RequestFilter[] neededFilters) {
        if (existingFilters.length == 0) { return neededFilters; }
        RequestFilter[] sorted = DisplayUtils.sortByDate(existingFilters);
        MicroSecondTimeRange[] ranges = new MicroSecondTimeRange[sorted.length];
        for (int i = 0; i < sorted.length; i++) {
            ranges[i] = new MicroSecondTimeRange(sorted[i]);
        }
        return notCovered(neededFilters, ranges);

    }

    public static RequestFilter[] notCovered(RequestFilter[] filters,
            MicroSecondTimeRange[] timeRanges) {
        List unsatisfied = new ArrayList();
        for (int i = 0; i < filters.length; i++) {
            boolean beginCovered = false;
            boolean endCovered = false;
            MicroSecondDate filterBegin = new MicroSecondDate(
                    filters[i].start_time);
            MicroSecondDate filterEnd = new MicroSecondDate(filters[i].end_time);
            for (int j = 0; j < timeRanges.length
                    && !(beginCovered && endCovered); j++) {
                MicroSecondDate timeBegin = timeRanges[j].getBeginTime();
                MicroSecondDate timeEnd = timeRanges[j].getEndTime();
                if ((filterBegin.after(timeBegin) && filterBegin.before(timeEnd))
                        || filterBegin.equals(timeBegin)) {
                    beginCovered = true;
                }
                if ((filterEnd.before(timeEnd) && timeBegin.before(filterEnd))
                        || filterEnd.equals(timeEnd)) {
                    endCovered = true;
                }
            }
            if (!beginCovered && !endCovered) {
                unsatisfied.add(filters[i]);
            } else if (!beginCovered) {
                unsatisfied.add(new RequestFilter(filters[i].channel_id,
                        filters[i].start_time,
                        timeRanges[0].getBeginTime().getFissuresTime()));
            } else if (!endCovered) {
                unsatisfied.add(new RequestFilter(
                        filters[i].channel_id,
                        timeRanges[timeRanges.length - 1].getEndTime().getFissuresTime(),
                        filters[i].end_time));
            }
        }
        return (RequestFilter[]) unsatisfied.toArray(new RequestFilter[unsatisfied.size()]);
    }

    private void insertIntoList(List list, LocalSeismogram[] localSeismograms) {
        for (int counter = 0; counter < localSeismograms.length; counter++) {
            list.add(localSeismograms[counter]);
        }
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/queue_seismograms:1.0
    //
    /***/

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws edu.iris.Fissures.FissuresException {
        return getNextRequestId();
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_queue:1.0
    //
    /***/

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws edu.iris.Fissures.FissuresException {

        return new LocalSeismogram[0];
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/cancel_request:1.0
    //
    /***/

    public void cancel_request(String a_request)
            throws edu.iris.Fissures.FissuresException {

    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/request_status:1.0
    //
    /***/

    public String request_status(String a_request)
            throws edu.iris.Fissures.FissuresException {

        return new String();
    }

    public String getFileIds(ChannelId channelId, MicroSecondDate beginDate,
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

    private static Logger logger = Logger.getLogger(DBDataCenter.class);
}// DBDataCenter
