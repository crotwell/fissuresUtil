package edu.sc.seis.fissuresUtil.bag;

/**
 * Taper.java
 *
 *
 * Created: Sat Oct 19 21:53:21 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version $Id: Taper.java 2782 2002-10-20 12:08:41Z crotwell $
 */

public class Taper {
    public Taper (){
	this(0.05f);
    }

    public Taper (float width){
	this(HANNING, width);
    }

    public Taper (int type, float width){
	this.type = type;
	this.width = width;
	
    }

    public void apply(float[] data) {
	int w = Math.round(data.length*width);
	float f0;
	float f1;
	double omega;

	if (type == HANNING) {
	    omega = Math.PI/w;
	    f0 = .5f;
	    f1 = .5f;
	} else if (type == HANNING) {
	    omega = Math.PI/w;
	    f0 = .54f;
	    f1 = .46f;
	} else {
	    // cosine
	    omega = Math.PI/2/w;
	    f0 = 1;
	    f1 = 1;
	}
	for (int i=0; i < w ; i++) {
	    data[i] = (float)(data[i] * (f0 - f1 * Math.cos(omega*i))); 
	    data[data.length-i] = 
		(float)(data[data.length-i] * (f0 - f1 * Math.cos(omega*i))); 
	} // end of for (int i=0; i<data.length; i++)
	
    }
    
    public static int HANNING = 0;

    public static int HAMMING = 1;

    public static int COSINE = 2;

    float width;

    int type;



}// Taper
