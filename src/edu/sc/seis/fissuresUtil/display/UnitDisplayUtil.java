/**
 * UnitDisplayUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitImpl;

public class UnitDisplayUtil
{


    /** tries to come up with better names for some standard units than the
     auto-generated versions. */
    public String getNameForUnit(UnitImpl unit) {
        // most common
        if (unit.equals(UnitImpl.METER_PER_SECOND)) {
            return "m/s";
        }
        if (unit.equals(UnitImpl.METER)) {
            return "m";
        }
        if (unit.equals(UnitImpl.METER_PER_SECOND_PER_SECOND)) {
            return "m/s/s";
        }
        return unit.toString();
    }
}

