/**
 * ProxyPlottableDC.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableDCOperations;

public interface ProxyPlottableDC extends PlottableDCOperations
{

    /**
     * Returns the DataCenterOperations directly inside of this one
     */
    public PlottableDCOperations getWrappedDC();

    /**
     * Traverses through all of the ProxySeismogramDCs contained by this one, or
     * the one it contains and if it finds one of the passed in class, returns
     * it.  If there isn't one, it throws IllegalArgumentException
     */
    public PlottableDCOperations getWrappedDC(Class wrappedClass);

    /** Resets the proxy, potentially removing any cached data and
     *  reresolving the corba reference. */
    public void reset();

    /** Gets the real corba DataCenter object for which this is a proxy.
     */
    public PlottableDC getCorbaObject();
}

