package edu.sc.seis.fissuresUtil.parmo3d;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.*;
import java.awt.Font;
import com.sun.j3d.utils.applet.MainFrame; 
// import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.Enumeration;

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
