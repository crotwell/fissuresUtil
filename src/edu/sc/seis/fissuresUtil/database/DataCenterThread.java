package edu.sc.seis.fissuresUtil.database;

import edu.sc.seis.fissuresUtil.cache.*;
import edu.sc.seis.fissuresUtil.xml.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.apache.log4j.*;

/**
 * DataCenterThread.java
 *
 *
 * Created: Mon Feb 17 15:12:15 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DataCenterThread implements Runnable{
    public DataCenterThread (RequestFilter[] requestFilters,
			     LocalDataCenterCallBack a_client,
			     SeisDataChangeListener initiator,
			     DataCenterOperations dbDataCenter){
	this.requestFilters = requestFilters;
	this.a_client = a_client;
	this.initiator = initiator;
	this.dbDataCenter = dbDataCenter;
    }

    public void run() {
	for(int counter = 0; counter <  requestFilters.length; counter++) {
	    try {
		RequestFilter[] temp = new RequestFilter[1];
		temp[0] = requestFilters[counter];
		//System.out.println("Making a request to retrieve seismograms");
		LocalSeismogram[] seis = dbDataCenter.retrieve_seismograms(temp);
		LocalSeismogramImpl[] seisImpl = castToLocalSeismogramImplArray(seis);
		//System.out.println("The length of the seismograms in thread is "+seis.length);
		a_client.pushData(seisImpl, initiator);
	    } catch(FissuresException fe) {
		fe.printStackTrace();
		continue;
	    }
	}
	a_client.finished(initiator);

    }

    private LocalSeismogramImpl[] castToLocalSeismogramImplArray(LocalSeismogram[] seismos) {
	LocalSeismogramImpl[] rtnValues = new LocalSeismogramImpl[seismos.length];
	for(int counter = 0; counter < seismos.length; counter++) {
	    rtnValues[counter] = (LocalSeismogramImpl) seismos[counter];
	}
	return rtnValues;
    }

    private RequestFilter[] requestFilters;
    
    private LocalDataCenterCallBack a_client;

    private DataCenterOperations dbDataCenter;

    private SeisDataChangeListener initiator;

    private static Category logger = Category.getInstance(DataCenterThread.class.getName());
    
}// DataCenterThread

