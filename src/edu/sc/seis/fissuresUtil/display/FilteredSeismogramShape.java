package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.UnsupportedDataEncoding;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Dimension;
import org.apache.log4j.*;
import java.util.Date;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * FilteredSeismogramShape.java
 *
 *
 * Created: Thu Aug 08 14:24:04 2002
 *
 * @author Charlie Groves
 * @version
 */

public class FilteredSeismogramShape extends SeismogramShape {
    public FilteredSeismogramShape(ColoredFilter filter, DataSetSeismogram seismogram){
        super(seismogram);
        this.seismogram = seismogram;
        this.filter = filter;
        filterData();
        super.dss = filteredSeis;
        super.stat = new Statistics(filteredSeis.getSeismogram());
    }
    
    
    public void filterData(){
        float[] fdata;
        if(seismogram.getSeismogram().can_convert_to_float())
            fdata = seismogram.getSeismogram().get_as_floats();
        else{
            int[] idata = seismogram.getSeismogram().get_as_longs();
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
        double dt = seismogram.getSeismogram().getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
        Cmplx[] filtered = filter.apply(dt, fftdata);
        // save memory
        fftdata = null;
        float[] outData = Cmplx.fftInverse(filtered, seismogram.getSeismogram().getNumPoints());
        for (int i=0; i<outData.length; i++) {
            outData[i] += fmean;
        } // end of for (int i=0; i<fdata.length; i++)
        TimeSeriesDataSel sel = new TimeSeriesDataSel();
        sel.flt_values(outData);
        filteredSeis = new DataSetSeismogram(new LocalSeismogramImpl(seismogram.getSeismogram(), sel), seismogram.getDataSet());;
    }
    
    public ColoredFilter getFilter(){ return filter; }
    
    public DataSetSeismogram getFilteredSeismogram(){ return filteredSeis; }
    
    protected DataSetSeismogram filteredSeis, seismogram;
    
    protected ColoredFilter filter;
    
    protected static SeisGramText localeText = new SeisGramText(null);
    
    static Category logger = Category.getInstance(FilteredSeismogramShape.class.getName());
}// FilteredSeismogramShape
