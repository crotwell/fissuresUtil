package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.model.*;
import java.text.DecimalFormat;

/**
 * AmpScaleMapper.java
 *
 *
 * Created: Fri Oct 22 14:47:39 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class AmpScaleMapper implements ScaleMapper, AmpListener {
    public AmpScaleMapper(int totalPixels, int hintPixels, Registrar reg){
	this.totalPixels = totalPixels;
        this.hintPixels = hintPixels;
	this.reg = reg;
	reg.addListener(this);
    } 
  
    public int getPixelLocation(int i) {
        return SeisPlotUtil.getPixel(totalPixels, range, 
				     minTick + i * tickInc);
	//earlier it was total
    }

    public String getLabel(int i) {
        if (isLabelTick(i)) {
            double value = minTick + i * tickInc;
            double absValue = Math.abs(value);
            // use regular notation
            DecimalFormat df;
            if (absValue < 10 && absValue != 0 ) {
                // exponential notation
                df = new DecimalFormat("0.00###");
            } else {
	        df = new DecimalFormat("#.####");
	    }
            return df.format(value);
        } else {
            return "";
        }
    }

    public int getNumTicks() {
        return numTicks;
    }

    public boolean isMajorTick(int i) {
        return ((i % majorTickStep) == firstMajorTick);
    }

    public boolean isLabelTick(int i) {
        return ((i % (2*majorTickStep)) == firstMajorTick);
    }

    protected void calculateTicks() {
        if (totalPixels == 0) {
            numTicks = 0;
            return;
        }

        // aim for about hintNumber ticks
        double hintNumber = totalPixels/(double)hintPixels;

        double rangeWidth = range.getMaxValue()-range.getMinValue();
        // find power of ten just larger than the goalTickInc 
        tickInc = Math.pow(10, 
			   Math.ceil(Math.log(rangeWidth) /
                                     Math.log(10.0)));
        double goalTickInc = (rangeWidth) / totalPixels * hintPixels;

        //mostly major ticks are ten x minor ticks, but may be overridden
        majorTickStep = 10;

        if (tickInc >= 8*goalTickInc) {
            tickInc /= 10;
        }
        if (tickInc >= 4*goalTickInc) {
            tickInc /= 4;
            majorTickStep = 4;
        }
        if (tickInc >= 2*goalTickInc) {
            tickInc /= 2;
            majorTickStep = 2;
        }

        minTick = tickInc *
            Math.floor(range.getMinValue() / tickInc);
        double minMajorTick = majorTickStep * tickInc *
            Math.floor(range.getMinValue() / 
                       (majorTickStep * tickInc));
        firstMajorTick = (int)Math.round((minTick-minMajorTick)/tickInc);
        if (firstMajorTick < 0) {
            firstMajorTick += majorTickStep;
        }

        numTicks = 1;
	while (minTick + numTicks * tickInc <= range.getMaxValue() ) {
            numTicks++; 
        }
         //System.out.println("tickInc="+tickInc+" minTick="+minTick+" minMajorTick="+minMajorTick+" numTicks="+numTicks);

    }
    
    public void setTotalPixels(int p) {
        totalPixels = p;
        calculateTicks();
    }

    public void setUnitRange(UnitRangeImpl r) {
        range = r;
        calculateTicks();
    }

    public void updateAmp(AmpEvent event){
	range = event.getAmp();
	calculateTicks();
    }

    public void setRegistrar(Registrar reg){
	this.reg.removeListener(this);
	this.reg = reg;
	reg.addListener(this);
    }

    private int firstMajorTick = 0;

    private int majorTickStep = 10;

    private double tickInc;

    private double minTick;

    private int numTicks = 0;

    private int totalPixels;

    private int hintPixels;

    private UnitRangeImpl range;

    private Registrar reg;

} // AmpScaleMapper
