package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalMotionVectorImpl;
import edu.iris.Fissures.IfSeismogramDC.VectorComponent;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Rotate.java
 *
 *
 * Created: Sun Dec 15 13:43:21 2002
 *
 * @author Philip Crotwell
 * @version $Id: Rotate.java 3839 2003-05-09 00:35:49Z crotwell $
 */
public class Rotate implements LocalMotionVectorFunction {
    
    public Rotate() {
        
    } // Rotate constructor
    
    public LocalMotionVector apply(LocalMotionVector vec) {
        VectorComponent[] data = new VectorComponent[3];
        
        
        return new  LocalMotionVectorImpl(vec.get_id(),
                                          vec.properties,
                                          vec.begin_time,
                                          vec.num_points,
                                          vec.sampling_info,
                                          vec.y_unit,
                                          vec.channel_group,
                                          vec.parm_ids,
                                              (TimeInterval[])vec.time_corrections,
                                          vec.sample_rate_history,
                                          data);
    }
    
    /**
     * Rotates x and y by the given angle theta. The x and y axis are
     * assumed to be perpendicular. Theta, in degrees, is positive from
     * x towards y, and so a rotation of 90 puts x into y and
     * -y into x.
     */
    public static void rotate(float[] x, float[] y, float theta) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must have the same length. "+x.length+" "+y.length);
        }
        float tempx, tempy;
        double h;
        double phi;
        for (int i=0; i<x.length; i++) {
            tempx = x[i];
            tempy = y[i];
            h = Math.sqrt(tempx*tempx+tempy*tempy);
            phi = Math.atan2(tempy, tempx);
            x[i] = (float)(h*Math.cos(phi+dtor(theta)));
            y[i] = (float)(h*Math.sin(phi+dtor(theta)));
        }
    }
    
    public static void rotate(float[] x,
                              float[] y,
                              float[] z,
                              double theta,
                              double phi) {
        
    }
    
    public static double dtor(double degree) {
        return Math.PI*degree/180.0;
    }
    
    public static double rtod(double radian) {
        return radian*180.0/Math.PI;
    }
    
} // Rotate
