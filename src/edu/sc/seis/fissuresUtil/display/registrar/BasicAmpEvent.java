package edu.sc.seis.fissuresUtil.display.registrar;

/**
 * BasicAmpEvent.java
 *
 * @author Created by Charlie Groves
 */

import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;

public class BasicAmpEvent implements AmpEvent{
    public BasicAmpEvent(DataSetSeismogram[] seismos, UnitRangeImpl[] amps){
        if ( seismos.length != amps.length) {
            throw new IllegalArgumentException("seismogram and amp arrays must have equal length "+seismos.length+" != "+amps.length);
        } // end of if ()
        this.seismos = seismos;
        this.amps = amps;
        generateGenericAmp();
    }

    public UnitRangeImpl getAmp(DataSetSeismogram seismo){
        return amps[indexOf(seismo)];
    }

    public boolean contains(DataSetSeismogram seismo){
        try{
            indexOf(seismo);
            return true;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    public UnitRangeImpl getAmp(){
        return genericAmp;
    }

    private void generateGenericAmp(){
        // Currently, we only do "real world units" in the case of one seismogram
        // eventually, the overlays should
        if(amps.length == 0){
            genericAmp = DisplayUtils.ONE_RANGE;
        } else if(amps.length == 1){
            genericAmp = calcGenericAmp(amps[0], seismos[0]);
        }else{
            boolean equal = true;
            for(int i = 1; i < amps.length; i++){
                if(!amps[i].equals(amps[i-1])){
                    equal = false;
                    i = amps.length;
                }
            }
            if(equal){
                genericAmp = amps[0];
            }else{
                double halfRange = (amps[0].getMaxValue() - amps[0].getMinValue())/2;
                genericAmp = new UnitRangeImpl(-halfRange, halfRange, amps[0].getUnit());
            }
        }
    }

    /** calculates a new generic amp using the response of the given seismogram.
     If seis does not have a response, then the initial amp is used. */
    protected UnitRangeImpl calcGenericAmp(UnitRangeImpl inAmp, DataSetSeismogram seis) {
        Object obj = seis.getAuxillaryData(StdAuxillaryDataNames.RESPONSE);
        Response resp = (Response)obj;
        if (obj != null) {
            UnitRangeImpl respUnitRange =
                new UnitRangeImpl(inAmp.min_value/resp.the_sensitivity.sensitivity_factor,
                                  inAmp.max_value/resp.the_sensitivity.sensitivity_factor,
                                  resp.stages[0].input_units);
            return UnitDisplayUtil.getBestForDisplay(respUnitRange);
        } else {
            return inAmp;
        }
    }

    public void setAmp(UnitRangeImpl amp){
        genericAmp = amp;
    }

    public DataSetSeismogram[] getSeismograms() {
        return seismos;
    }

    private int indexOf(DataSetSeismogram seismo){
        for(int i = 0; i < seismos.length; i++){
            if(seismos[i] == seismo){
                return i;
            }
        }
        throw new IllegalArgumentException("Seismogram is not in this AmpEvent");
    }

    private DataSetSeismogram[] seismos;

    private UnitRangeImpl[] amps;

    private UnitRangeImpl genericAmp;
}

