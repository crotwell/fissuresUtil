package edu.sc.seis.fissuresUtil.xml;
import edu.sc.seis.fissuresUtil.database.*;

import edu.iris.Fissures.IfSeismogramDC.*;

/**
 * DSSDataListener.java
 *
 *
 * Created: Tue Feb 11 10:13:15 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface SeisDataChangeListener {
    public void pushData(SeisDataChangeEvent sdce);
    
    public void error(SeisDataErrorEvent sdce);

    public void finished(SeisDataChangeEvent sdce);

}// DSSDataListener
