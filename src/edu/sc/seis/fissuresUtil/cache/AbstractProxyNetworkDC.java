/**
 * ProxyNetworkDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.NetworkDC;
import edu.iris.Fissures.IfNetwork.NetworkDCOperations;

public abstract class AbstractProxyNetworkDC implements ProxyNetworkDC {

    public AbstractProxyNetworkDC(NetworkDCOperations netDC) {
        this.netDC = netDC;
    }

    public NetworkDCOperations getWrappedDC() {
        return netDC;
    }

    public NetworkDCOperations getWrappedDC(Class wrappedClass) {
        if(this.getClass().isAssignableFrom(wrappedClass)) {
            return this;
        } else {
            NetworkDCOperations tmp = getWrappedDC();
            if(tmp instanceof ProxyNetworkDC) { return ((ProxyNetworkDC)tmp).getWrappedDC(wrappedClass); }
        }
        throw new IllegalArgumentException("Can't find class "
                + wrappedClass.getName());
    }

    public void reset() {
        if(netDC instanceof ProxyNetworkDC) {
            ((ProxyNetworkDC)netDC).reset();
        }
    }

    public org.omg.CORBA.Object getCorbaObject() {
        if(netDC instanceof NetworkDC) {
            return (NetworkDC)netDC;
        } else {
            return ((ProxyNetworkDC)netDC).getCorbaObject();
        }
    }

    protected NetworkDCOperations netDC;
}