/**
 * RetryDataCenter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.Time;
import org.apache.log4j.Logger;

public class RetryDataCenter implements DataCenterOperations
{
    public RetryDataCenter(DataCenterOperations dc, int retry) {
        this.dc = dc;
        this.retry = retry;
    }


    /***/
    public void cancel_request(String a_request) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                dc.cancel_request(a_request);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /** if long_lived is true then the request is "sticky" in that
     *the client wants the data center to return not just the data
     *that it has in its archive currently, but also any data that it
     *receives up to the  expiration_time. For instance if a station
     *sends its data by mailing tapes, then a researcher could issue
     *a request for data that is expected to be delivered from a
     *recent earthquake, even thought the data center does not yet
     *have the data. Note that expiration_time is ignored if long_lived
     *is false.*/
    public String request_seismograms(RequestFilter[] a_filterseq, DataCenterCallBack a_client, boolean long_lived, Time expiration_time) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.request_seismograms(a_filterseq, a_client, long_lived, expiration_time);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.retrieve_seismograms(a_filterseq);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public LocalSeismogram[] retrieve_queue(String a_request) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.retrieve_queue(a_request);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.available_data(a_filterseq);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public String request_status(String a_request) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.request_status(a_request);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    /***/
    public String queue_seismograms(RequestFilter[] a_filterseq) throws FissuresException {
        int count = 0;
        RuntimeException lastException = null;
        while (count < retry) {
            try {
                return dc.queue_seismograms(a_filterseq);
            } catch (RuntimeException t) {
                lastException = t;
                logger.warn("Caught exception, retrying "+count, t);
            }
            count++;
        }
        throw lastException;
    }

    DataCenterOperations dc;

    int retry;

    Logger logger = Logger.getLogger(RetryDataCenter.class);

}

