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
 * @version 0.2
 */

public class TimeScaleCalc implements ScaleMapper, TimeSyncListener {
    /**
       @param totalPixels the width of the axis being used in pixels

    */
    TimeScaleCalc (int totalPixels, TimeConfigRegistrar tr){
        this.totalPixels = totalPixels;
        this.timeRegistrar = new TimeConfigRegistrar(tr, this);
        setTimes();
    }

    public void calculateTicks(){
        int majTickNum = totalPixels/50;
        majTickTime = timeIntv/majTickNum;
        majTickRatio = 10;
        if(majTickTime <= 100000){
            timeFormat = new SimpleDateFormat("ss.S");
            if(majTickTime <= 1000){
                majTickTime = 1000;
            }else if(majTickTime <= 10000){
                majTickTime = 10000;
            }else if(majTickTime <= 100000){
                majTickTime = 100000;	
            }
        }else if(majTickTime <= 30 * 1000000){
            majTickRatio = 10;
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= 1000000){
                majTickTime = 1000000;
            }else if(majTickTime <= 5000000){
                majTickTime = 5000000;
            }else if(majTickTime <= 1000000){
                majTickTime = 10000000;
            }else
                majTickTime = 30000000;
        }
        else if(majTickTime <= 180000000){
            majTickRatio = 6;
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= 60000000){
                majTickTime = 60000000;
            }else if(majTickTime <= 120000000){
                majTickTime = 120000000;
            }else
                majTickTime = 180000000;
        }
        else if(majTickTime <= 1800000000){
            majTickRatio = 10;
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= 300000000)
                majTickTime = 300000000;
            else if(majTickTime <= 600000000)
                majTickTime = 600000000;
            else if(majTickTime <= 1200000000){
                majTickTime = 1200000000;
            }else             
                majTickTime = 1800000000;
        }
        else if(majTickTime <= 43200000000l){
            majTickRatio = 6;
            timeFormat = new SimpleDateFormat("MM/dd HH:mm");
            if(majTickTime <= 3600000000l){
                majTickTime = 3600000000l;
            }else if(majTickTime <= 7200000000l){
                majTickTime = 7200000000l;
            }else if(majTickTime <= 10800000000l){
                majTickTime = 10800000000l;
            }else if(majTickTime <= 21600000000l){
                majTickTime = 21600000000l;
            }else
                majTickTime = 43200000000l;
        }
        else{
            majTickRatio = 6;
            timeFormat = new SimpleDateFormat("MM/dd");
            majTickTime = 86400000000l;
        }
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        double numTicksDbl = ((timeIntv/(double)majTickTime) * majTickRatio);
        numTicks = (int)numTicksDbl;
        firstLabelTime = (beginTime/majTickTime + 1) * majTickTime;
        majTickOffset = (int)((firstLabelTime - beginTime)/(double)timeIntv * numTicks);
        tickOffset = (firstLabelTime - beginTime)/(double)timeIntv/majTickRatio * totalPixels;
        tickSpacing = totalPixels/numTicksDbl;
    }

    public void  setTotalPixels(int totalPixels) {
        this.totalPixels = totalPixels;
        calculateTicks();
    }
    
    public void setTimes(MicroSecondDate beginTime,
                         MicroSecondDate endTime) {
        this.beginTime = beginTime.getMicroSecondTime();
        this.endTime = endTime.getMicroSecondTime();
        timeIntv = (this.endTime - this.beginTime);
        calculateTicks();
    }

    public void setTimes(){
        setTimes(timeRegistrar.getTimeRange().getBeginTime(),
                 timeRegistrar.getTimeRange().getEndTime());
    }
    
    /**
       @returns the long time if  75 pixels are between the major ticks, else it returns a shortened version of the time 
       @param i the current tick
    */
    public  String getLabel(int i){
        if (isLabelTick(i)) {
            MicroSecondDate date = new MicroSecondDate((long)(firstLabelTime + i/majTickRatio * majTickTime));
            calendar.setTime(date);
            return timeFormat.format(calendar.getTime());
	    }
        return "";
    }

    /**
       @returns the location of the tick i in pixels
       @param i the current tick
    */
    public  int getPixelLocation(int i){ return (int)(i*tickSpacing + tickOffset); }

    /**
       @returns the number of ticks
    */
    public  int getNumTicks(){ return numTicks; }

    /**
       @returns if tick i is labeled
       @param i the current tick
    */
    public  boolean isLabelTick(int i){ return isMajorTick(i); }

    /**
       @returns if the tick i is major
       @param i the current tick
    */
    public  boolean isMajorTick(int i){
        if(i%majTickRatio - majTickOffset == 0)
            return true;
        return false;
    }

    public void updateTimeRange(){
        setTimes();
    }

    protected SimpleDateFormat timeFormat;
    
    protected Calendar calendar  = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    protected int totalPixels;
    
    protected long beginTime;

    protected long endTime;

    protected long firstLabelTime;

    protected long timeIntv;

    protected long majTickTime;

    protected int numTicks;

    protected int majTickRatio;

    protected int majTickOffset;
    
    protected double tickSpacing, tickOffset;

    protected TimeConfigRegistrar timeRegistrar;
}// TimeScaleCalc
