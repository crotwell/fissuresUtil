
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;

/**
 * SeismogramQueueElement.java
 *
 *
 * Created: Mon Mar  5 21:02:21 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class SeismogramQueueElement {
    
    public SeismogramQueueElement(DataCenterOperations seisDC,
	                          RequestFilter request,
			     SeismogramLoadedListener listener) {
	this.seisDC = seisDC;
        this.request = request;
	this.listener = listener;
    }

    SeismogramLoadedListener getListener() {
	return listener;
    }

    RequestFilter getRequest() {
	return request;
    }

    DataCenterOperations getDataCenter() {
	return seisDC;
    }

    DataCenterOperations seisDC;
    RequestFilter request;
    LocalSeismogram cache;
    SeismogramLoadedListener listener;

} // SeismogramQueueElement
