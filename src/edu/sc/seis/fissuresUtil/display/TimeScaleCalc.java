package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.*;
import java.text.*;
import java.util.*;

/**
 * TimeScaleCalc takes the total pixels of a given seismogram along with its 
 * beginning and ending times to make divisions that are decently human 
 * friendly for the time axis.
 *
 *
 * Created: Thu May 16 13:36:24 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class TimeScaleCalc extends TimeScaleMapper {
    private int majTickRatio, majTickNum;
    protected long timeIntv;//time between beginTime and endTime in microseconds
    protected long divInc;//time between major ticks in microseconds
    private double timeInc;//tick increment in microseconds
    private double majTickIntv;//the interval in pixels between major ticks
    protected DateFormat dateFormat, dateTimeFormat;
    protected SimpleDateFormat longTimeFormat, mediumTimeFormat, shortTimeFormat;
    protected Calendar calendar;
    
    /**
       @param totalPixels the width of the axis being used in pixels
       @param beginTime the start time of the axis
       @param endTime the end time of the axis
    */
    TimeScaleCalc (int totalPixels, MicroSecondDate beginTime, MicroSecondDate endTime){
	super(totalPixels, beginTime, endTime);
	dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.FULL);
	dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	longTimeFormat = new SimpleDateFormat("HH:mm:ss.S");
	longTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	mediumTimeFormat = new SimpleDateFormat("mm:ss.S");
	mediumTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	shortTimeFormat = new SimpleDateFormat("ss.S");
	shortTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calculateTicks();
    }

    /**
     Uses the time interval for the seismogram and passes a rough time division based on this to
     divCalculateTicks
    */
    public void calculateTicks(){
	timeIntv = (endTime.getMicroSecondTime() - beginTime.getMicroSecondTime())/ 1000;
	
	if(timeIntv <= 100)
	    this.divCalculateTicks(10);
	else if(timeIntv <= 500)
	    this.divCalculateTicks(50);
	else if(timeIntv <= 100)
	    this.divCalculateTicks(100);
	else if(timeIntv <= 5000)
	    this.divCalculateTicks(500);
	else if(timeIntv <= 10000)
	    this.divCalculateTicks(1000);
	else if(timeIntv <= 30000)
	    this.divCalculateTicks(3000);
	else if(timeIntv <= 60000)
	    this.divCalculateTicks(10000);
	else if(timeIntv <= 120000)
	    this.divCalculateTicks(20000);
	else if(timeIntv <= 300000)
	    this.divCalculateTicks(30000);
	else if(timeIntv <= 600000)
	    this.divCalculateTicks(60000);
	else if(timeIntv <= 1200000)
	    this.divCalculateTicks(120000);
	else if(timeIntv <= 3600000)
	    this.divCalculateTicks(360000);
	else if(timeIntv <= 7300000)
	    this.divCalculateTicks(720000);
	else
	    this.divCalculateTicks(1440000);
	
    }
    
    /**
       Takes the rough division interval from calculateTicks() and turns it into a working interval based on the
       number of pixels.  Then it determines the number of major ticks, total ticks, and the various time and pixel
       intervals between them.
       @param divIntv a rough estimate of time in between major ticks in microseconds from calculateTicks()
    */
    public void divCalculateTicks(long divIntv){
	int  maxMajTickNum = totalPixels/75;//calculates the maximum number of major ticks possible 
	while(timeIntv / divIntv > maxMajTickNum)//adjusts the division so it fits the major tick number
	    divIntv *= 2;
	majTickNum = (int)(timeIntv/divIntv);
	divInc = divIntv;
	majTickIntv = (totalPixels + totalPixels * ((majTickNum * divIntv - timeIntv)/(double)timeIntv))/((double)majTickNum++);
	if(majTickIntv/10 > 10)
	    numTicks = totalPixels/(int)majTickIntv * 12;
	else if(majTickIntv/5 > 10)
	    numTicks = totalPixels/(int)majTickIntv * 6;
	else if(majTickIntv/2 > 10)
	    numTicks = totalPixels/(int)majTickIntv * 2;
	else
	    numTicks = majTickNum;
	tickInc = majTickIntv/(numTicks/majTickNum);//get the tick increment in pixels
	timeInc = divIntv/(double)(numTicks/majTickNum)*1000;//tick increment in microseconds
	majTickRatio = numTicks/majTickNum--;
    }
	
    /**
       @returns the long time if  75 pixels are between the major ticks, else it returns a shortened version of the time 
       @param i the current tick
    */
    public String getLabel(int i){
	if (isLabelTick(i)) {
	    if(majTickIntv > 75 || i == 0 || divInc > 120000){
		MicroSecondDate date = new MicroSecondDate(beginTime.getMicroSecondTime()+(long)(i*timeInc));
		calendar.setTime(date);
		return longTimeFormat.format(calendar.getTime());
	    }
	    else if(divInc <= 10000){
		MicroSecondDate date = new MicroSecondDate(beginTime.getMicroSecondTime()+(long)(i*timeInc));
		calendar.setTime(date);
		return shortTimeFormat.format(calendar.getTime());
	    }
	    else{
		MicroSecondDate date = new MicroSecondDate(beginTime.getMicroSecondTime()+(long)(i*timeInc));
		calendar.setTime(date);
		return mediumTimeFormat.format(calendar.getTime());
	    }
	}
	else
            return "";
    }

    /**
       @returns the location of the tick i in pixels
       @param i the current tick
    */
    public int getPixelLocation(int i){
	return (int)(i*tickInc);
    }

    /**
       @returns the number of ticks
    */
    public int getNumTicks(){	return numTicks;}

    /**
       @returns if tick i is labeled
       @param i the current tick
    */
    public boolean isLabelTick(int i){
	 if (i%majTickRatio == 0 && (i/majTickRatio != majTickNum || totalPixels - this.getPixelLocation(i) > 40))
	     return true;
	 return false;
    }

    /**
       @returns if the tick i is major
       @param i the current tick
    */
    public boolean isMajorTick(int i){
	if (i%majTickRatio == 0 && (i/majTickRatio != majTickNum || totalPixels - this.getPixelLocation(i) > 40))
	    return true;
	return false;
    }
		
}// TimeScaleCalc
