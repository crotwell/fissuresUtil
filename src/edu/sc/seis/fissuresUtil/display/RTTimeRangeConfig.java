package edu.sc.seis.fissuresUtil.display;


import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.*;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import org.apache.log4j.*;
/**
 * RTTimeRangeConfig.java
 *
 *
 * Created: Mon Jun  3 15:47:31 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class RTTimeRangeConfig extends BasicTimeConfig{
    
    public RTTimeRangeConfig(DataSetSeismogram[] seismos){
	this(seismos, new TimeInterval(1, UnitImpl.SECOND));
    }

    public RTTimeRangeConfig (DataSetSeismogram[] seismos, 
			      TimeInterval update){
	this(seismos, update, 1);
    }

    public RTTimeRangeConfig(DataSetSeismogram[] seismos, 
			TimeInterval update, 
			float speed){
	super(seismos);
	this.update = update;
	this.speed = speed;
	this.lastDate = new MicroSecondDate();
	try {
	    URL url = new URL("http://www.seis.sc.edu/cgi-bin/date_time.pl");
	    InputStream is = url.openStream();
	    InputStreamReader isReader = new InputStreamReader(is);
	    BufferedReader bufferedReader = new BufferedReader(isReader);
	    String str;
	    String timeStr = null;
	    while((str = bufferedReader.readLine()) != null) {
		timeStr = str;
	    }
	    this.lastDate = new MicroSecondDate();
	    edu.iris.Fissures.Time serverTime = new edu.iris.Fissures.Time();
	    if(timeStr != null) {
		serverTime = new edu.iris.Fissures.Time(timeStr, -1);
	    }
	    MicroSecondDate serverDate = new MicroSecondDate(serverTime);
	    System.out.println("server Date is "+serverDate);
	    System.out.println("the lastDate is "+this.lastDate);
	    offset = new TimeInterval(this.lastDate, serverDate);
	    if(java.lang.Math.abs(offset.value) <  2000000) offset = new TimeInterval(serverDate, serverDate);
	    System.out.println("The offset is "+offset.value);
	} catch(Exception e) {e.printStackTrace();}
    }

    public void startTimer() {
	if (timer == null) {
	    timer = 
		new javax.swing.Timer((int)update.convertTo(UnitImpl.MILLISECOND).value,
		     new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
			     if (speed != 0) {
				 MicroSecondDate now = new MicroSecondDate().add(offset);
				 TimeInterval timeInterval = new TimeInterval(lastDate, now);
				 width = (TimeInterval)timeInterval.multiplyBy(speed);
				 lastDate = now;
				 shaleTime(timeInterval.getValue()/time.getInterval().getValue() * speed, 1);
				 logger.debug("Timer: updateTimeSyncListeners()  speed="+speed);
			     } // end of if (beginTime != null)
			 }
		     });
	    timer.setCoalesce(true); 
	    timer.start();
	}
    }

    public void stopTimer(){
	if(timer != null){
	    timer.stop();
	    timer = null;
	}
    }

    public void reset(){
	speed = 1;
	stopTimer();
	super.reset();
	startTimer();
    }
    
    public void setSpeed(float speed) {
	this.speed = speed;
    }

    public float getSpeed() {
	return speed;
    }

    private TimeInterval update;

    private TimeInterval offset;

    private MicroSecondDate lastDate;

    private float speed = 1;

    /** Timers are used for realTime update of the Seismograms **/
    protected javax.swing.Timer timer;

    protected javax.swing.Timer reloadTimer;

    protected TimeInterval width = new TimeInterval(1, UnitImpl.SECOND);

    private static Category logger = Category.getInstance(RTTimeRangeConfig.class.getName());
}// RTTimeRangeConfig
