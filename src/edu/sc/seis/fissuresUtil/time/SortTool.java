package edu.sc.seis.fissuresUtil.time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


/**
 * @author groves
 * Created on Oct 28, 2004
 */
public class SortTool {

    /**
     * Sorts the passed array of seismograms by begin time. If a seismogram is
     * completely enveloped by another seismogram in terms of time, it is not
     * returned
     * 
     * @returns the seismograms in order of begin time
     */
    public static LocalSeismogramImpl[] sortByDate(LocalSeismogramImpl[] seis) {
        Map seisTimes = new HashMap();
        for(int i = 0; i < seis.length; i++) {
            seisTimes.put(seis[i],
                          new MicroSecondTimeRange(seis[i].getBeginTime(),
                                                   seis[i].getEndTime()));
        }
        List seisList = SortTool.sortByDate(seisTimes);
        seis = new LocalSeismogramImpl[seisList.size()];
        return (LocalSeismogramImpl[])seisList.toArray(seis);
    }

    public static RequestFilter[] sortByDate(RequestFilter[] rf) {
        Map rfTimes = new HashMap();
        for(int i = 0; i < rf.length; i++) {
            rfTimes.put(rf[i], new MicroSecondTimeRange(rf[i]));
        }
        List rfList = SortTool.sortByDate(rfTimes);
        rf = new RequestFilter[rfList.size()];
        return (RequestFilter[])rfList.toArray(rf);
    }

    public static List sortByDate(Map objectTimes) {
        List sortedObj = new ArrayList();
        Iterator objIt = objectTimes.keySet().iterator();
        while(objIt.hasNext()) {
            Object currentObj = objIt.next();
            MicroSecondDate timeToBeAdded = ((MicroSecondTimeRange)objectTimes.get(currentObj)).getBeginTime();
            ListIterator it = sortedObj.listIterator();
            boolean added = false;
            while(it.hasNext()) {
                Object current = it.next();
                MicroSecondDate currentTime = ((MicroSecondTimeRange)objectTimes.get(current)).getBeginTime();
                if(timeToBeAdded.before(currentTime)) {
                    it.previous();
                    it.add(currentObj);
                    added = true;
                    break;
                }
            }
            if(!added) {
                sortedObj.add(currentObj);
            }
        }
        MicroSecondTimeRange prev = null;
        ListIterator it = sortedObj.listIterator();
        while(it.hasNext()) {
            MicroSecondTimeRange cur = (MicroSecondTimeRange)objectTimes.get(it.next());
            if(prev != null && prev.getEndTime().after(cur.getEndTime())) {
                it.remove();
            } else {
                prev = cur;
            }
        }
        return sortedObj;
    }}
