/**
 * FilteredDataSetSeismogram.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramContainerListener;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class FilteredDataSetSeismogram extends DataSetSeismogram implements SeismogramContainerListener{


    public FilteredDataSetSeismogram(DataSetSeismogram dss,
                                     ColoredFilter filter){
        super(dss.getDataSet(), filter.getName());
        this.filter = filter;
        wrappedDSS = dss;
        container = new SeismogramContainer(this, wrappedDSS);
    }

    public void updateData(){
        filterPool.invokeLater(new Filterer());
    }

    private static WorkerThreadPool filterPool = new WorkerThreadPool("FilterThread", 1, Thread.NORM_PRIORITY - 1);


    private class Filterer implements Runnable{
        public void run(){
            LocalSeismogramImpl[] containedSeis = container.getSeismograms();
            List alreadyFiltered = new ArrayList();
            Iterator it = dataMap.keySet().iterator();
            boolean found = false;
            while(it.hasNext()){
                LocalSeismogramImpl current = (LocalSeismogramImpl)it.next();
                for (int i = 0; i < containedSeis.length && !found; i++){
                    if(current == containedSeis[i]){
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
                    for (int j = 0; j < containedSeis.length && !found; j++) {
                        if(containedSeis == it.next()){
                            found = true;
                        }
                    }
                }
                if(!found){
                    dataMap.put(containedSeis[i], filterData(containedSeis[i],
                                                             filter));
                }
            }
            if((containedSeis.length - alreadyFiltered.size()) > 0){
                pushData(getFilteredSeismograms(), null);
            }
        }
    }

    public static LocalSeismogramImpl filterData(LocalSeismogramImpl seismogram,
                                                 ColoredFilter filter){
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
        Iterator it = dataMap.keySet().iterator();
        List filteredSeis = new ArrayList();
        while(it.hasNext()){
            filteredSeis.add(dataMap.get(it.next()));
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

    public edu.iris.Fissures.Time getBeginTime() {
        return wrappedDSS.getBeginTime();
    }

    public void setBeginTime(edu.iris.Fissures.Time time) {
        throw new UnsupportedOperationException("Cannot set begin time on filtered seismogram.  It is entirely reliant on the wrapped dss time");
    }

    public MicroSecondDate getEndMicroSecondDate() {
        return wrappedDSS.getEndMicroSecondDate();
    }

    public edu.iris.Fissures.Time getEndTime() {
        return wrappedDSS.getEndTime();
    }

    public void setEndTime(edu.iris.Fissures.Time time) {
        throw new UnsupportedOperationException("Cannot set end time on filtered seismogram.  It is entirely reliant on the wrapped dss time");

    }

    public RequestFilter getRequestFilter() {
        return wrappedDSS.getRequestFilter();
    }

    public ColoredFilter getFilter(){ return filter; }

    private ColoredFilter filter;

    private DataSetSeismogram wrappedDSS;

    private SeismogramContainer container;

    private Map dataMap = new HashMap();

    private static final Logger logger = Logger.getLogger(FilteredDataSetSeismogram.class);
}
