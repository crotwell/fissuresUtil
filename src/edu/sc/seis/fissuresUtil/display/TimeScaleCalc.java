package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.*;

import edu.iris.Fissures.model.MicroSecondDate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

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

public class TimeScaleCalc implements ScaleMapper, TimeListener {
    /**
       @param totalPixels the width of the axis being used in pixels

    */
    TimeScaleCalc (int totalPixels, Registrar reg){
        this.totalPixels = totalPixels;
        reg.addListener(this);
    }

    public void calculateTicks(){
        int majTickNum = totalPixels/60;
        majTickTime = timeIntv/majTickNum;
        majTickRatio = 10;
        if(majTickTime <= SECOND){
            //System.out.println("TimeScaleCalc SECOND"+" "+majTickTime);
            timeFormat = new SimpleDateFormat("mm:ss.S");
            if(majTickTime <= 1000){
                majTickTime = 1000;
            }else if(majTickTime <= 10000){
                majTickTime = 10000;
            }else if(majTickTime <= 100000){
                majTickTime = 100000;	
            }else {
                majTickTime = 500000;
                majTickRatio = 5;
            } // end of else
            
        }else if(majTickTime <= 45 * SECOND){
            majTickRatio = 10;
            //System.out.println("TimeScaleCalc 45SECOND"+totalPixels+" "+majTickTime);
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= 1.2*SECOND){
                majTickTime = SECOND;
            }else if(majTickTime <= 3*SECOND){
                majTickTime = 2*SECOND;
                majTickRatio = 4;
            }else if(majTickTime <= 6*SECOND){
                majTickTime = 5*SECOND;
                majTickRatio = 5;
            }else if(majTickTime <= 12*SECOND){
                majTickTime = 10*SECOND;
            }else if(majTickTime <= 22*SECOND){
                majTickTime = 20*SECOND;
                majTickRatio = 4;
            }else {
                majTickTime = 30*SECOND;
            } // end of else
        }
        else if(majTickTime <= 3*MINUTE){
            majTickRatio = 6;
            //System.out.println("TimeScaleCalc 3MINUTE"+" "+majTickTime);
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= MINUTE+10*SECOND){
                majTickTime = MINUTE;
            }else if(majTickTime <= 2*MINUTE+30*SECOND){
                majTickTime = 2*MINUTE;
                majTickRatio = 12;
            }else
                majTickTime = 2*MINUTE+30*SECOND;
        }
        else if(majTickTime <= 30*MINUTE){
            majTickRatio = 10;
            //System.out.println("TimeScaleCalc 30MINUTE"+" "+majTickTime);
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            if(majTickTime <= 6*MINUTE){
                majTickTime = 5*MINUTE;
                majTickRatio = 5;
            } else if(majTickTime <= 10*MINUTE){
                majTickTime = 10*MINUTE;
            } else if(majTickTime <= 20*MINUTE){
                majTickTime = 20*MINUTE;
            } else {
                majTickTime = 30*MINUTE;
            } // end of else
        }
        else if(majTickTime <= 4*HOUR){
            majTickRatio = 6;
            //System.out.println("TimeScaleCalc 4HOUR"+" "+majTickTime);
            timeFormat = new SimpleDateFormat("MM/dd HH:mm");
            if(majTickTime <= HOUR){
                majTickTime = HOUR;
            }else if(majTickTime <= 2*HOUR){
                majTickTime = 2*HOUR;
                majTickRatio = 4;
            }else if(majTickTime <= 3*HOUR){
                majTickTime = 3*HOUR;
                majTickRatio = 6;
            }else if(majTickTime <= 4*HOUR){
                majTickTime = 4*HOUR;
                majTickRatio = 4;
            }else {
                majTickTime = 4*HOUR;
                majTickRatio = 4;
            }
        }
        else if(majTickTime <= 4*DAY){
            majTickRatio = 6;
            //System.out.println("TimeScaleCalc ELSE"+" "+majTickTime);
            timeFormat = new SimpleDateFormat("MM/dd");
            majTickTime = DAY;
        }else{
	    majTickRatio = 7;
	    timeFormat = new SimpleDateFormat("MM/dd");
	    majTickTime = WEEK;
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

    public String getAxisLabel() {
        return "Time";
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

    public void updateTime(TimeEvent event){
	setTimes(event.getTime().getBeginTime(), event.getTime().getEndTime());
    }

    private SimpleDateFormat timeFormat;
    
    private Calendar calendar  = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private int totalPixels;
    
    private long beginTime;

    private long endTime;

    private long firstLabelTime;

    private long timeIntv;

    private long majTickTime;

    private int numTicks;

    private int majTickRatio;

    private int majTickOffset;
    
    private double tickSpacing, tickOffset;

    private static final long SECOND = 1000000;
    private static final long MINUTE = 60*SECOND;
    private static final long HOUR = 60*MINUTE;
    private static final long DAY = 24*HOUR;
    private static final long WEEK = 7*DAY;

}// TimeScaleCalc
