package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.model.UnitRangeImpl;

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
	this.seismos = seismos;
	this.amps = amps;
    }
    
    public UnitRangeImpl getAmp(DataSetSeismogram seismo){
	return amps[indexOf(seismo)];
    }
    
    public UnitRangeImpl getAmp(){
	if(genericAmp == null){
	    if(amps.length == 1){
		genericAmp = amps[0];
	    }else{
		boolean equal = true;
		for(int i = 0; i < seismos.length; i++){
		    if(!amps[i].equals(amps[i+1])){
			equal = false;
			i = seismos.length;
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
