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
 * SimplePlotUtil.java
 *
 *
 * Created: Thu Jul  8 11:22:02 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class SimplePlotUtil  {
    
    public SimplePlotUtil() {
	
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
					  MicroSecondTimeRange timeRange,
					  UnitRangeImpl ampRange,
					  Dimension size) 
	throws UnsupportedDataEncoding {

        LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;

	int[][] out = new int[2][];
	int seisIndex = 0;
	int pixelIndex = 0;
	int numAdded = 0;
	  
      
	if ( seis.getEndTime().before(timeRange.getBeginTime()) ||
	     seis.getBeginTime().after(timeRange.getEndTime()) ) {
	    
	    out[0] = new int[0];
	    out[1] = new int[0];
            logger.info("The end time is before the beginTime in simple seismogram");
	    return out;
	}
	   
	    

        MicroSecondDate tMin = timeRange.getBeginTime();
        MicroSecondDate tMax = timeRange.getEndTime();
	double yMin = ampRange.getMinValue();
        double yMax = ampRange.getMaxValue();


	int seisStartIndex = getPixel(seis.getNumPoints(),
				      seis.getBeginTime(),
				      seis.getEndTime(),
				      tMin);
	int seisEndIndex = getPixel(seis.getNumPoints(),
				    seis.getBeginTime(),
				    seis.getEndTime(),
				    tMax);
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
				       tMin,
				       tMax,
				       tempdate);
        
	  

	tempdate = getValue(seis.getNumPoints(),
			    seis.getBeginTime(),
			    seis.getEndTime(),
			    seisEndIndex);
      
        int pixelEndIndex = getPixel(size.width,
                                     tMin,
                                     tMax,
                                     tempdate);
                                     
 

       
	int pixels = size.width;
	out[0] = new int[2*pixels];
	out[1] = new int[out[0].length];
	int tempYvalues[] = new int [out[0].length];
	seisIndex = seisStartIndex;
	numAdded = 0;
	int xvalue = 0;
	int tempValue = 0;
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
	xvalue = tempValue;
	out[0][numAdded] = xvalue;
	out[0][numAdded+1] = xvalue;
	out[1][numAdded] = tempYvalues[j-1];
	out[1][numAdded+1] = tempYvalues[j-1];
	int temp[][] = new int[2][numAdded];
	System.arraycopy(out[0], 0, temp[0], 0, numAdded);
	System.arraycopy(out[1], 0, temp[1], 0, numAdded);
	return temp;
    }

    protected static int[][] compressYvalues(LocalSeismogram seismogram, 
					     MicroSecondTimeRange tr,
					     UnitRangeImpl ampRange,
					     Dimension size)throws UnsupportedDataEncoding {
	int[][] uncomp = scaleXvalues(seismogram, tr, ampRange, size);
	// enough points to take the extra time to compress the line
	int[][] comp = new int[2][2 * size.width];
	int j = 0, startIndex = 0, xvalue = 0, endIndex = 0;
	if(uncomp[0].length != 0) xvalue = uncomp[0][0];
	for(int i = 0; i < uncomp[0].length; i++) {
	    if(uncomp[0][i] != xvalue) {
		endIndex = i - 1;
		comp[1][j] = getMinValue(uncomp[1], startIndex, endIndex);
		comp[1][j+1] = (int)getMaxValue(uncomp[1], startIndex, endIndex);
		comp[0][j] = uncomp[0][i];
		comp[0][j+1] = uncomp[0][i];
		j = j + 2;
		startIndex = endIndex + 1;
		xvalue = uncomp[0][i];
		//System.out.println(xvalue);
	    }  
	}

	if(xvalue != 0) {
	    startIndex = uncomp[0].length - 1;
	    endIndex = uncomp[0].length - 1;
	    comp[1][j] = getMinValue(uncomp[1], startIndex, endIndex);
	    comp[1][j+1] = (int)getMaxValue(uncomp[1], startIndex, endIndex);
	    comp[0][j] = uncomp[0][endIndex];
	    comp[0][j+1] = uncomp[0][endIndex];
	    j = j + 2;
	}
	int temp[][] = new int[2][j];
	System.arraycopy(comp[0], 0, temp[0], 0, j);
	System.arraycopy(comp[1], 0, temp[1], 0, j);
	
	return temp;
    }
   
    protected static void  scaleYvalues(int[][] comp, LocalSeismogram seismogram, MicroSecondTimeRange tr, 
					UnitRangeImpl ampRange,  Dimension size) {
	LocalSeismogramImpl seis = (LocalSeismogramImpl)seismogram;
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

    static Category logger = Category.getInstance(SimplePlotUtil.class.getName());

} // SimplePlotUtil
