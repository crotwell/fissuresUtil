/**
 * ProxyNetworkDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkDCOperations;

public interface ProxyNetworkDC extends NetworkDCOperations {

    /**
     * Returns the DataCenterOperations directly inside of this one
     */
    public NetworkDCOperations getWrappedDC();

    /**
     * Traverses through all of the ProxyNetworkDCs contained by this one, or
     * the one it contains and if it finds one of the passed in class, returns
     * it. If there isn't one, it throws IllegalArgumentException
     */
    public NetworkDCOperations getWrappedDC(Class wrappedClass);

    /**
     * Resets the proxy, potentially removing any cached data and reresolving
     * the corba reference.
     */
    public void reset();

    /**
     * Gets the real corba object for which this is a proxy.
     */
    public org.omg.CORBA.Object getCorbaObject();
}