/**
 * ProxySeismogramDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;

public interface ProxySeismogramDC extends DataCenterOperations {

    /**
     * Returns the DataCenterOperations directly inside of this one
     */
    public DataCenterOperations getWrappedDC();

    /**
     * Traverses through all of the ProxySeismogramDCs contained by this one, or
     * the one it contains and if it finds one of the passed in class, returns
     * it. If there isn't one, it throws IllegalArgumentException
     */
    public DataCenterOperations getWrappedDC(Class wrappedClass);

    /**
     * Resets the proxy, potentially removing any cached data and reresolving
     * the corba reference.
     */
    public void reset();

    /**
     * Gets the real corba DataCenter object for which this is a proxy.
     */
    public org.omg.CORBA.Object getCorbaObject();
}