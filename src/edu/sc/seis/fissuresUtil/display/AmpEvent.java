package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * AmpEvent.java
 *
 *
 * Created: Sun Sep 15 20:01:55 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AmpEvent {
    public AmpEvent(DataSetSeismogram[] seismos, UnitRangeImpl[] amps){
        if ( seismos.length != amps.length) {
            throw new IllegalArgumentException("seismogram and amp arrays must have equal length "+seismos.length+" != "+amps.length);
        } // end of if ()

        this.seismos = seismos;
        this.amps = amps;
    }

    public UnitRangeImpl getAmp(DataSetSeismogram seismo){
        return amps[indexOf(seismo)];
    }

    public UnitRangeImpl getAmp(){
        if(genericAmp == null){
            if(amps.length == 0){
                genericAmp = new UnitRangeImpl(-1, 1, UnitImpl.COUNT);
            } else if(amps.length == 1){
                genericAmp = amps[0];
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
        return genericAmp;
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
        return 0;
    }

    private DataSetSeismogram[] seismos;
    private UnitRangeImpl[] amps;
    private UnitRangeImpl genericAmp;
}// AmpEvent
