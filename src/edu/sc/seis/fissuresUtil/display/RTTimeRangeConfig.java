package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
/**
 * RTTimeRangeConfig.java
 *
 *
 * Created: Mon Jun  3 15:47:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class RTTimeRangeConfig extends BoundedTimeConfig{
    public RTTimeRangeConfig (){
	this(new TimeConfigRegistrar());
    }

    public RTTimeRangeConfig (TimeConfigRegistrar reg){
	this(reg, new TimeInterval(1, UnitImpl.SECOND));
    }

    public RTTimeRangeConfig (TimeConfigRegistrar reg, TimeInterval update){
	this(reg, update, 1);
    }

    public RTTimeRangeConfig (TimeConfigRegistrar reg, 
			      TimeInterval update, 
			      float speed){
	super(reg);
	reg.setTimeConfig(this);
	this.update = update;
	this.speed = speed;
    }

    public void startTimer() {
	 
	if (timer == null) {
	    timer = 
		new javax.swing.Timer((int)update.convertTo(UnitImpl.MILLISECOND).value,
		     new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
			     width = (TimeInterval)update.multiplyBy(speed);
			     if (beginTime != null) {
				 setAllBeginTime(beginTime.add(width));
				 getRegistrar().updateTimeSyncListeners();
				 System.out.println("Timer: updateTimeSyncListeners()  speed="+speed);
			     } // end of if (beginTime != null)
			 }
		     });
	    timer.setCoalesce(true); 
	    timer.start();
	}
    }

    public void setSpeed(float speed) {
	this.speed = speed;
    }

    public float getSpeed() {
	return speed;
    }

    private TimeInterval update;

    private float speed = 1;

    /** Timers are used for realTime update of the Seismograms **/
    protected javax.swing.Timer timer;

    protected javax.swing.Timer reloadTimer;

    protected TimeInterval width = new TimeInterval(1, UnitImpl.SECOND);

           
}// RTTimeRangeConfig
