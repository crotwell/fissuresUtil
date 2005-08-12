package edu.sc.seis.fissuresUtil.display.particlemotion;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author hedx Created on Jul 14, 2005
 */

public class ParticleMotionPoint implements Cloneable{

    public ParticleMotionPoint() {
        this(0, 0, (byte)0, null);
    }

    public ParticleMotionPoint(MicroSecondTimeRange range) {
        this(0, 0, (byte)0, range);
    }

    public ParticleMotionPoint(byte type, MicroSecondTimeRange range) {
        this(0, 0, type, range);
    }

    public ParticleMotionPoint(double xVal, double yVal, MicroSecondTimeRange range) {
        this(xVal, yVal, (byte)0, range);
    }
    
    public ParticleMotionPoint(double xVal,
                               double yVal,
                               byte type){
        this(xVal, yVal, type, null);
    }

    public ParticleMotionPoint(double xVal,
                               double yVal,
                               byte type,
                               MicroSecondTimeRange range) {
        this.xVal = xVal;
        this.yVal = yVal;
        this.type = type;
        this.range = range;
    }
    
    public Object clone(){
        ParticleMotionPoint clone = new ParticleMotionPoint(this.xVal, this.yVal, this.type);
        MicroSecondDate beginTime = (MicroSecondDate)this.range.getBeginTime().clone();
        MicroSecondDate endTime =  (MicroSecondDate)this.range.getEndTime().clone();
        MicroSecondTimeRange newRange = new MicroSecondTimeRange(beginTime, endTime);
        clone.setRange(newRange);
        return clone;
        
    }
    
    public boolean arePointsSimilar(ParticleMotionPoint otherPoint){
        if(otherPoint.getXVal() == this.xVal && otherPoint.getYVal() == this.yVal){
            return true;
        }
        return false;
    }
/*
    public Object clone() {
        ParticleMotionPoint cloned = new ParticleMotionPoint(this.xVal,
                                                             this.yVal,
                                                             this.type,
                                                             this.range);
        return cloned;
    }*/
    
    
    public void setBeginTime(MicroSecondDate beginTime){
        MicroSecondDate endTime = this.range.getEndTime();
        this.range = new MicroSecondTimeRange(beginTime, endTime);
    }
    
    public void setEndTime(MicroSecondDate endTime){
        MicroSecondDate beginTime = this.range.getBeginTime();
        this.range = new MicroSecondTimeRange(beginTime, endTime);
    }
    
    public MicroSecondTimeRange getRange() {
        return range;
    }

    public void setRange(MicroSecondTimeRange range) {
        this.range = range;
    }

    public double getXVal() {
        return xVal;
    }

    public void setXVal(double val) {
        xVal = val;
    }

    public double getYVal() {
        return yVal;
    }

    public void setYVal(double val) {
        yVal = val;
    }
    
    public String toString(){
        String returnVal = "X-Value: " + xVal + "\nY-Value: " + yVal + "\nTimeRange: " + range;
        return returnVal;
    }

    public byte getType() {
        return type;
    }
    public void setType(byte type) {
        this.type = type;
    }
    private double xVal, yVal;

    private MicroSecondTimeRange range;

    private byte type;
    
}
