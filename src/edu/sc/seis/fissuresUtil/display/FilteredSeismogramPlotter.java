package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.lang.ref.SoftReference;
import edu.sc.seis.fissuresUtil.bag.Statistics;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;
import org.apache.log4j.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;

/* FilteredSeismogramPlotter.java
 *
 *
 * Created: Tue Jun 25 17:35:40 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class FilteredSeismogramPlotter extends AbstractSeismogramPlotter{
    public FilteredSeismogramPlotter(ColoredFilter filter, DataSetSeismogram seis){
	this.seismogram = seis;
	this.filter = filter;
	filterData();
    }

    public void draw(Graphics2D canvas, Dimension size, TimeSnapshot timeState, AmpSnapshot ampState){
	if(visible){
	    try{
		MicroSecondTimeRange overTimeRange = timeState.getTimeRange(filteredSeis).
		    getOversizedTimeRange(BasicSeismogramDisplay.OVERSIZED_SCALE);
		int[][] pixels = SimplePlotUtil.compressYvalues(filteredSeis.getSeismogram(), overTimeRange, ampState.getAmpRange(filteredSeis), size);
		SimplePlotUtil.scaleYvalues(pixels, filteredSeis.getSeismogram(), overTimeRange, ampState.getAmpRange(filteredSeis), size); 
		SimplePlotUtil.flipArray(pixels[1], size.height);
		int[] xPixels = pixels[0];
		int[] yPixels = pixels[1];
		GeneralPath currentShape = new GeneralPath();
		if(xPixels.length >= 2){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPixels.length - 1);
		    currentShape.moveTo(xPixels[0], yPixels[0]);
		    for(int i = 1; i < xPixels.length; i++)
			currentShape.lineTo(xPixels[i], yPixels[i]);
		}else if(xPixels.length == 1){
		    currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		    currentShape.moveTo(0, yPixels[0]);
		    currentShape.lineTo(size.width, yPixels[0]);
		}
		canvas.setColor(filter.getColor());
		canvas.draw(currentShape);
	    }catch(Exception e){ e.printStackTrace(); }
	}
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
    
    protected DataSetSeismogram filteredSeis;

    protected ColoredFilter filter;
    
    protected static SeisGramText localeText = new SeisGramText(null);
    
    static Category logger = Category.getInstance(FilteredSeismogramPlotter.class.getName());
}// FilteredSeismogramPlotter
