
package edu.sc.seis.fissuresUtil.bag;

/**
 * Statistics.java
 *
 *
 * Created: Wed Apr  4 22:27:52 2001
 *
 * @author Philip Crotwell
 * @version $Id: Statistics.java 2663 2002-10-02 21:52:15Z crotwell $
 */

public class Statistics  {
    
    public Statistics(int[] iSeries) {
	this.iSeries = iSeries;
    }

    public Statistics(float[] fSeries) {
	this.fSeries = fSeries;
    }

    public double min() {
	double min=Double.MAX_VALUE;
	if (iSeries != null) {
	    for (int i=0; i<iSeries.length; i++) {
		min = Math.min(min, iSeries[i]);
	    } // end of for (int i=0; i<iSeries.length; i++)
	} else if (fSeries != null) {
	    for (int i=0; i<fSeries.length; i++) {
		min = Math.min(min, fSeries[i]);
	    } // end of for (int i=0; i<iSeries.length; i++)
	}
	return min;
    }

    public double max() {
	double max=-1*Double.MAX_VALUE;
	if (iSeries != null) {
	    for (int i=0; i<iSeries.length; i++) {
		max = Math.max(max, iSeries[i]);
	    } // end of for (int i=0; i<iSeries.length; i++)
	} else if (fSeries != null) {
	    for (int i=0; i<fSeries.length; i++) {
		max = Math.max(max, fSeries[i]);
	    } // end of for (int i=0; i<iSeries.length; i++)
	}
	return max;
    }

    public double mean() {
	if ( ! meanCalculated) {
	    meanVal = binarySum(0, getLength())/getLength();
	    meanCalculated = true;
	}
	return meanVal;
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
	if (fSeries != null) {
	    return fSeries.length;
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
	if (fSeries != null) {
	    for (int i=0; i< fSeries.length; i++) {
		bin = (int)Math.floor((fSeries[i]-start)/width);
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
	if (fSeries != null) {
	    return fBinarySum(start, finish);
	} // end of if (iSeries != null)
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

    protected double binarySumDevSqr(int start, int finish, double mean) {
	if (iSeries != null) {
	    return iBinarySumDevSqr(start, finish, mean);
	} // end of if (iSeries != null)
	if (fSeries != null) {
	    return fBinarySumDevSqr(start, finish, mean);
	} // end of if (iSeries != null)
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

    protected double binarySumDevLag(int start, int finish, 
				     double mean, int lag) {
	if (iSeries != null) {
	    return iBinarySumDevLag(start, finish, mean, lag);
	} // end of if (iSeries != null)
	if (fSeries != null) {
	    return fBinarySumDevLag(start, finish, mean, lag);
	} // end of if (iSeries != null)
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

    protected float[] fSeries;

    protected double meanVal;
    protected boolean meanCalculated = false;

    protected double variance;
    protected boolean varianceCalculated = false;
    
    protected double[] autocorrelation = new double[0];
    protected double[] partialautocorr = new double[0];

} // Statistics
