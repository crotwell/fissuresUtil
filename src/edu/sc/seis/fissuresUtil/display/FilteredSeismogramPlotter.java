package edu.sc.seis.fissuresUtil.display;

import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.Dimension;
import java.lang.ref.SoftReference;
import edu.sc.seis.fissuresUtil.freq.ButterworthFilter;
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

public class FilteredSeismogramPlotter implements Plotter{
    public FilteredSeismogramPlotter(ButterworthFilter filter, LocalSeismogram seis, TimeRangeConfig tr, AmpRangeConfig ar){
	this.seis = (LocalSeismogramImpl)seis;
	this.timeConfig = tr;
	this.ampConfig = ar;
	this.filter = filter;
	filterData();
    }

    public Shape draw(Dimension size){
	if(visible){
	    try{
		MicroSecondTimeRange overTimeRange = timeConfig.getTimeRange(seis).
		    getOversizedTimeRange(BasicSeismogramDisplay.OVERSIZED_SCALE);
		int[][] pixels = SimplePlotUtil.compressYvalues(filteredSeis, overTimeRange, ampConfig.getAmpRange(seis), size);
		SimplePlotUtil.scaleYvalues(pixels, filteredSeis, overTimeRange, ampConfig.getAmpRange(seis), size); 
		int[] xPixels = pixels[0];
		int[] yPixels = pixels[1];
		if(xPixels.length >= 2){
		    GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPixels.length - 1);
		    currentShape.moveTo(xPixels[0], yPixels[0]);
		    for(int i = 1; i < xPixels.length; i++)
			currentShape.lineTo(xPixels[i], yPixels[i]);
		    return currentShape;
		}else if(xPixels.length == 1){
		    GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 2);
		    currentShape.moveTo(0, yPixels[0]);
		    currentShape.lineTo(size.width, yPixels[0]);
		    return currentShape;		
		}
	    }catch(Exception e){ e.printStackTrace(); }
	}
	return new GeneralPath();
    }

    public void filterData(){
	float[] fdata = seis.get_as_floats();
	// remove the mean before filtering
	double mean = 0;
	for (int i=0; i<fdata.length; i++) {
	    mean += fdata[i];
	} // end of for (int i=0; i<fdata.length; i++)
	mean /= fdata.length;
	float fmean = (float)mean;
	for (int i=0; i<fdata.length; i++) {
	    fdata[i] -= fmean;
	} // end of for (int i=0; i<fdata.length; i++)
	Cmplx[] fftdata = Cmplx.fft(fdata);
	//save memory
	fdata = null;
	double dt = seis.getSampling().getPeriod().convertTo(UnitImpl.SECOND).getValue();
	Cmplx[] filtered = filter.apply(dt, fftdata);
	// save memory
	fftdata = null;
	float[] outData = Cmplx.fftInverse(filtered, seis.getNumPoints());
	TimeSeriesDataSel sel = new TimeSeriesDataSel();
	sel.flt_values(outData);
	filteredSeis = new LocalSeismogramImpl(seis, sel);
    }

    public ButterworthFilter getFilter(){ return filter; }

    public void toggleVisibility(){ visible = !visible; } 

    public void setVisibility(boolean b){ visible = b; }

    public LocalSeismogramImpl getUnfilteredSeismogram(){ return seis; }

    protected LocalSeismogramImpl seis, filteredSeis;
    
    protected TimeRangeConfig timeConfig;
    
    protected AmpRangeConfig ampConfig;
    
    protected ButterworthFilter filter;
    
    protected boolean visible = true;
    
    protected static SeisGramText localeText = new SeisGramText(null);
    
    static Category logger = Category.getInstance(FilteredSeismogramPlotter.class.getName());
}// FilteredSeismogramPlotter
