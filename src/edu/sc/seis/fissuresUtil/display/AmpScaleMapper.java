package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.IfNetwork.Response;
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

    public AmpScaleMapper(int totalPixels,
                          int hintPixels,
                          Registrar reg){
        this.totalPixels = totalPixels;
        this.hintPixels = hintPixels;
        this.reg = reg;
        reg.addListener(this);
    }

    public int getPixelLocation(int i){

        return (int)Math.round(SimplePlotUtil.linearInterp(minTick, 0,
                                                           calcRange, totalPixels,
                                                           minTick + i * tickInc));
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

    public String getAxisLabel() {
        return unitDisplayUtil.getNameForUnit(getUnit());
    }

    public UnitImpl getUnit() {
        return range.getUnit();
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

        double rangeWidth = range.getMaxValue()-range.getMinValue();

        if ( rangeWidth == 0) {
            // not a real range
            numTicks = 0;
            return;
        } // end of if ()


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
        calcRange = tickInc * numTicks + minTick;
    }

    public void setTotalPixels(int p) {
        totalPixels = p;
        calculateTicks();
    }

    public int getTotalPixels(){ return totalPixels; }

    public void setUnitRange(UnitRangeImpl r) {
        range = r;
        calculateTicks();
    }

    public UnitRangeImpl getUnitRange() {
        return range;
    }

    public void updateAmp(AmpEvent event){
        lastAmpEvent = event;
        if (event.getSeismograms().length != 0) {
        setUnitRange(getRealWorldUnitRange(event.getAmp(), event.getSeismograms()[0]));
        } else {
            setUnitRange(event.getAmp());
        }
    }

    public void setRegistrar(Registrar reg){
        this.reg.removeListener(this);
        this.reg = reg;
        reg.addListener(this);
    }

    public UnitRangeImpl getRealWorldUnitRange(UnitRangeImpl ur, DataSetSeismogram seismo) {
        UnitImpl lastUnit = null;
        UnitImpl realWorldUnit = UnitImpl.COUNT;
        // this is the constant to divide by to get real worl units (not counts)
        float sensitivity = 1.0f;
        Object responseObj =
            seismo.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
        if (responseObj != null && responseObj instanceof Response) {
            Response response = (Response)responseObj;
            realWorldUnit = (UnitImpl)response.stages[0].input_units;
            sensitivity = response.the_sensitivity.sensitivity_factor;
        }
        UnitRangeImpl out = new UnitRangeImpl(ur.getMinValue()/sensitivity,
                                              ur.getMaxValue()/sensitivity,
                                              realWorldUnit);
        return out;
    }

    private UnitDisplayUtil unitDisplayUtil = new UnitDisplayUtil();

    private AmpEvent lastAmpEvent = null;

    private int firstMajorTick = 0;

    private int majorTickStep = 10;

    private double tickInc;

    private double minTick;

    private double calcRange;

    private int numTicks = 0;

    private int totalPixels;

    private int hintPixels;

    private UnitRangeImpl range;

    private Registrar reg;

} // AmpScaleMapper
