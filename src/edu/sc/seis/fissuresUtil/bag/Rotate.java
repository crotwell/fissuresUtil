package edu.sc.seis.fissuresUtil.bag;

import java.awt.geom.AffineTransform;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.iris.Fissures.IfSeismogramDC.VectorComponent;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalMotionVectorImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Rotate.java
 *
 *
 * Created: Sun Dec 15 13:43:21 2002
 *
 * @author Philip Crotwell
 * @version $Id: Rotate.java 10257 2004-08-31 13:47:25Z groves $
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

    /** rotates the two seismograms to the great circle path radial and transverse.
     It is assumed that
     the two seismograms orientation are perpendicular to each other and
     are oriented in the east, x and north, y, directions.
     @returns the rotated data from the two seismograms, index 0 is
     the tangential and index 1 is the radial.*/
    public static float[][] rotateGCP(LocalSeismogramImpl x,
                                      LocalSeismogramImpl y,
                                      Location staLoc,
                                      Location evtLoc) throws FissuresException  {
        DistAz distAz = new DistAz(staLoc, evtLoc);
        return Rotate.rotate(x, y, dtor(180+distAz.getBaz()));
    }

    /** rotates the two seismograms by the given angle. It is assumed that
     the two seismograms orientation are perpendicular to each other and
     that the sense of the rotation is from x towards y.
     @returns the rotated data from the two seismograms, index 0 is
     the new x and index 1 is the new y.*/
    public static float[][] rotate(LocalSeismogramImpl x,
                                   LocalSeismogramImpl y,
                                   double radians) throws FissuresException  {
        float[][] data = new float[2][];
        float[] temp = x.get_as_floats();
        data[0] = new float[temp.length];
        System.arraycopy(temp, 0, data[0], 0, temp.length);
        temp = y.get_as_floats();
        data[1] = new float[temp.length];
        System.arraycopy(temp, 0, data[1], 0, temp.length);
        rotate(data[0], data[1], radians);
        return data;
    }

    /**
     * Rotates the x and y arrays by the given angle in radians. The x and y axis are
     * assumed to be perpendicular. Theta, in radians, is positive from
     * x towards y, and so a rotation of PI/2 puts x into y and
     * -y into x.
     */
    public static void rotate(float[] x, float[] y, double radians) {

        rotate(x, y, AffineTransform.getRotateInstance(radians));
    }

    /** Performs the rotation from the given matrix. It is assumed to be
     a pure rotation matrix (no translation) and the translation components
     of the affine transform are ignored if present. */
    public static void rotate(float[] x, float[] y, AffineTransform affine) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("x and y must have the same length. "+x.length+" "+y.length);
        }
        float tempx, tempy;
        // matrix is m00 m10 m01 m11 where the matrix is
        //           m00 m01
        //           m10 m11

        double[] matrix = new double[4];
        affine.getMatrix(matrix);
        for (int i=0; i<x.length; i++) {
            tempx = x[i];
            tempy = y[i];
            x[i] = (float)(tempx*matrix[0] + tempy*matrix[2]);
            y[i] = (float)(tempx*matrix[1] + tempy*matrix[3]);
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
