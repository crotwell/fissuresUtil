package edu.sc.seis.fissuresUtil.bag;

/**
 * RMean.java
 *
 *
 * Created: Sat Oct 19 21:54:26 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: RMean.java 2787 2002-10-21 01:10:16Z crotwell $
 */

public class RMean {
    public static LocalSeismogram removeMean(LocalSeismogram seis){
	if(seismo.can_convert_to_float()){
	    float[] fSeries = seis.get_as_floats();
	    return new LocalSeismogramImpl(seis, removeMean(fSeries));
	}else{
	    int[] iSeries = seismo.get_as_longs();
	    return new LocalSeismogramImpl(seis, removeMean(iSeries));
	}
    }
    
    public static float[] removeMean(float[] data) {
	float[] out = new float[data.length];
	removeMeanInPlace(out);
	return out;
    }
    
    public static void removeMeanInPlace(float[] data) {
	Statistics stat = new Statistics(data);
	float mean = (float)stat.mean();
	for (int i=0; i<data.length; i++) {
	    data[i] -= mean;
	} // end of for (int i=0; i<data.length; i++)

    }

    public static int[] removeMean(int[] data) {
	int[] out = new int[data.length];
	System.arraycopy(data, 0, out, 0, data.length);
	removeMeanInPlace(out);
	return out;
    }

    public static void removeMeanInPlace(int[] data) {
	Statistics stat = new Statistics(data);
	int mean = Math.round(stat.mean());
	for (int i=0; i<data.length; i++) {
	    data[i] -= mean;
	} // end of for (int i=0; i<data.length; i++)
	return data;
    }

}// RMean
