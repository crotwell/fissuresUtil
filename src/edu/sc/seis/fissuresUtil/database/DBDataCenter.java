package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.model.*;

import edu.sc.seis.fissuresUtil.cache.*;
import edu.sc.seis.fissuresUtil.xml.*;


import java.util.*;

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
    private DBDataCenter (DataCenterOperations dataCenterRouter){
	this.dataCenterRouter = dataCenterRouter;
	hsqlRequestFilterDb = new HSQLRequestFilterDb(dataCenterRouter);
    }
    
    private DBDataCenter () {

	hsqlRequestFilterDb = new HSQLRequestFilterDb();
    }

    public static DBDataCenter getDataCenter(DataCenterOperations dataCenterRouter) {
	if(dbDataCenter == null) {
	    dbDataCenter = new DBDataCenter(dataCenterRouter);
 	}
	return dbDataCenter;
    }

    public static DBDataCenter getDataCenter() {
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
	return new RequestFilter[0];
    }
     public String
	request_seismograms(RequestFilter[] a_filterseq,
                        DataCenterCallBack a_client,
                        boolean long_lived,
                        edu.iris.Fissures.Time expiration_time)
        throws edu.iris.Fissures.FissuresException {
	 return new String();
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

	//RequestFilter param_sequence[] = new RequestFilter[1];
	//	param_sequence[0] = a_filterseq[counter];
	LocalSeismogramImpl[] seis = hsqlRequestFilterDb.getSeismograms(a_filterseq);
	if(seis.length != 0) {
	    a_client.pushData(seis, initiator);
	}

	////System.out.println("Before finding available data");
	RequestFilter[] available_seq = hsqlRequestFilterDb.available_data(a_filterseq);
// 	if(available_seq.length != 0) {
// 	    LocalSeismogram[] seist = hsqlRequestFilterDb.getSeismograms(available_seq);
// 	    a_client.pushData(seist, initiator);
// 	}
	////System.out.println("After available data");
	RequestFilter[] missing_seq = RequestFilterUtil.leftOuterJoin(a_filterseq, available_seq);
	if(missing_seq.length == 0) {
	    a_client.finished(initiator);
	    return new String();
	}
	Thread t = new Thread(dbThreadGroup,
                          new DataCenterThread(missing_seq,
                                               a_client,
                                               initiator,
                                               this),
                          "DBDataCenter"+getThreadNum());
	t.start();
	
	
	return new String();
	
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_seismograms:1.0
    //
    /***/

    public synchronized LocalSeismogram[]
    retrieve_seismograms(RequestFilter[] a_filterseq)
        throws edu.iris.Fissures.FissuresException {
	//here first check with the database to see if
	//if the seismogram is already in the cache.
	//if it is the case .. return it..
	//else use the dataCenter Router to get the seismograms.
	//LocalSeismogram[] localSeismograms = new LocalSeismogram[a_filterseq.length];
	ArrayList arrayList = new ArrayList();
	//	for(int counter = 0; counter < a_filterseq.length; counter++) {
	LocalSeismogramImpl[] localSeismograms = hsqlRequestFilterDb.getSeismograms(a_filterseq);
	if(localSeismograms.length == 0 && dataCenterRouter != null)  {
	    localSeismograms = (LocalSeismogramImpl[])dataCenterRouter.retrieve_seismograms(a_filterseq);
	    hsqlRequestFilterDb.addSeismogram(localSeismograms);
	}
	insertIntoArrayList(arrayList, localSeismograms);
	    //}
	LocalSeismogram[] rtnValues = new LocalSeismogram[arrayList.size()];
	rtnValues = (LocalSeismogram[]) arrayList.toArray(rtnValues);
	return rtnValues;
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
	return new String();
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
			     MicroSecondDate endDate) {
	return hsqlRequestFilterDb.getFileIds(channelId,
				      beginDate,
				      endDate);
    }

    public LocalSeismogram getSeismogram(String fileIds) {
	return hsqlRequestFilterDb.getSeismogram(fileIds);
    }

    private DataCenterOperations dataCenterRouter;

    private HSQLRequestFilterDb hsqlRequestFilterDb;

    private static DBDataCenter dbDataCenter;

    
    private static int threadNum = 0;

    private synchronized static int getThreadNum() {
        return threadNum++;
    }

    private ThreadGroup dbThreadGroup = new ThreadGroup("DBDataCenter");

}// DBDataCenter

