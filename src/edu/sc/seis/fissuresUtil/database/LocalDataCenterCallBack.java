package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.sc.seis.fissuresUtil.xml.*;

/**
 * LocalDataCenterCallBack.java
 *
 *
 * Created: Wed Feb 19 14:53:26 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface LocalDataCenterCallBack {

    public void pushData(LocalSeismogram[] seis, SeisDataChangeListener initiator);
    
    public void finished(SeisDataChangeListener dss);
    
}// LocalDataCenterCallBack
