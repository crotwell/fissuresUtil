package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.MicroSecondDate;

/**
 * Abstract superclass of scale mappers that are for time scales.
 *
 *
 * Created: Mon Oct 18 16:31:59 1999
 *
 * @author Philip Crotwell
 * @version 0.1
 */

public abstract class TimeScaleMapper implements ScaleMapper {

    public TimeScaleMapper(int totalPixels,
                           int hintPixels,
                           MicroSecondDate beginTime,
                           MicroSecondDate endTime) {
        if (endTime.before(beginTime)) {
            throw new IllegalArgumentException("endTime must be after beginTime, "+beginTime.toString()+"  "+endTime.toString());
        }
        this.totalPixels = totalPixels;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
    public TimeScaleMapper(int totalPixels,
                           MicroSecondDate beginTime,
                           MicroSecondDate endTime) {
        if (endTime.before(beginTime)) {
            throw new IllegalArgumentException("endTime must be after beginTime, "+beginTime.toString()+"  "+endTime.toString());
        }
        this.totalPixels = totalPixels;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public void  setTotalPixels(int totalPixels) {
        this.totalPixels = totalPixels;
        calculateTicks();
    }

    public void  setTotalPixels(int totalPixels, int hintPixels) {
        this.hintPixels = hintPixels;
        this.totalPixels = totalPixels;
        calculateTicks();
    }

    public void  setHintPixels(int hintPixels) {
        this.hintPixels = hintPixels;
        calculateTicks();
    }

    public void setTimes(MicroSecondDate beginTime,
                         MicroSecondDate endTime) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        calculateTicks();
    }

    public boolean isMajorTick(int i) {
        return ((i % majorTickStep) == firstMajorTick);
    }

    public boolean isLabelTick(int i) {
        return ((i % (2 * majorTickStep)) == firstMajorTick);
    }

    public String getAxisLabel() {
        return "Time";
    }

    protected abstract void calculateTicks();

    protected int totalPixels;

    protected MicroSecondDate minTick;

    protected MicroSecondDate beginTime;

    protected MicroSecondDate endTime;

    protected int firstMajorTick = 0;

    protected int majorTickStep = 50; // just a guess

    protected double tickInc;

    protected int numTicks = 0;

    protected int hintPixels;

} // TimeScaleMapper
