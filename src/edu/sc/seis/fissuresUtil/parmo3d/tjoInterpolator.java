package edu.sc.seis.fissuresUtil.parmo3d;

import java.awt.event.KeyEvent;
import java.util.Enumeration;
import javax.media.j3d.Alpha;
import javax.media.j3d.PositionPathInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.vecmath.Point3f;

public class tjoInterpolator extends PositionPathInterpolator {

    public tjoInterpolator(Alpha alpha, TransformGroup target, Transform3D axisOfTranslation, 
                           float[] knots, Point3f[] positions) {

           super(alpha, target, axisOfTranslation, knots, positions);
	   //System.out.println("after the call to the super class");
    }
    public void processStimulus(Enumeration criteria) {
           super.processStimulus(criteria);
           this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
    }

}
