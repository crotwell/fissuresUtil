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

 public class RTTimeRangeConfig extends AbstractTimeRangeConfig{
    public RTTimeRangeConfig (){
	super();
	setDisplayInterval(width);
	beginTime = new MicroSecondDate(0);
   }

    /**
     * Takes the information from the passed seismogram and uses it along with the information already taken from other seismograms 
     * along with an internal set of calculations to determine the amount of time the passed seismogram will be displayed
     *
     * @param seis the seismogram to be displayed
     * @return the time it will be displayed
     */
     public MicroSecondTimeRange getTimeRange(LocalSeismogram seis) {
	 /* MicroSecondDate curr = (MicroSecondDate)(seismos.get(seis));
	 MicroSecondDate tempTime = curr.add(displayInterval);
	 MicroSecondDate endTime = tempTime.add(displayInterval);
	 beginTime = tempTime;*/
	 MicroSecondDate endTime = beginTime.add(displayInterval);
	 MicroSecondDate tempTime = beginTime;
	 return new MicroSecondTimeRange(beginTime, endTime);
     }

    /**
     * Get the total display time regardless of a particular seismogram, for things such as axes
     *
     * @return the time range being displayed
     */
     public MicroSecondTimeRange getTimeRange() {

	 MicroSecondDate endTime = beginTime.add(displayInterval);
	 MicroSecondDate tempTime = beginTime;
	 //this.beginTime = endTime;
	 System.out.println("The function getTimeRange is Called "+tempTime+" end Time is "+beginTime);
 
	 return new MicroSecondTimeRange(tempTime, endTime);
     }

    
    /**
     * Takes the information from the TimeSyncEvent, adjusts the MicroSecondTimeRange, and updates according to the information in the 
     * event
     *
     */
     public  void fireTimeRangeEvent(TimeSyncEvent e) {

     }

     public void startTimer() {
	 
	 if (timer == null) {
	     timer = 
		new javax.swing.Timer(1000,
			  new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
				  width = new TimeInterval(speed, UnitImpl.SECOND);
				  setBeginTime(beginTime.add(width));
				  // updateTimeSyncListeners();
			      }
			  });//20000
	     timer.setCoalesce(true); 
	     timer.start();
	 }
     }

     public void setSpeed(float speed) {

	 this.speed = speed;
     }

     private float speed = 1;

     /** Timers are used for realTime update of the Seismograms **/
     protected javax.swing.Timer timer;

     protected javax.swing.Timer reloadTimer;

     protected TimeInterval width = new TimeInterval(1, UnitImpl.SECOND);

           
}// RTTimeRangeConfig
