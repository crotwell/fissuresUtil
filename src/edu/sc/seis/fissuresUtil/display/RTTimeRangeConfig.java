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
	this(seismos, new TimeInterval(.125, UnitImpl.SECOND));
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
    }
    
    public void add(DataSetSeismogram[] seismos){
	if(time == null){
	    time = new MicroSecondTimeRange(seismos[0].getSeismogram().getBeginTime(), 
					    seismos[0].getSeismogram().getEndTime().subtract(threeMinutes).add(serverTimeOffset));
	}
	super.add(seismos);
    }

    public void startTimer() {
	if (timer == null) {
	    timer = 
		new javax.swing.Timer((int)update.convertTo(UnitImpl.MILLISECOND).value,
		     new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
			     if (speed != 0 && lastDate != null) {
				 MicroSecondDate now = new MicroSecondDate();
				 TimeInterval timeInterval = new TimeInterval(lastDate, now);
				 lastDate = now;
				 shaleTime(timeInterval.getValue()/time.getInterval().getValue() * speed, 1);
				 //logger.debug("Timer: updateTimeSyncListeners()  speed="+speed);
			     } else{
				 lastDate = new MicroSecondDate();
			     }
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

    private static TimeInterval getServerTimeOffset(){
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
	    MicroSecondDate localTime = new MicroSecondDate();
	    edu.iris.Fissures.Time serverTime = new edu.iris.Fissures.Time();
	    if(timeStr != null) {
		serverTime = new edu.iris.Fissures.Time(timeStr, -1);
	    }
	    MicroSecondDate serverDate = new MicroSecondDate(serverTime);
	    //System.out.println("server Date is "+serverDate);
	    //System.out.println("the lastDate is "+this.lastDate);
	    TimeInterval offset = new TimeInterval(localTime, serverDate);
	    if(java.lang.Math.abs(offset.value) <  2000000) offset = new TimeInterval(serverDate, serverDate);
	    //System.out.println("The offset is "+offset.value);
	    return offset;
	} 
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }	

    private TimeInterval update;

    public static final TimeInterval serverTimeOffset = RTTimeRangeConfig.getServerTimeOffset();

    private MicroSecondDate lastDate;

    private float speed = 1;

    /** Timers are used for realTime update of the Seismograms **/
    protected javax.swing.Timer timer;

    protected javax.swing.Timer reloadTimer;

    protected TimeInterval width;

    private static Category logger = Category.getInstance(RTTimeRangeConfig.class.getName());

    private static long MINUTE = 60 * 1000 * 1000;

    private static TimeInterval threeMinutes = new TimeInterval(new MicroSecondDate(0), new MicroSecondDate(3 * MINUTE));
}// RTTimeRangeConfig
