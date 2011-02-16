package edu.sc.seis.fissuresUtil.display.registrar;

/**
 * BasicAmpEvent.java
 * 
 * @author Created by Charlie Groves
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class BasicAmpEvent implements AmpEvent {
    public BasicAmpEvent(DataSetSeismogram[] seismos, UnitRangeImpl[] amps, UnitRangeImpl genericRange) {
        if (seismos.length != amps.length) { throw new IllegalArgumentException(
                "seismogram and amp arrays must have equal length "
                        + seismos.length + " != " + amps.length); } // end of if
        // ()
        this.seismos = seismos;
        this.amps = amps;
        this.genericAmp = genericRange;
    }
    
    

    public UnitRangeImpl getAmp(DataSetSeismogram seismo) {
        return amps[indexOf(seismo)];
    }

    public boolean contains(DataSetSeismogram seismo) {
        try {
            indexOf(seismo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public UnitRangeImpl getAmp() {
        return genericAmp;
    }

    protected void generateGenericAmp() {
        // Currently, we only do "real world units" in the case of one
        // seismogram
        // eventually, the overlays should
        if (amps.length == 0) {
            genericAmp = DisplayUtils.ONE_RANGE;
        } else if (amps.length == 1) {
            genericAmp = UnitDisplayUtil.getRealWorldUnitRange(amps[0],
                    seismos[0]);
        } else {
            boolean totallyEqual = true;
            for (int i = 1; i < amps.length; i++) {
                if (!amps[i].equals(amps[i - 1])) {
                    totallyEqual = false;
                    break;
                }
            }
            if (totallyEqual) {
                genericAmp = UnitDisplayUtil.getRealWorldUnitRange(amps[0],
                        seismos[0]);
            } else {
                genericAmp = DisplayUtils.ONE_RANGE;
            }
        }
    }

    public DataSetSeismogram[] getSeismograms() {
        return seismos;
    }

    private int indexOf(DataSetSeismogram seismo) {
        for (int i = 0; i < seismos.length; i++) {
            if (seismos[i] == seismo) { return i; }
        }
        throw new IllegalArgumentException("Seismogram is not in this AmpEvent");
    }

    protected DataSetSeismogram[] seismos;

    protected UnitRangeImpl[] amps;

    protected UnitRangeImpl genericAmp;

    private static Logger logger = LoggerFactory.getLogger(BasicAmpEvent.class);

}

