/**
 * UnitDisplayUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.UnitRange;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import org.apache.log4j.Logger;

public class UnitDisplayUtil
{

    public static final String mu = "\u03BC";

    public QuantityImpl getBestForDisplay(QuantityImpl quantity) {
        UnitRangeImpl inRange = new UnitRangeImpl(quantity.getValue(),
                                          quantity.getValue(),
                                          quantity.getUnit());
        inRange = getBestForDisplay(inRange);
        return new QuantityImpl(inRange.getMinValue(), inRange.getUnit());
    }

    public UnitRangeImpl getBestForDisplay(UnitRangeImpl inRange) {
        // just in case we don't have a case for this unit
        UnitRangeImpl outRange = inRange;

        if (inRange.getUnit().equals(UnitImpl.METER_PER_SECOND)) {
            logger.debug("in velocity section"+inRange);
            // velocity
            if (Math.abs(inRange.getMinValue()) < .001 &&
                Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec
                outRange = inRange.convertTo(UnitImpl.MICRON_PER_SECOND);
                logger.debug("use micron/sec "+outRange);
            } else if (Math.abs(inRange.getMinValue()) < 1 &&
                Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND);
                logger.debug("use mm/sec "+outRange);
            }
        } else if (inRange.getUnit().equals(UnitImpl.METER)) {
            // displacement
            if (Math.abs(inRange.getMinValue()) < .001 &&
                Math.abs(inRange.getMaxValue()) < .001) {
                // use micron
                outRange = inRange.convertTo(UnitImpl.MICRON);
            } else if (Math.abs(inRange.getMinValue()) < 1 &&
                Math.abs(inRange.getMaxValue()) < 1) {
                // use mm
                outRange = inRange.convertTo(UnitImpl.MILLIMETER);
            }
        } else if (inRange.getUnit().equals(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            //acceleration
            if (Math.abs(inRange.getMinValue()) < .001 &&
                Math.abs(inRange.getMaxValue()) < .001) {
                // use micron/sec/sec
                outRange = inRange.convertTo(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND);
            } else if (Math.abs(inRange.getMinValue()) < 1 &&
                Math.abs(inRange.getMaxValue()) < 1) {
                // use mm/sec/sec
                outRange = inRange.convertTo(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND);
            }
        } else {
            logger.debug("No case, using amp range of "+outRange.getMinValue()+" to "
                             +outRange.getMaxValue()+" "+
                             outRange.getUnit());
        }
        return outRange;
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
        logger.debug("sensitivity is "+sensitivity+" to get to "+realWorldUnit);
        UnitRangeImpl out = new UnitRangeImpl(ur.getMinValue()/sensitivity,
                                              ur.getMaxValue()/sensitivity,
                                              realWorldUnit);

        return getBestForDisplay(out);
    }

    /** tries to come up with better names for some standard units than the
     auto-generated versions. */
    public String getNameForUnit(UnitImpl unit) {
        // most common
        if (unit.equals(UnitImpl.METER_PER_SECOND)) {
            return "m/s";
        }
        if (unit.equals(UnitImpl.MICRON_PER_SECOND)) {
            return mu+"m/s";
        }
        if (unit.equals(UnitImpl.MILLIMETER_PER_SECOND)) {
            return "mm/s";
        }
        if (unit.equals(UnitImpl.METER)) {
            return "m";
        }
        if (unit.equals(UnitImpl.MILLIMETER)) {
            return "mm";
        }
        if (unit.equals(UnitImpl.MICROMETER)) {
            return mu+"m";
        }
        if (unit.equals(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            return "m/s/s";
        }
        if (unit.equals(UnitImpl.MILLIMETER_PER_SECOND_PER_SECOND)) {
            return "mm/s/s";
        }
        if (unit.equals(UnitImpl.MICROMETER_PER_SECOND_PER_SECOND)) {
            return mu+"m/s/s";
        }
        // not a unit we have a friendly name for
        logger.debug("not a unit we have a friendly name for"+unit.toString());
        return unit.toString();
    }

    static Logger logger = Logger.getLogger(UnitDisplayUtil.class);

}

