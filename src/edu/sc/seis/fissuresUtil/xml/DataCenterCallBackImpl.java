package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBackPOA;
import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

/**
 * DataCenterCallBackImpl.java
 *
 *
 * Created: Tue Feb 11 11:27:41 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataCenterCallBackImpl extends DataCenterCallBackPOA{
    public DataCenterCallBackImpl (){
	
    }
   
    
    public void setCallBack(Object obj) {
	this.callBackObject = obj;
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenterCallBack/return_seismograms:1.0
    //
    /***/

    public void
    return_seismograms(String the_request,
                       LocalSeismogram[] seismograms) {
	
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenterCallBack/return_group:1.0
    //
    /***/

    public void
    return_group(String the_request,
                 LocalMotionVector[] seismograms) {

	
    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenterCallBack/return_error:1.0
    //
    /***/

    public void
    return_error(String the_request,
                 edu.iris.Fissures.Error the_error,
                 RequestFilter[] filters) {

    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenterCallBack/finished:1.0
    //
    /***/

    public void
	finished(String the_request) {

    }

    //
    // IDL:iris.edu/Fissures/IfSeismogramDC/DataCenterCallBack/canceled:1.0
    //
    /***/

    public void
	canceled(String the_request) {

    }

    private Object callBackObject;

    
}// DataCenterCallBackImpl
