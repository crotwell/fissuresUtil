package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * This class is Similar to DCOperations. This class differs from the DCOperations in the signature of the method
 * request_seismograms which takes a callback Object. This interface makes use of a LocalDataCenterCallBack object for
 * callback whereas DCOperations uses CORBA Object as a CallBackObject.
 * LocalDCOperations.java
 *
 *
 * Created: Wed Feb 19 14:40:22 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface LocalDCOperations {
    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/available_data:1.0
    //
    /***/

    RequestFilter[]
    available_data(RequestFilter[] a_filterseq);

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

    String
    request_seismograms(RequestFilter[] a_filterseq,
                        LocalDataCenterCallBack a_client,
                        SeisDataChangeListener initiator,
                        boolean long_lived,
                        edu.iris.Fissures.Time expiration_time)
        throws edu.iris.Fissures.FissuresException;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_seismograms:1.0
    //
    /***/

    LocalSeismogram[]
    retrieve_seismograms(RequestFilter[] a_filterseq)
        throws edu.iris.Fissures.FissuresException;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/queue_seismograms:1.0
    //
    /***/

    String
    queue_seismograms(RequestFilter[] a_filterseq)
        throws edu.iris.Fissures.FissuresException;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/retrieve_queue:1.0
    //
    /***/

    LocalSeismogram[]
    retrieve_queue(String a_request)
        throws edu.iris.Fissures.FissuresException;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/cancel_request:1.0
    //
    /***/

    void
    cancel_request(String a_request)
        throws edu.iris.Fissures.FissuresException;

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenter/request_status:1.0
    //
    /***/

    String
    request_status(String a_request)
        throws edu.iris.Fissures.FissuresException;
    
}// LocalDCOperations
