
package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Statistics.java
 *
 *
 * Created: Wed Apr  4 22:27:52 2001
 *
 * @author Philip Crotwell
 * @version $Id: Statistics.java 2794 2002-10-22 01:39:52Z crotwell $
 */

public class Statistics  {
    
    public Statistics(int[] iSeries) {
	this.iSeries = iSeries;
	beginIndex = 0;
	endIndex = iSeries.length;
    }

    public Statistics(short[] sSeries) {
	this.sSeries = sSeries;
	beginIndex = 0;
	endIndex = sSeries.length;
    }
    
    public Statistics(float[] fSeries) {
	this.fSeries = fSeries;
	beginIndex = 0;
	endIndex = fSeries.length;
    }
    
    public Statistics(double[] dSeries) {
	this.dSeries = dSeries;
	beginIndex = 0;
	endIndex = dSeries.length;
    }
    
    public Statistics(LocalSeismogramImpl seismo){
	if(seismo.can_convert_to_float()){
	    fSeries = seismo.get_as_floats();
	    endIndex = fSeries.length;
	}else{
	    iSeries = seismo.get_as_longs();
	    endIndex = iSeries.length;
	}
	beginIndex = 0;
    }

    public double min() {
	return minMaxMean()[0];
    }

    public double min(int beginIndex, int endIndex){
	return minMaxMean(beginIndex, endIndex)[0];
    }

    public double max(){
	return minMaxMean()[1];
    }

    public double max(int beginIndex, int endIndex){
	return minMaxMean(beginIndex, endIndex)[1];
    }

    public double mean(){
	return minMaxMean()[2];
    }

    public double mean(int beginIndex, int endIndex){
	return minMaxMean(beginIndex, endIndex)[2];
    }

    public double[] minMaxMean(){
	return minMaxMean(0, getLength());
    }

    public double[] minMaxMean(int beginIndex, int endIndex){
	if(minMaxMeanCalculated){
	    if(beginIndex == this.beginIndex && endIndex == this.endIndex){
		return minMaxMean;
	    }
	    if(this.beginIndex > beginIndex && this.endIndex < endIndex || 
	       this.beginIndex < beginIndex && this.endIndex > endIndex){
		return calculateMinMaxMean(beginIndex, endIndex);
	    }
	    int removalStart, removalEnd, newDataStart, newDataEnd;
	    if(this.beginIndex < beginIndex || this.endIndex < endIndex){
		removalStart = this.beginIndex;
		removalEnd = beginIndex - 1;
		newDataStart = this.endIndex;
		newDataEnd = endIndex - 1;
	    }else{
		removalStart = endIndex;
		removalEnd = this.endIndex - 1;
		newDataStart = beginIndex;
		newDataEnd = this.beginIndex;
	    }
	    minMaxMean[2] *= this.endIndex - this.beginIndex;
	    if(iSeries != null){
		for(int j = removalStart; j <= removalEnd; j++) {
		    if(iSeries[j] <= minMaxMean[0]){ 
			// if min is found in remave section reaclulate
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    if(iSeries[j] >= minMaxMean[1]){
			// if max is found in remove section reaclulate
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    minMaxMean[2] -= iSeries[j];
		}
		for(int j = newDataStart; j <= newDataEnd; j++) {
		    if(iSeries[j] < minMaxMean[0]){ 
			minMaxMean[0] = iSeries[j];
		    }
		    if(iSeries[j] > minMaxMean[1]){
			minMaxMean[1] = iSeries[j];
		    }
		    minMaxMean[2] += iSeries[j];
		}
	    }else if(sSeries != null){
		for(int j = removalStart; j <= removalEnd; j++) {
		    if(sSeries[j] <= minMaxMean[0]){ 
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    if(sSeries[j] >= minMaxMean[1]){
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    minMaxMean[2] -= sSeries[j];
		}
		for(int j = newDataStart; j <= newDataEnd; j++) {
		    if(sSeries[j] < minMaxMean[0]){ 
			minMaxMean[0] = sSeries[j];
		    }
		    if(sSeries[j] > minMaxMean[1]){
			minMaxMean[1] = sSeries[j];
		    }
		    minMaxMean[2] += sSeries[j];
		}
	    }else if(fSeries != null){
		for(int j = removalStart; j <= removalEnd; j++) {
		    if(fSeries[j] <= minMaxMean[0]){ 
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    if(fSeries[j] >= minMaxMean[1]){
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    minMaxMean[2] -= fSeries[j];
		}
		for(int j = newDataStart; j <= newDataEnd; j++) {
		    if(fSeries[j] < minMaxMean[0]){ 
			minMaxMean[0] = fSeries[j];
		    }
		    if(fSeries[j] > minMaxMean[1]){
			minMaxMean[1] = fSeries[j];
		    }
		    minMaxMean[2] += fSeries[j];
		}
	    }else if(dSeries != null){
		for(int j = removalStart; j <= removalEnd; j++) {
		    if(dSeries[j] <= minMaxMean[0]){ 
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    if(dSeries[j] >= minMaxMean[1]){
			return calculateMinMaxMean(beginIndex, endIndex);
		    }
		    minMaxMean[2] -= dSeries[j];
		}
		for(int j = newDataStart; j <= newDataEnd; j++) {
		    if(dSeries[j] < minMaxMean[0]){ 
			minMaxMean[0] = dSeries[j];
		    }
		    if(dSeries[j] > minMaxMean[1]){
			minMaxMean[1] = dSeries[j];
		    }
		    minMaxMean[2] += dSeries[j];
		}
	    }
	    this.beginIndex = beginIndex;
	    this.endIndex = endIndex;
	    minMaxMean[2] /= endIndex - beginIndex;
	    return minMaxMean;
	}
	return calculateMinMaxMean(beginIndex, endIndex);
    }
    
    private double[] calculateMinMaxMean(int beginIndex, int endIndex){
	minMaxMean[0] = Double.POSITIVE_INFINITY;
	minMaxMean[1] = Double.NEGATIVE_INFINITY;
	minMaxMean[2] = 0;
	if (iSeries != null) {
	    for (int i = beginIndex; i < endIndex; i++) {
		minMaxMean[0] = Math.min(minMaxMean[0], iSeries[i]);
		minMaxMean[1] = Math.max(minMaxMean[1], iSeries[i]);
		minMaxMean[2] += iSeries[i];
	    } // end of for (int i=0; i<iSeries.length; i++)
	} else if (sSeries != null) {
	    for (int i = beginIndex; i < endIndex; i++) {
		minMaxMean[0] = Math.min(minMaxMean[0], sSeries[i]);
		minMaxMean[1] = Math.max(minMaxMean[1], sSeries[i]);
		minMaxMean[2] += sSeries[i];
	    } // end of for (int i=0; i<sSeries.length; i++)
	} else if (fSeries != null) {
	    for (int i = beginIndex; i < endIndex; i++) {
		minMaxMean[0] = Math.min(minMaxMean[0], fSeries[i]);
		minMaxMean[1] = Math.max(minMaxMean[1], fSeries[i]);
		minMaxMean[2] += fSeries[i];
	    } // end of for (int i=0; i<fSeries.length; i++)
	} else if (dSeries != null) {
	    for (int i = beginIndex; i < endIndex; i++) {
		minMaxMean[0] = Math.min(minMaxMean[0], dSeries[i]);
		minMaxMean[1] = Math.max(minMaxMean[1], dSeries[i]);
		minMaxMean[2] += dSeries[i];
	    } // end of for (int i=0; i<dSeries.length; i++)
	}
	minMaxMean[2] /= (endIndex - beginIndex);
	this.beginIndex = beginIndex;
	this.endIndex = endIndex;
	minMaxMeanCalculated = true;
	return minMaxMean;
    }

    public double var() {
	if ( ! varianceCalculated) {
	    variance = binarySumDevSqr(0, getLength(), mean()) /
		(getLength()-1);
	}
	return variance;
    }

    public double stddev() {
	return Math.sqrt(var());
    }

    public double[] acf(int maxlag) {
	if (autocorrelation.length < maxlag+1) {
	    double[] tmp = new double[maxlag+1];
	    System.arraycopy(autocorrelation, 0, 
			     tmp, 0, 
			     autocorrelation.length);
	    double normalizer = binarySumDevSqr(0, getLength(), mean());
	    for (int i=autocorrelation.length; i< maxlag+1; i++) {
		tmp[i] = binarySumDevLag(0, getLength(), mean(), i) /
		    normalizer;
	    }
	    autocorrelation = tmp;
	}
	return autocorrelation;
    }

    public double[] acf95conf(int maxlag) {
	double[] acfVals = acf(maxlag);
	double[] out = new double[acfVals.length];
	double sumsqrs=0;
	for (int i=0; i<acfVals.length; i++) {
	    sumsqrs += acfVals[i]*acfVals[i];
	    out[i] = 1.96*Math.sqrt(1+2*sumsqrs) /
		Math.sqrt(getLength());
	}
	return out;
    }

    public double[] acfTRatio(int maxlag) {
	double[] acfVals = acf(maxlag);
	double[] conf = acf95conf(maxlag);
	double[] out = new double[acfVals.length];
	for (int i=0; i<acfVals.length; i++) {
	    out[i] = 1.96* Math.abs(acfVals[i]) /
		conf[i];
	}
	return out;
    }

    /** Computes the partial autocorrelation function, after Wei, William S.
     *  Time Series Analysis, pp 22-23. */
    public double[] pacf(int maxlag) {
	if (partialautocorr.length < maxlag) {
	    double[] tmp = new double[maxlag];
	    System.arraycopy(partialautocorr, 0, 
			     tmp, 0, 
			     partialautocorr.length);
	    
	    double[] myacf = acf(maxlag);
	    double[][] pacfMatrix = new double[maxlag+1][maxlag+1];
	    pacfMatrix[1][1] = myacf[1];
	    for (int k=2; k<=maxlag; k++) {
		double topSum = 0;
		double botSum = 0;
		for (int j=1; j<k; j++) {
		    topSum += pacfMatrix[k-1][j] * myacf[k-j];
		    botSum += pacfMatrix[k-1][j] * myacf[j];
		}
		pacfMatrix[k][k] = ( myacf[k] - topSum ) /
		                   ( 1 - botSum );
		for (int j=1; j< k; j++) {
		    pacfMatrix[k][j] = pacfMatrix[k-1][j] -
			pacfMatrix[k][k] * pacfMatrix[k-1][k-j];
		}
	    }
	    partialautocorr = new double[maxlag+1];
	    partialautocorr[0] = 1;
	    for (int k=1; k<=maxlag; k++) {
		partialautocorr[k] = pacfMatrix[k][k];
	    }
	}
	return partialautocorr;
    }
	
    public double pacf95conf(int maxlag) {
	double out = 1.96 /
	    Math.sqrt(getLength());
	return out;
    }

    public double[] pacfTRatio(int maxlag) {
	double[] pacfVals = pacf(maxlag);
	double conf = pacf95conf(maxlag);
	double[] out = new double[pacfVals.length];
	for (int i=0; i<pacfVals.length; i++) {
	    out[i] = 1.96* Math.abs(pacfVals[i]) /
		conf;
	}
	return out;
    }

    public int getLength() {
	if (iSeries != null) {
	    return iSeries.length;
	}
	if (sSeries != null) {
	    return sSeries.length;
	}
	if (fSeries != null) {
	    return fSeries.length;
	}
	if (dSeries != null) {
	    return dSeries.length;
	}
	return 0;
    }

    /** Creates a histogram of the values. Each value is added to the bin
	Math.floor((value-start)/width) and the returned int array has
	length number
    */
    public int[] histogram(double start, double width, int number) {
	int[] histo = new int[number];
	int bin;
	if (iSeries != null) {
	    for (int i=0; i< iSeries.length; i++) {
		bin = (int)Math.floor((iSeries[i]-start)/width);
		if (bin >= 0 && bin < number) {
		    histo[bin]++;
		} // end of if (bin >= 0 && bin < number)
	    } // end of for (int i=0; i< iSeries.length; i++)
	    return histo;	    
	}
	if (sSeries != null) {
	    for (int i=0; i< sSeries.length; i++) {
		bin = (int)Math.floor((sSeries[i]-start)/width);
		if (bin >= 0 && bin < number) {
		    histo[bin]++;
		} // end of if (bin >= 0 && bin < number)
	    } // end of for (int i=0; i< iSeries.length; i++)
	    return histo;	    
	}
	if (fSeries != null) {
	    for (int i=0; i< fSeries.length; i++) {
		bin = (int)Math.floor((fSeries[i]-start)/width);
		if (bin >= 0 && bin < number) {
		    histo[bin]++;
		} // end of if (bin >= 0 && bin < number)
	    } // end of for (int i=0; i< iSeries.length; i++)
	    return histo;	    
	}
	if (dSeries != null) {
	    for (int i=0; i< dSeries.length; i++) {
		bin = (int)Math.floor((dSeries[i]-start)/width);
		if (bin >= 0 && bin < number) {
		    histo[bin]++;
		} // end of if (bin >= 0 && bin < number)
	    } // end of for (int i=0; i< iSeries.length; i++)
	    return histo;	    
	}
	return new int[0];
    }

    protected double binarySum(int start, int finish) {
	if (iSeries != null) {
	    return iBinarySum(start, finish);
	} // end of if (iSeries != null)
	if (sSeries != null) {
	    return sBinarySum(start, finish);
	} // end of if (sSeries != null)
	if (fSeries != null) {
	    return fBinarySum(start, finish);
	} // end of if (fSeries != null)
	if (dSeries != null) {
	    return dBinarySum(start, finish);
	} // end of if (dSeries != null)
	return 0;
    }
	
    private double iBinarySum(int start, int finish) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += iSeries[i];
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return iBinarySum(start, middle) +
		iBinarySum(middle, finish);
	}
    }
	
    private double sBinarySum(int start, int finish) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += sSeries[i];
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return sBinarySum(start, middle) +
		sBinarySum(middle, finish);
	}
    }
	
    private double fBinarySum(int start, int finish) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += fSeries[i];
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return fBinarySum(start, middle) +
		fBinarySum(middle, finish);
	}
    }
	
    private double dBinarySum(int start, int finish) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += dSeries[i];
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return dBinarySum(start, middle) +
		dBinarySum(middle, finish);
	}
    }

    protected double binarySumDevSqr(int start, int finish, double mean) {
	if (iSeries != null) {
	    return iBinarySumDevSqr(start, finish, mean);
	} // end of if (iSeries != null)
	if (sSeries != null) {
	    return sBinarySumDevSqr(start, finish, mean);
	} // end of if (sSeries != null)
	if (fSeries != null) {
	    return fBinarySumDevSqr(start, finish, mean);
	} // end of if (iSeries != null)
	if (dSeries != null) {
	    return dBinarySumDevSqr(start, finish, mean);
	} // end of if (dSeries != null)
	return 0;
    }

    private double iBinarySumDevSqr(int start, int finish, double mean) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += (iSeries[i]-mean)*(iSeries[i]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return iBinarySumDevSqr(start, middle, mean) +
		iBinarySumDevSqr(middle, finish, mean);
	}
    }

    private double sBinarySumDevSqr(int start, int finish, double mean) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += (sSeries[i]-mean)*(sSeries[i]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return sBinarySumDevSqr(start, middle, mean) +
		sBinarySumDevSqr(middle, finish, mean);
	}
    }

    private double fBinarySumDevSqr(int start, int finish, double mean) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += (fSeries[i]-mean)*(fSeries[i]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return fBinarySumDevSqr(start, middle, mean) +
		fBinarySumDevSqr(middle, finish, mean);
	}
    }

    private double dBinarySumDevSqr(int start, int finish, double mean) {
	if (finish-start < 8) {
	    double val = 0;
	    for (int i=start; i< finish; i++) {
		val += (dSeries[i]-mean)*(dSeries[i]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return dBinarySumDevSqr(start, middle, mean) +
		dBinarySumDevSqr(middle, finish, mean);
	}
    }

    protected double binarySumDevLag(int start, int finish, 
				     double mean, int lag) {
	if (iSeries != null) {
	    return iBinarySumDevLag(start, finish, mean, lag);
	} // end of if (iSeries != null)
	if (sSeries != null) {
	    return sBinarySumDevLag(start, finish, mean, lag);
	} // end of if (sSeries != null)
	if (fSeries != null) {
	    return fBinarySumDevLag(start, finish, mean, lag);
	} // end of if (iSeries != null)
	if (dSeries != null) {
	    return dBinarySumDevLag(start, finish, mean, lag);
	} // end of if (dSeries != null)
	return 0;
    }

    private double iBinarySumDevLag(int start, int finish, 
				    double mean, int lag) {
	if (finish-start < lag+8) {
	    double val = 0;
	    for (int i=start; i< finish && i<getLength()-lag; i++) {
		val += (iSeries[i]-mean)*(iSeries[i+lag]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return iBinarySumDevLag(start, middle, mean, lag) +
		iBinarySumDevLag(middle, finish, mean, lag);
	}
    }

    private double sBinarySumDevLag(int start, int finish, 
				    double mean, int lag) {
	if (finish-start < lag+8) {
	    double val = 0;
	    for (int i=start; i< finish && i<getLength()-lag; i++) {
		val += (sSeries[i]-mean)*(sSeries[i+lag]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return sBinarySumDevLag(start, middle, mean, lag) +
		sBinarySumDevLag(middle, finish, mean, lag);
	}
    }

    private double fBinarySumDevLag(int start, int finish, 
				    double mean, int lag) {
	if (finish-start < lag+8) {
	    double val = 0;
	    for (int i=start; i< finish && i<getLength()-lag; i++) {
		val += (fSeries[i]-mean)*(fSeries[i+lag]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return fBinarySumDevLag(start, middle, mean, lag) +
		fBinarySumDevLag(middle, finish, mean, lag);
	}
    }

    private double dBinarySumDevLag(int start, int finish, 
				    double mean, int lag) {
	if (finish-start < lag+8) {
	    double val = 0;
	    for (int i=start; i< finish && i<getLength()-lag; i++) {
		val += (dSeries[i]-mean)*(dSeries[i+lag]-mean);
	    }
	    return val;
	} else {
	    int middle = (start + finish) / 2;
	    return dBinarySumDevLag(start, middle, mean, lag) +
		dBinarySumDevLag(middle, finish, mean, lag);
	}
    }

    public static void main(String[] args) {
	int[] testSeries = new int[10];
	testSeries[0] = 13;
	testSeries[1] = 8;
	testSeries[2] = 15;
	testSeries[3] = 4;
	testSeries[4] = 4;
	testSeries[5] = 12;
	testSeries[6] = 11;
	testSeries[7] = 7;
	testSeries[8] = 14;
	testSeries[9] = 12;
	Statistics s = new Statistics(testSeries);
	System.out.println("Mean = "+s.mean());
	System.out.println("Variance = "+s.var());
	double[] testACF = s.acf(5);
	for (int i=0; i<testACF.length; i++) {
	    System.out.println("acf "+i+" = "+testACF[i]);
	}
	double[] testPACF = s.pacf(5);
	for (int i=0; i<testPACF.length; i++) {
	    System.out.println("pacf "+i+" = "+testPACF[i]);
	}
    }

    protected int[] iSeries;

    protected short[] sSeries;

    protected float[] fSeries;

    protected double[] dSeries;

    protected boolean minMaxMeanCalculated;
    
    protected double variance;
    protected boolean varianceCalculated = false;
    
    protected double[] autocorrelation = new double[0];
    protected double[] partialautocorr = new double[0];
    protected double[] minMaxMean = new double[3];
    
    protected int beginIndex, endIndex;

} // Statistics
