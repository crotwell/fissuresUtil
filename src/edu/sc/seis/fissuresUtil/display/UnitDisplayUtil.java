/**
 * UnitDisplayUtil.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.display;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.log4j.Logger;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;

public class UnitDisplayUtil {

    public static final String mu = "\u03BC";

    public static QuantityImpl getBestForDisplay(QuantityImpl quantity) {
        UnitRangeImpl inRange = new UnitRangeImpl(quantity.getValue(),
                                                  quantity.getValue(),
                                                  quantity.getUnit());
        inRange = getBestForDisplay(inRange);
        return new QuantityImpl(inRange.getMinValue(), inRange.getUnit());
    }

    public static UnitRangeImpl getBestForDisplay(UnitRangeImpl inRange) {
        // just in case we don't have a case for this unit
        UnitRangeImpl outRange = inRange;
        if(inRange.getUnit().isConvertableTo(UnitImpl.METER_PER_SECOND)) {
            // velocity
            inRange = inRange.convertTo(UnitImpl.METER_PER_SECOND);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer/sec
                outRange = inRange.convertTo(UnitImpl.NANOMETER_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec
                outRange = inRange.convertTo(UnitImpl.MICRON_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND);
            }
        } else if(inRange.getUnit().isConvertableTo(UnitImpl.METER)) {
            // displacement
            inRange = inRange.convertTo(UnitImpl.METER);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer
                outRange = inRange.convertTo(UnitImpl.NANOMETER);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron
                outRange = inRange.convertTo(UnitImpl.MICRON);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm
                outRange = inRange.convertTo(UnitImpl.MILLIMETER);
            }
        } else if(inRange.getUnit()
                .isConvertableTo(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            //acceleration
            inRange = inRange.convertTo(UnitImpl.METER_PER_SECOND_PER_SECOND);
            if(Math.abs(inRange.getMinValue()) < .000001
                    && Math.abs(inRange.getMaxValue()) < .000001) {
                // use nanometer/sec/sec
                outRange = inRange.convertTo(UnitImpl.NANOMETER_PER_SECOND_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec/sec
                outRange = inRange.convertTo(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND);
            }
        } else if(inRange.getUnit().isConvertableTo(UnitImpl.COUNT)) {
            //acceleration
            inRange = inRange.convertTo(UnitImpl.COUNT);
            if(Math.abs(inRange.getMinValue()) < .001
                    && Math.abs(inRange.getMaxValue()) < .001) {
                outRange = inRange.convertTo(UnitImpl.MICROCOUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1
                    && Math.abs(inRange.getMaxValue()) < 1) {
                outRange = inRange.convertTo(UnitImpl.MILLICOUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1000
                    && Math.abs(inRange.getMaxValue()) < 1000) {
                outRange = inRange.convertTo(UnitImpl.COUNT);
            } else if(Math.abs(inRange.getMinValue()) < 1000000
                    && Math.abs(inRange.getMaxValue()) < 1000000) {
                outRange = inRange.convertTo(UnitImpl.KILOCOUNT);
            } else {
                outRange = inRange.convertTo(UnitImpl.MEGACOUNT);
            }
        } else {
            //            logger.debug("No case, using amp range of
            // "+outRange.getMinValue()+" to "
            //                             +outRange.getMaxValue()+" "+
            //                             outRange.getUnit());
        }
        return outRange;
    }

    /**
     * calculates a new UnitRangeImpl using the response of the given
     * seismogram. If seis does not have a response, then the input amp is used.
     */
    public static UnitRangeImpl getRealWorldUnitRange(UnitRangeImpl ur,
                                                      DataSetSeismogram seismo) {
        UnitRangeImpl out = ur;
        if(ur.getUnit().isConvertableTo(UnitImpl.COUNT)) {
            Object responseObj = seismo.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
            if(responseObj != null && responseObj instanceof Response) {
                Response response = (Response)responseObj;
                UnitImpl realWorldUnit = (UnitImpl)response.stages[0].input_units;
                // this is the constant to divide by to get real worl units (not
                // counts)
                float sensitivity = response.the_sensitivity.sensitivity_factor;
                //        logger.debug("sensitivity is "+sensitivity+" to get to
                // "+realWorldUnit);
                if(sensitivity > 0) {
                    out = new UnitRangeImpl(ur.getMinValue() / sensitivity,
                                            ur.getMaxValue() / sensitivity,
                                            realWorldUnit);
                } else {
                    out = new UnitRangeImpl(ur.getMaxValue() / sensitivity,
                                            ur.getMinValue() / sensitivity,
                                            realWorldUnit);
                    seismo.addAuxillaryData("sensitivity",
                                            response.the_sensitivity);
                }
            }
        }
        return getBestForDisplay(out);
    }

    /**
     * tries to come up with better names for some standard units than the
     * auto-generated versions.
     */
    public static String getNameForUnit(UnitImpl unit) {
        // most common
        if(unit.equals(UnitImpl.METER_PER_SECOND)) { return "m/s"; }
        if(unit.equals(UnitImpl.MICRON_PER_SECOND)) { return "microns/sec"; }
        if(unit.equals(UnitImpl.MILLIMETER_PER_SECOND)) { return "mm/s"; }
        if(unit.equals(UnitImpl.NANOMETER_PER_SECOND)) { return "nm/s"; }
        if(unit.equals(UnitImpl.KILOMETER)) { return "km"; }
        if(unit.equals(UnitImpl.METER)) { return "m"; }
        if(unit.equals(UnitImpl.MILLIMETER)) { return "mm"; }
        if(unit.equals(UnitImpl.MICROMETER)) { return "micrometer"; }
        if(unit.equals(UnitImpl.NANOMETER)) { return "nanometer"; }
        if(unit.equals(UnitImpl.METER_PER_SECOND_PER_SECOND)) { return "m/s/s"; }
        if(unit.equals(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND)) { return "mm/s/s"; }
        if(unit.equals(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND)) { return "microns/s/s"; }
        if(unit.equals(UnitImpl.NANOMETER_PER_SECOND_PER_SECOND)) { return "nm/s/s"; }
        if(unit.equals(UnitImpl.SECOND)) { return "s"; }
        if(unit.equals(UnitImpl.DEGREE)) { return "deg"; }
        if(unit.equals(UnitImpl.COUNT)) { return "COUNTS"; }
        if(unit.equals(UnitImpl.MILLICOUNT)) { return "COUNTS x 10^-3"; }
        if(unit.equals(UnitImpl.MICROCOUNT)) { return "COUNTS x 10^-6"; }
        if(unit.equals(UnitImpl.KILOCOUNT)) { return "COUNTS x 10^3"; }
        if(unit.equals(UnitImpl.MEGACOUNT)) { return "COUNTS x 10^6"; }
        // not a unit we have a friendly name for
        //logger.debug("not a unit we have a friendly name
        // for"+unit.toString());
        return unit.toString();
    }

    public static String formatQuantityImpl(Quantity quantity) {
        return formatQuantityImpl(quantity,
                                  new DecimalFormat("#,###,##0.0##; -#,###,##0.0##"));
    }

    public static String formatQuantityImpl(Quantity quantity,
                                            NumberFormat format) {
        if(quantity != null) { return format.format(quantity.value) + " "
                + getNameForUnit((UnitImpl)quantity.the_units).toLowerCase(); }
        return "...";
    }

    static Logger logger = Logger.getLogger(UnitDisplayUtil.class);
}