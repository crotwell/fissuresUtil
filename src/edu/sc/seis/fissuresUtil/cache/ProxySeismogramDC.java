/**
 * ProxySeismogramDC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;

public interface ProxySeismogramDC extends DataCenterOperations {

    /** Resets the proxy, potentially removing any cached data and
     *  reresolving the corba reference. */
    public void reset();

    /** Gets the real corba DataCenter object for which this is a proxy.
     */
    public DataCenter getCorbaObject();
}

