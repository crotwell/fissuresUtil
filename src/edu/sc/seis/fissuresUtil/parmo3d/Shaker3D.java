
package edu.sc.seis.fissuresUtil.parmo3d;

/*
    Shaker3D is used to construct several objects necessary for ground motion
    animation using the Java3D API.
*/

import javax.media.j3d.LineArray;
import javax.media.j3d.PointArray;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

public class Shaker3D {

   public static final int SINE = 0;
   public static final int TRIANGLE = 1;
   public static final int BOX = 2;
   public static final int OTHER = 3;
   static final int NUMWAVES = 4;

   int numberOfPoints = 41;
   int wavetype = this.SINE;
   float backAzimuth = 0.0f;
   float angleOfIncidence = 0.0f;
   boolean centered = true;

       public Shaker3D() {

       }       

       public Shaker3D(int waveFlag, float baz, float angI) {

          this.backAzimuth = baz;
          this.angleOfIncidence = angI;
          this.setWaveType(waveFlag);

       }
       public Shaker3D(int waveFlag, int npts, float baz, float angI) {

          this.backAzimuth = baz;
          this.angleOfIncidence = angI;
          this.setWaveType(waveFlag);
          this.setNumPoints(npts);

       }

       public Shaker3D(int waveFlag) {

          this.setWaveType(waveFlag);
       }

       private Point3f rotateToZEN(double amplitude) {

/*  For this application, the axes correlate as follows:

       Z = x
       E = y
       N = z

       We start with an amplitude of a vector pointed in the
       direction defined by backAzimuth and angleOfIncidence
       and resolve this onto the Z-E-N coordinates.

       Results go into a Point3f object
*/

       double z = amplitude*Math.cos(Math.toRadians(angleOfIncidence));
       double horizProjection = amplitude*Math.sin(Math.toRadians(angleOfIncidence));
       double n = horizProjection*Math.cos(Math.toRadians(backAzimuth));
       double e = horizProjection*Math.sin(Math.toRadians(backAzimuth));

       Point3d dblresult = new Point3d(z,e,n);
       Point3f result = new Point3f(dblresult);

       return result;

       }

       public void setWaveType(int waveFlag) {

         if (waveFlag < NUMWAVES) {

            wavetype = waveFlag;

         } else {

	     System.out.println("Bad Wave Flag: " + waveFlag );

         }

       }

       public int getWaveType() {

         return wavetype;

       }

       public void setNumPoints(int numPoints) {

         numberOfPoints = numPoints;

       }

       public int getNumPoints() {

         return numberOfPoints;

       }

       public void getWavePoints(Point3f[] wavePoints) {

         if (wavetype == 0) {

            if (centered) {

               int halfwidth = numberOfPoints/2;
               int quarterwidth = halfwidth/2;
               
               int i = 0;

               while (i < quarterwidth) {

                   wavePoints[i] = new Point3f(0.0f,0.0f,0.0f);
                   wavePoints[i+halfwidth] = new Point3f(0.0f,0.0f,0.0f);
                   i++;

               }

               double inc = 2*Math.PI/halfwidth;

               while (quarterwidth <= i && i < 3*quarterwidth) {

                   double pt = (i - quarterwidth)*inc;
                   double value = Math.sin(pt);
                   wavePoints[i] = new Point3f();
                   wavePoints[i] = rotateToZEN(value);
                   i++;

               }

            } 
         }
       }

       public void getWaveLineArray(LineArray waveLineArray) {

         int totalVertex = waveLineArray.getVertexCount();

         if (wavetype == 0) {

            if (centered) {

               int halfwidth = numberOfPoints/2;
               int quarterwidth = halfwidth/2;
               
               System.out.println(quarterwidth + " " + halfwidth + " " + numberOfPoints);

               int i = 0;
               int vertex = 0;

               waveLineArray.setCoordinate(vertex, new Point3f(0.0f,0.0f,0.0f));

               i++;
               while (i <= quarterwidth) {

                   vertex++;
                   waveLineArray.setCoordinate(vertex, new Point3f(0.0f,0.0f,0.0f));
                   vertex++;
                   waveLineArray.setCoordinate(vertex, new Point3f(0.0f,0.0f,0.0f));
                   i++;

               }

               double inc = 2*Math.PI/halfwidth;

               vertex++;

               int phase = 0;
               while (quarterwidth < i && i <= 3*quarterwidth) {

                   double pt = phase*inc;
                   double value = Math.sin(pt);                  
                   Point3f point = rotateToZEN(value);

                   waveLineArray.setCoordinate(vertex, point);
                   vertex++;
                   waveLineArray.setCoordinate(vertex, point);
                   vertex++;
                   i++;
                   phase++;

               }

               while (i < numberOfPoints) {

                   waveLineArray.setCoordinate(vertex, new Point3f(0.0f,0.0f,0.0f));
                   vertex++;
                   waveLineArray.setCoordinate(vertex, new Point3f(0.0f,0.0f,0.0f));
                   vertex++;
                   i++;

               }
            } 
         }
         return;
       }

       public void getSeisPointArrays
               (PointArray zPointArray, PointArray ePointArray, PointArray nPointArray) {

/*  Set locations as the lower half of the frame
    Max scale = 0.2
    Variations are in the y-plane, "time" is along the x-axis
*/
         float xstart = -.6f;
         float xinc = -2*xstart/(numberOfPoints-1);
         float zbase = -.32f;
         float nbase = -.56f;
         float ebase = -.8f;
         float scale = 0.2f;
         Point3f zPoints[] = new Point3f[numberOfPoints];
         Point3f nPoints[] = new Point3f[numberOfPoints];
         Point3f ePoints[] = new Point3f[numberOfPoints];

         if (wavetype == 0) {

            if (centered) {

               int halfwidth = numberOfPoints/2;
               int quarterwidth = halfwidth/2;
               
  //             System.out.println(quarterwidth + " " + halfwidth + " " + numberOfPoints);

               int i = 0;

               zPoints[i]=new Point3f(xstart,zbase,0.0f);
               nPoints[i]=new Point3f(xstart,nbase,0.0f);
               ePoints[i]=new Point3f(xstart,ebase,0.0f);
   //            System.out.println(i + " " + xstart + " " + zbase + " " + nbase + " " + ebase); 

               i++;

               while (i <= quarterwidth) {

                   float xpt = xstart + i*xinc;
                   zPoints[i]=new Point3f(xpt,zbase,0.0f);
                   nPoints[i]=new Point3f(xpt,nbase,0.0f);
                   ePoints[i]=new Point3f(xpt,ebase,0.0f);

         //      System.out.println(i + " " + xpt + " " + zbase + " " + nbase + " " + ebase); 

                   i++;

               }

               double inc = 2*Math.PI/halfwidth;

          //     System.out.println("Starting sine wave: " + i);

               int phase = 1;
               while (quarterwidth < i && i <= 3*quarterwidth) {

                   double pt = phase*inc;
                   double value = Math.sin(pt);                  
                   Point3f value3D = new Point3f();
                   value3D = rotateToZEN(value);
                   float zen[] = new float[3];
                   value3D.get(zen);

                   float xpt = xstart + i*xinc;
                   float zValue = zbase + scale*zen[0];
                   float nValue = nbase + scale*zen[1];
                   float eValue = ebase + scale*zen[2];

                   zPoints[i]=new Point3f(xpt,zValue,0.0f);
                   nPoints[i]=new Point3f(xpt,nValue,0.0f);
                   ePoints[i]=new Point3f(xpt,eValue,0.0f);

     //          System.out.println(i + " " + xpt + " " + zValue + " " + nValue + " " + eValue); 
                   i++;
                   phase++;

               }

//               System.out.println("Ending sine wave: " + i);

               while (i < numberOfPoints) {

                   float xpt = xstart + i*xinc;

      //         System.out.println(i + " " + xpt + " " + zbase + " " + nbase + " " + ebase); 

                   zPoints[i]=new Point3f(xpt,zbase,0.0f);
                   nPoints[i]=new Point3f(xpt,nbase,0.0f);
                   ePoints[i]=new Point3f(xpt,ebase,0.0f);

                   i++;

               }
               int vertex = zPointArray.getVertexCount();
               ePointArray.setCoordinates(0,ePoints);
               nPointArray.setCoordinates(0,nPoints);
               zPointArray.setCoordinates(0,zPoints);
               i=0;
               while ( i < numberOfPoints ) {

        //         System.out.println(zPoints[i] + " " + nPoints[i] + " " + ePoints[i]);
                 i++;
               }

            } 
         }
         return;
       }

    public static void main(String[] args) {

       Shaker3D shake = new Shaker3D();
       float num;

       Shaker3D shake2 = new Shaker3D(Shaker3D.BOX);

       num = shake2.getWaveType();

    } // end of main method of Shaker3D

} // end of class Shaker3D
