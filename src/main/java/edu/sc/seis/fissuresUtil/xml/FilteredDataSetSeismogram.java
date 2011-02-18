/**
 * FilteredDataSetSeismogram.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerFactory;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;

public class FilteredDataSetSeismogram extends DataSetSeismogram implements SeismogramContainerListener{


    private FilteredDataSetSeismogram(DataSetSeismogram dss,
                                      NamedFilter filter){
        super(dss.getDataSet(), filter.getName());
        this.filter = filter;
        wrappedDSS = dss;
        container = SeismogramContainerFactory.create(this, wrappedDSS);
        container.getSeismograms();
    }

    public static FilteredDataSetSeismogram getFiltered(DataSetSeismogram dss,
                                                        NamedFilter filter){
        Map dssMap = (Map)filterMap.get(filter);
        if(dssMap != null){
            FilteredDataSetSeismogram fDSS = (FilteredDataSetSeismogram)dssMap.get(dss);
            if(fDSS != null){
                return fDSS;
            }
        }else{
            dssMap = new HashMap();
            filterMap.put(filter, dssMap);
        }
        FilteredDataSetSeismogram fDSS = new FilteredDataSetSeismogram(dss, filter);
        dssMap.put(dss, fDSS);
        return fDSS;
    }

    private static Map filterMap = new HashMap();

    public void updateData(){
        filterPool.invokeLater(new Filterer());
    }
    private class Filterer implements Runnable{
        public void run(){
            LocalSeismogramImpl[] containedSeis = container.getSeismograms();
            List alreadyFiltered = new ArrayList();
            Iterator it = data.iterator();
            boolean found = false;
            while(it.hasNext()){
                SoftReference currentRef = (SoftReference)it.next();
                LocalSeismogramImpl current = (LocalSeismogramImpl)currentRef.get();
                if(current == null){
                    it.remove();
                    break;
                }
                for (int i = 0; i < containedSeis.length && !found; i++){
                    if(current.getEndTime().equals(containedSeis[i].getEndTime()) &&
                       current.getBeginTime().equals(containedSeis[i].getBeginTime())){
                        found = true;
                        alreadyFiltered.add(containedSeis[i]);
                    }
                }
                if(!found){
                    it.remove();
                }
            }
            for (int i = 0; i < containedSeis.length; i++){
                it = alreadyFiltered.iterator();
                found = false;
                while(it.hasNext()){
                    Object o = it.next();
                    if(containedSeis[i] == o){
                        found = true;
                    }
                }
                if(!found){
                    try {
                    data.add(new SoftReference(filterData(containedSeis[i],
                                                          filter)));
                    } catch ( FissuresException e) {
                        GlobalExceptionHandler.handle("Problem filtering seismograms", e);
                    }
                }
            }
            if((containedSeis.length - alreadyFiltered.size()) > 0){
                pushData(getFilteredSeismograms(), null);
            }
        }
    }

    public static LocalSeismogramImpl filterData(LocalSeismogramImpl seismogram,
                                                 NamedFilter filter) throws FissuresException {
        float[] fdata;
        if(seismogram.can_convert_to_float())
            fdata = seismogram.get_as_floats();
        else{
            int[] idata = seismogram.get_as_longs();
            fdata = new float[idata.length];
            for(int i = 0; i < idata.length; i++)
                fdata[i] = idata[i];
            idata = null;
        }
        // remove the mean before filtering
        Statistics stats = new Statistics(fdata);
        double mean = stats.mean();
        float fmean = (float)mean;
        for (int i=0; i<fdata.length; i++) {
            fdata[i] -= fmean;
        } // end of for (int i=0; i<fdata.length; i++)
        Cmplx[] fftdata = Cmplx.fft(fdata);
        //save memory
        fdata = null;
        double dt = seismogram.getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
        Cmplx[] filtered = filter.apply(dt, fftdata);
        // save memory
        fftdata = null;
        float[] outData = Cmplx.fftInverse(filtered, seismogram.getNumPoints());
        for (int i=0; i<outData.length; i++) {
            outData[i] += fmean;
        } // end of for (int i=0; i<fdata.length; i++)
        TimeSeriesDataSel sel = new TimeSeriesDataSel();
        sel.flt_values(outData);
        return new LocalSeismogramImpl(seismogram, sel);
    }

    private LocalSeismogramImpl[] getFilteredSeismograms(){
        boolean someCollected = false;
        Iterator it = data.iterator();
        List filteredSeis = new ArrayList();
        while(it.hasNext()){
            SoftReference current = (SoftReference)it.next();
            LocalSeismogramImpl curSeis = (LocalSeismogramImpl)current.get();
            if(curSeis != null){
                filteredSeis.add(curSeis);
            }else{
                it.remove();
                someCollected = true;
            }
        }
        if(someCollected){
            updateData();
        }
        return (LocalSeismogramImpl[])filteredSeis.toArray(new LocalSeismogramImpl[filteredSeis.size()]);
    }

    public void retrieveData(SeisDataChangeListener dataListener) {
        pushData(getFilteredSeismograms(), dataListener);
    }

    public boolean equals(Object other){
        if(super.equals(other)){
            FilteredDataSetSeismogram otherFil = (FilteredDataSetSeismogram)other;
            if(otherFil.getFilter().equals(getFilter())){
                return true;
            }
        }
        return false;
    }
    public MicroSecondDate getBeginMicroSecondDate() {
        return wrappedDSS.getBeginMicroSecondDate();
    }

    public Time getBeginTime() {
        return wrappedDSS.getBeginTime();
    }

    public void setBeginTime(Time time) {
        throw new UnsupportedOperationException("Cannot set begin time on filtered seismogram.  It is entirely reliant on the wrapped dss time");
    }

    public MicroSecondDate getEndMicroSecondDate() {
        return wrappedDSS.getEndMicroSecondDate();
    }

    public Time getEndTime() {
        return wrappedDSS.getEndTime();
    }

    public void setEndTime(Time time) {
        throw new UnsupportedOperationException("Cannot set end time on filtered seismogram.  It is entirely reliant on the wrapped dss time");

    }

    public RequestFilter getRequestFilter() {
        return wrappedDSS.getRequestFilter();
    }

    public NamedFilter getFilter(){ return filter; }

    private static WorkerThreadPool filterPool = new WorkerThreadPool("FilterThread", 1, Thread.NORM_PRIORITY - 1);

    private NamedFilter filter;

    private DataSetSeismogram wrappedDSS;

    private SeismogramContainer container;

    private List data = new ArrayList();

    private static final Logger logger = LoggerFactory.getLogger(FilteredDataSetSeismogram.class);
}
