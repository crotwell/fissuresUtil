package edu.sc.seis.fissuresUtil.display;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeListener;

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
    TimeScaleCalc (int totalPixels, TimeConfig tc){
        this.totalPixels = totalPixels;
        tc.addListener(this);
    }
    
    public void calculateTicks(){
        if(totalPixels == 0){
            numTicks = 0;
            return;
        }
        int majTickNum = totalPixels/60;
        if (majTickNum < 2) {majTickNum = 2;}// prevent divide by zero
        majTickTime = timeIntv/majTickNum;
        majTickRatio = 10;
        daysInBorder = false;
        if(majTickTime <= SECOND){
            setTimeFormat("mm:ss.S");
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
            setTimeFormat("mm:ss");
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
            setTimeFormat("HH:mm:ss");
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
            setTimeFormat("HH:mm:ss");
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
            daysInBorder = true;
            setTimeFormat("MM/dd HH:mm");
            if(majTickTime <= HOUR){
                majTickTime = HOUR;
            }else if(majTickTime <= 2*HOUR){
                majTickTime = 2*HOUR;
                majTickRatio = 4;
            }else if(majTickTime <= 3*HOUR){
                majTickTime = 3*HOUR;
                majTickRatio = 6;
            }else{
                majTickTime = 4*HOUR;
                majTickRatio = 4;
            }
        }
        else if(majTickTime <= 4*DAY){
            majTickRatio = 6;
            daysInBorder = true;
            setTimeFormat("MM/dd");
            majTickTime = DAY;
        }else{
            majTickRatio = 7;
            daysInBorder = true;
            setTimeFormat("MM/dd");
            majTickTime = WEEK;
        }
        borderFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        double numTicksDbl = ((timeIntv/(double)majTickTime) * majTickRatio);
        numTicks = (int)numTicksDbl;
        if(beginTime > 0){
            firstLabelTime = (beginTime/majTickTime + 1) * majTickTime;
        }else{
            firstLabelTime = (beginTime/majTickTime) * majTickTime;
        }
        majTickOffset = (int)((firstLabelTime - beginTime)/(double)timeIntv * numTicks);
        tickOffset = (firstLabelTime - beginTime)/(double)timeIntv/majTickRatio * totalPixels;
        tickSpacing = totalPixels/numTicksDbl;
    }
    
    public void  setTotalPixels(int totalPixels) {
        this.totalPixels = totalPixels;
        calculateTicks();
    }
    
    private void setTimeFormat(String newFormat){
        if(!borderFormat.toPattern().equals(newFormat)){
            borderFormat = new SimpleDateFormat(newFormat);
        }
    }
    
    /**
     * read the number of pixels allocated for this scale;
     * @return
     */
    public int  getTotalPixels() {
        return this.totalPixels;
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
            MicroSecondDate date = new MicroSecondDate(firstLabelTime + i/majTickRatio * majTickTime);
            calendar.setTime(date);
            return borderFormat.format(calendar.getTime());
        }
        return "";
    }
    
    public String getAxisLabel() {
        if(relative){
            return "Relative time";
        }
        if(time != null && !daysInBorder){
            Date middleDate = time.getBeginTime().add(new TimeInterval(time.getInterval().divideBy(2)));
            calendar.setTime(middleDate);
            return axisFormat.format(calendar.getTime()) + " (GMT)";
        }else{
            return "Time (GMT)";
        }
    }
    
    /**
     @returns the location of the tick i in pixels
     @param i the current tick
     */
    public  int getPixelLocation(int i){
        return (int)(i*tickSpacing + tickOffset); }
    
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
        if(i%majTickRatio - majTickOffset == 0){
            return true;
        }
        return false;
    }
    
    public void updateTime(TimeEvent event){
        time = event.getTime();
        if(roundTheEpoch.intersects(time))
            relative = true;
        else
            relative = false;
        setTimes(event.getTime().getBeginTime(), event.getTime().getEndTime());
    }
    
    private SimpleDateFormat borderFormat = new SimpleDateFormat("MM/dd/yyy");
    
    private boolean daysInBorder = false;
    
    private MicroSecondTimeRange time;
    
    private SimpleDateFormat axisFormat = new SimpleDateFormat("MM/dd/yyyy");
    //Five days before the epoch to 10 after
    public static MicroSecondTimeRange roundTheEpoch = new MicroSecondTimeRange(new MicroSecondDate(0),
                                                                                new TimeInterval(20, UnitImpl.DAY));
    
    private boolean relative = false;
    
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

