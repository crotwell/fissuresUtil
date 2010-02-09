/**
 * ProxyNetworkDC.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;

public abstract class AbstractProxySeismogramDC implements ProxySeismogramDC {

    public AbstractProxySeismogramDC() {
        this(null);
    }

    public AbstractProxySeismogramDC(DataCenterOperations seisDC) {
        this.seisDC = seisDC;
    }

    public DataCenterOperations getWrappedDC() {
        return seisDC;
    }

    public DataCenterOperations getWrappedDC(Class wrappedClass) {
        if(this.getClass().isAssignableFrom(wrappedClass)) {
            return this;
        }
        DataCenterOperations tmp = getWrappedDC();
        if(tmp instanceof ProxySeismogramDC) {
            return ((ProxySeismogramDC)tmp).getWrappedDC(wrappedClass);
        }
        throw new IllegalArgumentException("Can't find class "
                + wrappedClass.getName());
    }

    public void reset() {
        if(seisDC instanceof ProxySeismogramDC) {
            ((ProxySeismogramDC)seisDC).reset();
        }
    }

    public org.omg.CORBA.Object getCorbaObject() {
        if(seisDC instanceof DataCenter) {
            return (DataCenter)seisDC;
        } else {
            return ((ProxySeismogramDC)seisDC).getCorbaObject();
        }
    }

    public String getServerName() {
        if(seisDC instanceof ProxySeismogramDC) {
            return ((ProxySeismogramDC)seisDC).getServerName();
        }
        return null;
    }

    public String getServerType() {
        return SEISDC_TYPE;
    }

    public String getServerDNS() {
        if(seisDC instanceof ProxySeismogramDC) {
            return ((ProxySeismogramDC)seisDC).getServerDNS();
        }
        return null;
    }
    
    public String getFullName(){
        return getServerDNS() + "/" + getServerName();
    }

    protected DataCenterOperations seisDC;
}