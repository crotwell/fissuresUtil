package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.display.TimePlotConfig;
import edu.iris.Fissures.IfTimeSeries.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfParameterMgr.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.awt.Dimension;
import java.util.Date;
import org.apache.log4j.*;

/**
 * SeisPlotUtil.java
 *
 *
 * Created: Thu Jul  8 11:22:02 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class SeisPlotUtil  {
    
    public SeisPlotUtil() {
	
    }
 
    /* Note: this implementation only clips in time, not in amplitude
       ie the y dimension and the yMin and yMax scale the data, but
       don't throw out points larger than yMax or smaller tham yMin
    */      
    public static int[][] calculatePlottable(LocalSeismogram seismogram, 
					     UnitRangeImpl a,
					     MicroSecondTimeRange t,
                                             Dimension size) 
    throws UnsupportedDataEncoding {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

	double pointsPerPixal = t.getInterval().divideBy(seis.getSampling().getPeriod()).getValue() / 
	    size.width;
        if (pointsPerPixal > 3) {
            return getPlottableCompress(seis, a, t, size);
        } else {
            return getPlottableSimple(seis,  a, t, size);
        }
    }

    /* Note: this implementation only clips in time, not in amplitude
       ie the y dimension and the yMin and yMax scale the data, but
       don't throw out points larger than yMax or smaller tham yMin
    */    
    public static int[][] getPlottableCompress(LocalSeismogram seismogram, 
                                               UnitRangeImpl a,
					       MicroSecondTimeRange t,
                                               Dimension size) 
    throws UnsupportedDataEncoding {

	int[][] uncomp = getPlottableSimple(seismogram, a, t, size);
        if (uncomp[0].length < 3*size.width) {
            // not enough points to be worth it
            return uncomp;
        }

        // enough points to take the extra time to compress the line
        int[][] comp = new int[2][];
        int pixels = uncomp[0][uncomp[0].length-1] - uncomp[0][0] + 1;
        comp[0] = new int[2*pixels];
        comp[1] = new int[2*pixels];

        int j=0;
        int currPixel = uncomp[0][0];
        comp[0][j] = currPixel;
        comp[1][j] = uncomp[1][0];
        comp[0][j+1] = currPixel;
        comp[1][j+1] = uncomp[1][0];
        for (int i=0; i<uncomp[0].length; i++) {
            if (currPixel != uncomp[0][i]) {
                currPixel = uncomp[0][i];
                j+=2;
                comp[0][j] = currPixel;
                comp[1][j] = uncomp[1][i];
                comp[0][j+1] = currPixel;
                comp[1][j+1] = uncomp[1][i];
            } else {
                if (comp[1][j] > uncomp[1][i]) {
                    comp[1][j] = uncomp[1][i];
                }
                if (comp[1][j+1] < uncomp[1][i]) {
                    comp[1][j+1] = uncomp[1][i];
                }
            }
        }
        return comp;
    }

  
    /* Note: this implementation only clips in time, not in amplitude
       ie the y dimension and the yMin and yMax scale the data, but
       don't throw out points larger than yMax or smaller tham yMin
    */    
    public static int[][] getPlottableSimple(LocalSeismogram seismogram, 
					     UnitRangeImpl a, 
					     MicroSecondTimeRange t,
					     Dimension size) 
    throws UnsupportedDataEncoding {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

        int[][] out = new int[2][];
        //System.out.println("SeisPlotUtil Time window: "+seis.getBeginTime()+" "+config.getBeginTime()+"\n"+seis.getEndTime()+" "+config.getEndTime());

	if ( seis.getEndTime().before(t.getBeginTime()) ||
	     seis.getBeginTime().after(t.getEndTime())){
	    out[0] = new int[0];
	    out[1] = new int[0];
            //Logger.log(5,"SeisPlotUtil no data in time window");

	    return out;
	}

        MicroSecondDate tMin = t.getBeginTime();
        MicroSecondDate tMax = t.getEndTime();
	double yMin = a.getMinValue();
        double yMax = a.getMaxValue();

	int seisStartIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMin);
	int seisEndIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMax);
	// get one more
	seisStartIndex--;
	seisEndIndex++;
	if (seisStartIndex < 0) {
	    seisStartIndex = 0;
	}
	if (seisEndIndex >= seis.getNumPoints()) {
	    seisEndIndex = seis.getNumPoints()-1;
	}
        MicroSecondDate temp = getValue(seis.getNumPoints(),
                                        seis.getBeginTime(),
                                        seis.getEndTime(),
                                        seisStartIndex);
        int pixelStartIndex = getPixel(size.width, 
				       t.getBeginTime(),
				       t.getEndTime(),
                                     temp);
        
        temp = getValue(seis.getNumPoints(),
                        seis.getBeginTime(),
                        seis.getEndTime(),
                        seisEndIndex);
        int pixelEndIndex = getPixel(size.width,
                                     t.getBeginTime(),
                                     t.getEndTime(),
                                     temp);
                                     

        return getPlottableSimple(seis, 
                                  seisStartIndex, seisEndIndex,
                                  pixelStartIndex, pixelEndIndex,
                                  yMin, yMax,
                                  size);
    }

    /** calculates the line to be plotted zoomed to fit the window. */
    public static int[][] getPlottableSimple(LocalSeismogramImpl seis, 
                                             int seisStartIndex,
                                             int seisEndIndex,
                                             int pixelStartIndex,
                                             int pixelEndIndex,
                                             Dimension size) 
    throws UnsupportedDataEncoding {
        double yMin = seis.getMinValue(seisStartIndex,
                                       seisEndIndex).getValue();
        double yMax = seis.getMaxValue(seisStartIndex,
                                       seisEndIndex).getValue();
        return getPlottableSimple(seis, seisStartIndex, seisEndIndex,
                                  pixelStartIndex, pixelEndIndex,
                                  yMin, yMax,
                                  size);
    }

    public static int[][] getPlottableSimple(LocalSeismogramImpl seis, 
                                             int seisStartIndex,
                                             int seisEndIndex,
                                             int pixelStartIndex,
                                             int pixelEndIndex,
                                             double yMin,
                                             double yMax,
                                             Dimension size) 
    throws UnsupportedDataEncoding {
        int[][] out = new int[2][];
	int seisIndex = 0;
	int pixelIndex = 0;
	int numAdded = 0;
        //System.out.println("SeisplotUtil start="+seisStartIndex+
        //                   "  end="+seisEndIndex+" numPts="+seis.getNumPoints());


	//	    Logger.log(5,"few points");
	out[0] = new int[seisEndIndex-seisStartIndex+1];
	out[1] = new int[out[0].length];
	seisIndex = seisStartIndex;
	numAdded = 0;
	while (seisIndex <= seisEndIndex) {
	    out[0][numAdded] = 
		Math.round((float)(linearInterp(seisStartIndex,
						pixelStartIndex,
						seisEndIndex,
						pixelEndIndex,
						seisIndex)));
	    out[1][numAdded] = 
		Math.round((float)(linearInterp(yMin, 0,
                                                yMax, size.height,
			 seis.getValueAt(seisIndex).getValue())));
	    seisIndex++;
	    numAdded++;
	}
	
	int temp[][] = new int[2][];
	temp[0] = new int[numAdded];
	temp[1] = new int[numAdded];
	System.arraycopy(out[0], 0, temp[0], 0, numAdded);
	System.arraycopy(out[1], 0, temp[1], 0, numAdded);
	return temp;

    }

    /** rotates the seismograms to a new azimuth and a perpendicular. */
    public static LocalSeismogramImpl[] vectorRotate(LocalSeismogramImpl hSeis,
                                                 LocalSeismogramImpl vSeis,
                                                 ChannelImpl hChannel,
                                                 ChannelImpl vChannel,
                                                 double azimuth,
                                                 MicroSecondDate begin,
                                                 MicroSecondDate end) 
    throws UnsupportedDataEncoding {
        Assert.isTrue(hSeis.y_unit.equals(vSeis.y_unit), 
                      "Units must be the same");
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[2];
      
	int hSeisStartIndex = getPixel(hSeis.getNumPoints(),
				      hSeis.getBeginTime(),
				      hSeis.getEndTime(),
				      begin);
	int hSeisEndIndex = getPixel(hSeis.getNumPoints(),
                                    hSeis.getBeginTime(),
                                    hSeis.getEndTime(),
                                    end);
	int vSeisStartIndex = getPixel(vSeis.getNumPoints(),
				      vSeis.getBeginTime(),
				      vSeis.getEndTime(),
				      begin);
	int vSeisEndIndex = getPixel(vSeis.getNumPoints(),
                                    vSeis.getBeginTime(),
                                    vSeis.getEndTime(),
                                    end);
        float[] hData = hSeis.getValues(hSeisStartIndex, 
                                        hSeisEndIndex-hSeisStartIndex);
        float[] vData = vSeis.getValues(vSeisStartIndex, 
                                        vSeisEndIndex-vSeisStartIndex);
        if (hData.length > vData.length) {
            float[] temp = new float[vData.length];
            System.arraycopy(hData, 0, temp, 0, temp.length);
            hData = temp;            
        } else {
            float[] temp = new float[hData.length];
            System.arraycopy(vData, 0, temp, 0, temp.length);
            vData = temp;
        }

        float[] tempX = new float[hData.length];
        float[] tempY = new float[hData.length];
        double dToR = Math.PI/180.0;
        double azimuthA = (hChannel.an_orientation.azimuth-azimuth)*dToR;
        double azimuthB = (vChannel.an_orientation.azimuth-azimuth)*dToR;
        for (int i=0; i<hData.length; i++) {
            tempY[i] = (float)(Math.cos(azimuthA) * hData[i] +
                Math.cos(azimuthB) * vData[i]);
            tempX[i] = (float)(Math.sin(azimuthA) * hData[i] +
                Math.sin(azimuthB) * vData[i]);

        }
        TimeSeriesDataSel xData = new TimeSeriesDataSel();
        xData.flt_values(tempX);
        TimeSeriesDataSel yData = new TimeSeriesDataSel();
        yData.flt_values(tempY);
        edu.iris.Fissures.Time time = 
                new edu.iris.Fissures.Time(ISOTime.getISOString(begin), 
                                           -1);
        Property[] props = new Property[1];
        props[0] = new Property("Name", hSeis.getName()+" rotated");
        out[0] = new LocalSeismogramImpl("tempX",
                                         props,
                                         time,
                                         tempX.length,
                                         (SamplingImpl)hSeis.sampling_info,
                                         (UnitImpl)hSeis.y_unit,
                                         hSeis.channel_id,
					 new ParameterRef[0],
                                         hSeis.time_corrections,
					 hSeis.sample_rate_history,
                                         xData);
        props = new Property[1];
        props[0] = new Property("Name", vSeis.getName()+" rotated");
        out[1] = new LocalSeismogramImpl("tempY",
                                         props,
                                         time,
                                         tempY.length,
                                         (SamplingImpl)vSeis.sampling_info,
                                         (UnitImpl)vSeis.y_unit,
                                         vSeis.channel_id,
					 new ParameterRef[0],
                                         vSeis.time_corrections,
					 vSeis.sample_rate_history,
                                         yData);
        return out;
    }

    /** flips an array of ints to be inArray[i] = maxValue -
        inArray[i]. This is mainly useful when displaying as
        calculations are generally done in the traditional y positive
        up coordinate system, and then "flipped" at the last minute to
        plot int the graphical y positive down screen coordinate
        system.  
    */
    public static final void flipArray(int[] inArray, int maxValue) {
	for (int i = 0; i < inArray.length; i++) {
	    // this does the equivalent of inArray[i] = maxValue - intArray[i];
	    // but is presumably faster as no temp ints need be created
	    inArray[i] *= -1;
	    inArray[i] += maxValue;
	}
    }

    public static LocalSeismogram createTestData() {
        return createTestData("Fake Data");
    }

    public static LocalSeismogram createTestData(String name) {
        int[] dataBits = new int[100];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
            tmpDouble = Math.random()*2.0 -1.0;
            //    tmpDouble = .4 + Math.random()*.1;
            // this makes the values a little more likely to be close 
            // to the center, making it slightly more seimogram like
            tmpDouble = tmpDouble * tmpDouble * tmpDouble * tmpDouble * tmpDouble;
            dataBits[i] = (int)Math.round(tmpDouble*2000.0);
        }

        return createTestData(name, dataBits);
    }

    public static LocalSeismogram createTestData(String name, int[] dataBits) {
        String id = "Nowhere: "+name;
        edu.iris.Fissures.Time time = 
                new edu.iris.Fissures.Time("19991231T235959.000Z", 
                                                    -1);
        
        TimeInterval timeInterval = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl sampling = 
            new SamplingImpl(20,
                         timeInterval);
        ChannelId channelID = new ChannelId(new NetworkId("XX",
							  time), 
					    "FAKE",
					    "00", 
					    "BHZ",
                                            time);

        TimeSeriesDataSel bits = new TimeSeriesDataSel();
        bits.int_values(dataBits);

        Property[] props = new Property[1];
        props[0] = new Property("Name", name);
	TimeInterval[] time_corr = new TimeInterval[1];
	time_corr[0] = new TimeInterval(.123, UnitImpl.SECOND);
        LocalSeismogramImpl seis = 
	    new LocalSeismogramImpl(id,
				    props,
				    time,
				    dataBits.length,
				    sampling,
				    UnitImpl.COUNT,
				    channelID,
				    new ParameterRef[0],
				    time_corr,
				    new SamplingImpl[0],
				    bits);
        return seis;
    }


    public static LocalSeismogram createSineWave() {
        return createSineWave(0);
    }

    public static LocalSeismogram createSineWave(double phase) {
        return createSineWave(phase, 1);
    }

     public static LocalSeismogram createSineWave(double phase, double hertz) {
	 return createSineWave(phase, hertz, 1200);
     }

    public static LocalSeismogram createSineWave(double phase, double hertz, int numPoints) {
	return createSineWave(phase, hertz, numPoints, 1000);
    }

    public static LocalSeismogram createSineWave(double phase, double hertz, int numPoints, double amp) {
        int[] dataBits = new int[numPoints];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*amp);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }

   public static LocalSeismogram createHighSineWave(double phase, double hertz) {
        int[] dataBits = new int[120];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*1000.0+500);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }

    public static LocalSeismogram createLowSineWave(double phase, double hertz) {
        int[] dataBits = new int[120];
        double tmpDouble;
        for (int i=0; i<dataBits.length; i++) {
             dataBits[i] = 
                 (int)Math.round(Math.sin(phase + 
                                          i*Math.PI*hertz/20.0)*1000.0-500);
	}
	

        return createTestData("Sine Wave, phase "+phase+" hertz "+hertz,
                              dataBits);
    }
    
    /*
    public static ChannelGroup createChannelGroup() {
               LocalSeismogramImpl[] seis = new LocalSeismogramImpl[3];
        //        Seismogram seis = SeisPlotUtil.createTestData();
        seis[0] = SeisPlotUtil.createSineWave();
        seis[1] = SeisPlotUtil.createSineWave(Math.PI/2);
        seis[2] = SeisPlotUtil.createSineWave(Math.PI);
        edu.iris.Fissures.ElementId id = 
            new edu.iris.Fissures.ElementId("Nowhere",
                                                     "Sine Wave ChannelGroup",
                                                     "0");
        edu.iris.Fissures.ChannelGroupAttr chGroupAttr =
            new  edu.iris.Fissures.ChannelGroupAttr(id,
                                                             "",
                                                             edu.iris.Fissures.ChannelGroupType.CG3_COMPONENT);
        return new ChannelGroup(chGroupAttr, seis);
    }
    */
                   

    /** solves the equation <pre>(yb-ya)/(xb-xa) = (y-ya)/(x-xa)</pre>
     *  for y given x. Useful for finding the pixel for a value given the
     *  dimension of the area and the range of values it is supposed to
     *  cover. Note, this does not check for xa == xb, in which case a
     *  divide by zero would occur.
     */
    public static final double linearInterp(double xa, double ya,
                                      double xb, double yb,
                                      double x) {
        if (x == xa) return ya;
        if (x == xb) return yb;
        return (yb - ya)*(x-xa)/(xb-xa) + ya;
    }

    public static final int getPixel(int totalPixels, 
                               MicroSecondDate begin,
                               MicroSecondDate end,
                               MicroSecondDate value) {
        return (int)Math.round(linearInterp(begin.getMicroSecondTime(),
					    0,
                                            end.getMicroSecondTime(),
                                            totalPixels, 
                                            value.getMicroSecondTime()));
    }

    public static final MicroSecondDate getValue(int totalPixels, 
                                           MicroSecondDate begin,
                                           MicroSecondDate end,
                                           int pixel) {
        double value = 
            linearInterp(0,
                         0,
                         totalPixels,
                         end.getMicroSecondTime()-begin.getMicroSecondTime(),
                         pixel);
        return new MicroSecondDate(begin.getMicroSecondTime() +
                                   Math.round(value));
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               QuantityImpl value) {
        QuantityImpl converted = value.convertTo(range.getUnit());
        return getPixel(totalPixels, range, converted.getValue());
    }

    public static final int getPixel(int totalPixels, 
                               UnitRangeImpl range,
                               double value) {
        return (int)Math.round(linearInterp(range.getMinValue(), 0,
                                            range.getMaxValue(), totalPixels, 
                                            value));
    }

    public static final QuantityImpl getValue(int totalPixels, 
                                    UnitRangeImpl range,
                                    int pixel) {
        double value = linearInterp(0, 
                                    range.getMinValue(),
                                    totalPixels, 
                                    range.getMaxValue(),
                                    pixel);
        return new QuantityImpl(value, range.getUnit());
    }        

    public static final MicroSecondDate getTimeForIndex(int index,
				      		MicroSecondDate beginTime,
						SamplingImpl sampling) {
	TimeInterval width = sampling.getPeriod();
	width = (TimeInterval)width.multiplyBy(index);
	return beginTime.add(width);
    }


    protected static int[][] scaleXvalues(LocalSeismogram seismogram, 
					  TimePlotConfig config,
					  Dimension size) 
	throws UnsupportedDataEncoding {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

	int[][] out = new int[2][];
	int seisIndex = 0;
	int pixelIndex = 0;
	int numAdded = 0;
	  
      
	if ( seis.getEndTime().before(config.getBeginTime()) ||
	     seis.getBeginTime().after(config.getEndTime()) ) {
	    
	    out[0] = new int[0];
	    out[1] = new int[0];
            logger.info("The end time is before the beginTime in simple seismogram");
	    return out;
	}
	   
	    

        MicroSecondDate tMin = config.getBeginTime();
        MicroSecondDate tMax = config.getEndTime();
	UnitRangeImpl ampRange = config.getAmpRange().convertTo(seis.getUnit());
        double yMin = ampRange.getMinValue();
        double yMax = ampRange.getMaxValue();


	int seisStartIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      config.getBeginTime());
	int seisEndIndex = getPixel(seis.getNumPoints(),
				    seis.getBeginTime(),
				    seis.getEndTime(),
				    config.getEndTime());
	seisStartIndex--;
	seisEndIndex++;
	if (seisStartIndex < 0) {
	    seisStartIndex = 0;
	}
	if (seisEndIndex >= seis.getNumPoints()) {
	    seisEndIndex = seis.getNumPoints()-1;
	}

	MicroSecondDate tempdate = getValue(seis.getNumPoints(),
					    seis.getBeginTime(),
					    seis.getEndTime(),
					    seisStartIndex);
	int pixelStartIndex = getPixel(size.width, 
				       config.getBeginTime(),
				       config.getEndTime(),
				       tempdate);
        
	  

	tempdate = getValue(seis.getNumPoints(),
			    seis.getBeginTime(),
			    seis.getEndTime(),
			    seisEndIndex);
      
        int pixelEndIndex = getPixel(size.width,
                                     config.getBeginTime(),
                                     config.getEndTime(),
                                     tempdate);
                                     
 

       
	int pixels = size.width;
	out[0] = new int[2*pixels];
	out[1] = new int[out[0].length];
	int tempYvalues[] = new int [out[0].length];

	seisIndex = seisStartIndex;
	numAdded = 0;
	int xvalue = 0;
	int tempValue;
	xvalue =  Math.round((float)(linearInterp(seisStartIndex,
						  pixelStartIndex,
						  seisEndIndex,
						  pixelEndIndex,
						  seisIndex)));
	seisIndex++;
	int j;
	j = 0;
	while (seisIndex <= seisEndIndex) {
	    
	    tempValue = 
		Math.round((float)(linearInterp(seisStartIndex,
						pixelStartIndex,
						seisEndIndex,
						pixelEndIndex,
						seisIndex)));
	    
	    tempYvalues[j++] = (int)seis.getValueAt(seisIndex).getValue();
	    if(tempValue != xvalue) {
		out[0][numAdded] = xvalue;
		out[0][numAdded+1] = xvalue;
		out[1][numAdded] = getMinValue(tempYvalues, 0, j-1);
		out[1][numAdded+1] = (int)getMaxValue(tempYvalues, 0, j-1);
		j = 0;
		xvalue = tempValue;
		numAdded = numAdded+2;

	    }
	    seisIndex++;
	}
	
	int temp[][] = new int[2][];
	temp[0] = new int[numAdded];
	temp[1] = new int[numAdded];
	System.arraycopy(out[0], 0, temp[0], 0, numAdded);
	System.arraycopy(out[1], 0, temp[1], 0, numAdded);

	return temp;

    }

    protected static int[][] compressYvalues(LocalSeismogram seismogram, 
					     TimePlotConfig config,
					     Dimension size)throws UnsupportedDataEncoding {
	

	int[][] uncomp = scaleXvalues(seismogram, config, size);
	

        // enough points to take the extra time to compress the line
        int[][] comp = new int[2][];
        int pixels = size.width;
	int numValuesperPixel = uncomp[0].length/size.width;
	comp[0] = new int[2*pixels];
        comp[1] = new int[2*pixels];
	
	
	
        int j=0, i, startIndex, endIndex;
	int xvalue;
	startIndex = 0; 
	xvalue = 0;
	for(i = 0, j = 0; i < uncomp[0].length; i++) {
	  
	    if(uncomp[0][i] != xvalue) {
		endIndex = i-1;
		comp[1][j] = getMinValue(uncomp[1], startIndex, endIndex);
		comp[1][j+1] = (int)getMaxValue(uncomp[1], startIndex, endIndex);
		comp[0][j] = uncomp[0][i];
		comp[0][j+1] = uncomp[0][i];
		j = j + 2;
	   
		startIndex = endIndex + 1;
		xvalue = uncomp[0][i];
	    }  
	   
	}
      
	return comp;
    }
   
    protected static void  scaleYvalues(int[][] comp, LocalSeismogram seismogram, TimePlotConfig config, Dimension size) {
	LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
	UnitRangeImpl ampRange = config.getAmpRange().convertTo(seis.getUnit());
        double yMin = ampRange.getMinValue();
        double yMax = ampRange.getMaxValue();
	for( int i =0 ; i < comp[1].length; i++) {
	    comp[1][i] = Math.round((float)(linearInterp(yMin, 0,
							 yMax, size.height,
							 comp[1][i])));
	}
	
	flipArray(comp[1], size.height);

    }
	
    private static int getMinValue(int[] yValues, int startIndex, int endIndex) {

	int minValue = java.lang.Integer.MAX_VALUE;
	for( int i = startIndex; i <= endIndex; i++) {
	    if(yValues[i] < minValue) minValue = yValues[i];

	}
	return minValue;

    }

    private static int getMaxValue(int[] yValues, int startIndex, int endIndex) {

	int maxValue = java.lang.Integer.MIN_VALUE;
	for( int i = startIndex; i <= endIndex; i++) {
	    if(yValues[i] > maxValue) maxValue = yValues[i];
	}
	return maxValue;

    }

    static Category logger = Category.getInstance(SeisPlotUtil.class.getName());

} // SeisPlotUtil
