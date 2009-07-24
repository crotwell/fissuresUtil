package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * This class is used as a callback object in LocalDCOperations. The class
 * that intends to receive callback calls must implement this interface.
 *
 * LocalDataCenterCallBack.java
 *
 *
 * Created: Wed Feb 19 14:53:26 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface LocalDataCenterCallBack {

    /**
     * used to push information about the seismograms to the callback object.
     * @param seis - an array of LocalSeismogramImpl
     * @param initiator - the callback object that initiated the request for seismograms.
     */
    public void pushData(LocalSeismogramImpl[] seis, SeisDataChangeListener initiator);

    /**
     * Informs the callback object that it is done with the processing
     * of its request for seismograms.
     * @param dss - the callback object that initiated the request for seismograms.
     */
    public void finished(SeisDataChangeListener dss);

    /**
     * Informs the callback object that it is done with the processing
     * of its request for seismograms because it encountered a problem.
     * @param dss - the callback object that initiated the request for seismograms.
     * @param e - the exception explaining the error encountered.
     */
    public void error(SeisDataChangeListener dss, Throwable e);

}// LocalDataCenterCallBack
