package edu.sc.seis.fissuresUtil.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author groves Created on Feb 15, 2005
 */
public class EarthquakeStationIterator implements Iterator {

    public EarthquakeStationIterator(DataSet ds) {
        buildIterationSequence(ds, ds.getEvent());
        evIterator = evs.iterator();
    }

    private void buildIterationSequence(DataSet ds, EventAccessOperations ev) {
        String[] names = ds.getDataSetSeismogramNames();
        if(names.length > 0) {
            Map stations = null;
            if(ds.getEvent() != null) {
                stations = updateMap(ds.getEvent());
            } else if(ev == null) {
                stations = updateMap("No Earthquake");
            }
            for(int i = 0; i < names.length; i++) {
                DataSetSeismogram seis = ds.getDataSetSeismogram(names[i]);
                String sta = seis.getRequestFilter().channel_id.station_code;
                if(!stations.containsKey(sta)) {
                    stations.put(sta, new ArrayList());
                }
                ((List)stations.get(sta)).add(seis);
            }
        }
        names = ds.getDataSetNames();
        if (ds.getEvent() != null){
            ev = ds.getEvent();
        }
        for(int i = 0; i < names.length; i++) {
            buildIterationSequence(ds.getDataSet(names[i]), ev);
        }
    }

    private Map updateMap(Object key) {
        if(!eqToStationMaps.containsKey(key)) {
            eqToStationMaps.put(key, new HashMap());
            evs.add(key);
        }
        return (Map)eqToStationMaps.get(key);
    }

    public void remove() {
        throw new UnsupportedOperationException("Can't remove from the seismograms using the iterator");
    }

    public boolean hasNext() {
        return evIterator.hasNext() || hasNext(curEvIterator);
    }

    public Object next() {
        if(hasNext(curEvIterator)) {
            return curEvIterator.next();
        } else if(evIterator.hasNext()) {
            curEvIterator = ((Map)eqToStationMaps.get(evIterator.next())).values()
                    .iterator();
            return next();
        }
        throw new IllegalStateException("Iterator is done!  Call hasNext before you call next!");
    }

    private boolean hasNext(Iterator it) {
        return it != null && it.hasNext();
    }

    private Iterator evIterator, curEvIterator;

    private Map eqToStationMaps = new HashMap();

    private List evs = new ArrayList();
}