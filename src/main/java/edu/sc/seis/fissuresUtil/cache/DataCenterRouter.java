package edu.sc.seis.fissuresUtil.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 *  Routes data center requests to the correct datacenter. This allow the
 *  calling object to deal with multiple datacenters without keeping track
 *  of which networks come from which datacenters. The first datacenter to
 *  return data for a particular request is assumed to have satisfied the
 *  request. But if no data is returned, then the request propigates to the
 *  next datacenter.
 *
 *
 * Created: Wed Jan 22 15:24:39 2003
 *
 * @author <a href="mailto:crotwell@Philip-Crotwells-Computer.local.">Philip Crotwell</a>
 * @version 1.0
 */
public class DataCenterRouter implements DataCenterOperations {
    public void addDataCenter(ProxySeismogramDC dc) {
        unmatchedDCList.add(dc);
    }

    public void addDataCenter(NetworkAccess[] net, ProxySeismogramDC dc) {
        for ( int i=0; i<net.length; i++) {
            addDataCenter(net[i].get_attributes().get_id(), dc);
        } // end of for ()
    }

    public void addDataCenter(NetworkAccess net, ProxySeismogramDC dc) {
        addDataCenter(net.get_attributes().get_id(), dc);
    }

    public void addDataCenter(NetworkId networkId,  ProxySeismogramDC dc) {
        addDataCenter(networkId.network_code, dc);
    }

    public void addDataCenter(String network_code,  ProxySeismogramDC dc) {
        logger.debug("adding "+network_code);
        List dcList = (List)netToDCMap.get(network_code);
        if ( dcList == null) {
            dcList = new LinkedList();
            netToDCMap.put(network_code, dcList);
        } // end of if ()

        if ( ! dcList.contains(dc)) {
            dcList.add(dc);
        }

        Iterator it = netToDCMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            dcList = (List)netToDCMap.get(key);
            logger.debug("netToDCMap: "+key+" size is "+dcList.size());
        }
    }

    public List getDataCenter(NetworkAccess net) {
        return getDataCenter(net.get_attributes().get_code());
    }

    public List getDataCenter(NetworkId networkId) {
        return getDataCenter(networkId.network_code);
    }

    public List getDataCenter(String networkCode) {
        return (List)netToDCMap.get(networkCode);
    }

    public List getDataCenter(ChannelId chanId) {
        return getDataCenter(chanId.network_id);
    }

    public RequestFilter[] available_data(RequestFilter[] filters) {
        HashMap datacenterMap = makeMap(filters);
        LinkedList out = new LinkedList();
        if (datacenterMap.keySet().size() == 0) {
            String s = "No datacenters found for request: ";
            for (int i = 0; i < filters.length; i++) {
                s+= " "+ChannelIdUtil.toStringNoDates(filters[i].channel_id)+
                    filters[i].start_time.date_time+
                    " to "+filters[i].end_time.date_time+", ";
            }
            logger.warn(s);
        }

        Iterator it = datacenterMap.keySet().iterator();
        while ( it.hasNext()) {
            List dcList = (List)it.next();
            Iterator dcIt = dcList.iterator();
            List dcFilters =
                (List)datacenterMap.get(dcList);
            while ( dcIt.hasNext()) {
                ProxySeismogramDC dc = (ProxySeismogramDC)dcIt.next();
                RequestFilter[] tempRF =
                    dc.available_data((RequestFilter[])dcFilters.toArray(new RequestFilter[0]));
                for ( int i=0; i<tempRF.length; i++) {
                    out.add(tempRF[i]);
                } // end of for ()
            } // end of while ()
        } // end of while ()

        return (RequestFilter[])out.toArray(new RequestFilter[0]);
    }

    public String request_seismograms(RequestFilter[] filters,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      edu.iris.Fissures.Time expiration_time)
        throws edu.iris.Fissures.FissuresException {
        return null;
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] filters)
        throws edu.iris.Fissures.FissuresException {
        HashMap datacenterMap = makeMap(filters);
        LinkedList out = new LinkedList();
        Iterator it = datacenterMap.keySet().iterator();
        while ( it.hasNext()) {
            List dcList = (List)it.next();
            Iterator dcIt = dcList.iterator();
            List dcFilters =
                (List)datacenterMap.get(dcList);
            while ( dcIt.hasNext()) {
                ProxySeismogramDC dc = (ProxySeismogramDC)dcIt.next();
                LocalSeismogram[] tempSeis =
                    dc.retrieve_seismograms((RequestFilter[])dcFilters.toArray(new RequestFilter[0]));
                for ( int i=0; i<tempSeis.length; i++) {
                    out.add(tempSeis[i]);
                } // end of for ()

                if ( tempSeis.length != 0) {
                    // got some data, so finish, don't make same request to
                    // another datacenter
                    break;
                } // end of if ()

            } // end of while ()

        } // end of while ()

        return (LocalSeismogramImpl[])out.toArray(new LocalSeismogramImpl[0]);
    }


    public String queue_seismograms(RequestFilter[] a_filterseq)
        throws edu.iris.Fissures.FissuresException {
        return null;
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
        throws edu.iris.Fissures.FissuresException {
        return null;
    }

    public void cancel_request(String a_request)
        throws edu.iris.Fissures.FissuresException {

    }

    public String request_status(String a_request)
        throws edu.iris.Fissures.FissuresException {
        return null;
    }

    /**
     * Sorts the request filters into Lists indexed by the list of datacenters
     * that support each networkId. So the keys of the map are Lists of
     * DataCenters, all of which support a given network. And the values of
     * the map is a List of RequestFilters that are for the given network.
     */
    protected HashMap makeMap(RequestFilter[] filters) {
        HashMap datacenterMap = new HashMap();
        for ( int i=0; i<filters.length; i++) {
            List datacenters = getDataCenter(filters[i].channel_id);
            if ( datacenters == null) {
                // unknow network
                logger.debug("Unknown network, no datacenter configured for "+
                                 ChannelIdUtil.toString(filters[i].channel_id));
                datacenters = new LinkedList();
                datacenters.addAll(unmatchedDCList);
                continue;
            } // end of if ()

            List dcFilters =
                (List)datacenterMap.get(datacenters);
            if ( dcFilters == null) {
                dcFilters = new LinkedList();
                datacenterMap.put(getDataCenter(filters[i].channel_id),
                                  dcFilters);
            } // end of if ()
            dcFilters.add(filters[i]);
        } // end of for ()
        return datacenterMap;
    }

    protected HashMap netToDCMap = new HashMap();

    protected List unmatchedDCList = new LinkedList();

    private static Logger logger = LoggerFactory.getLogger(DataCenterRouter.class.getName());
} // DataCenterRouter
