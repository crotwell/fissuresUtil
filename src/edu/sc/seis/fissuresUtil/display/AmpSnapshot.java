package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import edu.iris.Fissures.model.UnitRangeImpl;
import java.util.Iterator;

/**
 * AmpSnapshot.java
 *
 *
 * Created: Mon Jul 22 12:47:38 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AmpSnapshot {
    public AmpSnapshot(HashMap seismos, UnitRangeImpl generalRange){
	update(seismos, generalRange);
    }

    public void update(HashMap seismos, UnitRangeImpl generalRange){
	this.generalRange = generalRange;
	Iterator e = seismos.keySet().iterator();
	while(e.hasNext()){
	    DataSetSeismogram current = (DataSetSeismogram)e.next();
	    UnitRangeImpl currentUnit = (UnitRangeImpl)seismos.get(current);
	    this.seismoAmpRange.put(current, new UnitRangeImpl(currentUnit.getMinValue(), currentUnit.getMaxValue(),
							       currentUnit.getUnit()));
	}
    }    

    public UnitRangeImpl getAmpRange(){ return generalRange; }
    
    public UnitRangeImpl getAmpRange(DataSetSeismogram seis){ return (UnitRangeImpl)seismoAmpRange.get(seis); }

    private UnitRangeImpl generalRange;

    private HashMap seismoAmpRange = new HashMap();;
    
}// AmpSnapshot
