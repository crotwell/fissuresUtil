package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import java.sql.SQLException;
import java.util.*;
import org.apache.log4j.*;

/**
 * DataSetSeismogram.java
 *
 *
 * Created: Tue Feb 11 10:08:37 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DCDataSetSeismogram 
    extends DataSetSeismogram 
    implements LocalDataCenterCallBack, Cloneable 
{


    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco) {
        this(rf, dco, null);
    }

    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco,
                               DataSet ds) {
        this(rf, dco, ds, null);
    }

    public DCDataSetSeismogram(RequestFilter rf,
                               DataCenterOperations dco,
                               DataSet ds,
                               String name) {
        super(ds, name);
        this.requestFilter = rf;
        this.dataCenterOps = dco;
    }

    public void retrieveData(SeisDataChangeListener dataListener) 
    {

        RequestFilter[] temp = new RequestFilter[1];
        temp[0] = requestFilter;
        try {
            if(this.dataCenterOps instanceof DBDataCenter) {

                ((DBDataCenter)this.dataCenterOps).request_seismograms(temp,
                                                                       (LocalDataCenterCallBack)this,
                                                                       dataListener,
                                                                       false,
                                                                       new MicroSecondDate().getFissuresTime());

            } else {
                /*
                DBDataCenter.getDataCenter(this.dataCenterOps).request_seismograms(temp,
                                                                                   (LocalDataCenterCallBack)this,
                                                                                   dataListener,
                                                                                   false,
                                                                                   new MicroSecondDate().getFissuresTime());
                */
            }
        } catch(FissuresException fe) {
            //          throw new DataRetrievalException("Exception occurred while using DataCenter to get Data",fe);
            //       } catch(SQLException fe) {
            //    throw new DataRetrievalException("Exception occurred while using DataCenter to get Data",fe);
        }
    }

    private DataCenterOperations dataCenterOps;

    static Category logger =
        Category.getInstance(DCDataSetSeismogram.class.getName());



}// DataSetSeismogram
