package edu.sc.seis.fissuresUtil.database;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Category;

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
        synchronized(initiators){
            initiators.add(initiator);
        }
        this.dbDataCenter = dbDataCenter;
    }

    public void run() {
        List seismograms = new ArrayList();
        for(int counter = 0; counter <  requestFilters.length; counter++) {
            try {
                RequestFilter[] temp = { requestFilters[counter] };
                LocalSeismogram[] seis = dbDataCenter.retrieve_seismograms(temp);
                LocalSeismogramImpl[] seisImpl = castToLocalSeismogramImplArray(seis);
                synchronized(initiators){
                    pushed = true;
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.pushData(seisImpl, ((SeisDataChangeListener)it.next()));
                    }
                }
                for (int i = 0; i < seisImpl.length; i++){
                    seismograms.add(seisImpl[i]);
                }
            } catch(FissuresException fe) {
                synchronized(initiators){
                    pushed = true;
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.error(((SeisDataChangeListener)it.next()), fe);
                    }
                    continue;
                }
            } catch(org.omg.CORBA.SystemException fe) {
                synchronized(initiators){
                    pushed = true;
                    Iterator it = initiators.iterator();
                    while(it.hasNext()){
                        a_client.error(((SeisDataChangeListener)it.next()), fe);
                    }
                    continue;
                }
            }
        }
        LocalSeismogramImpl[] seisArray = new LocalSeismogramImpl[seismograms.size()];
        seisRef = new SoftReference(seismograms.toArray(seisArray));
        synchronized(initiators){
            Iterator it = initiators.iterator();
            while(it.hasNext()){
                a_client.finished(((SeisDataChangeListener)it.next()));
            }
        }
    }

    public boolean getData(SeisDataChangeListener listener,
                           RequestFilter[] requestFilters){
        for (int i = 0; i < requestFilters.length; i++){
            boolean found = false;
            for (int j = 0; j < this.requestFilters.length && !found; j++){
                if(requestFilters[i] == this.requestFilters[j]){
                    found = true;
                }
            }
            if(!found){
                return false;
            }
        }
        if(!pushed){
            synchronized(initiators){
                initiators.add(listener);
            }
            return true;
        }
        LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])seisRef.get();
        if(seis != null){
            a_client.pushData(seis, listener);
            a_client.finished(listener);
            return true;
        }else{
            return false;
        }
    }

    private LocalSeismogramImpl[] castToLocalSeismogramImplArray(LocalSeismogram[] seismos) {
        LocalSeismogramImpl[] rtnValues = new LocalSeismogramImpl[seismos.length];
        for(int counter = 0; counter < seismos.length; counter++) {
            rtnValues[counter] = (LocalSeismogramImpl) seismos[counter];
        }
        return rtnValues;
    }

    private SoftReference seisRef;

    private RequestFilter[] requestFilters;

    private LocalDataCenterCallBack a_client;

    private DataCenterOperations dbDataCenter;

    private List initiators = Collections.synchronizedList(new ArrayList());

    private static Category logger = Category.getInstance(DataCenterThread.class.getName());

    private boolean pushed = false;

}// DataCenterThread

