package edu.sc.seis.fissuresUtil.display;

import java.util.HashMap;
import edu.iris.Fissures.model.UnitRangeImpl;

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
    public AmpSnapshot(HashMap seismoAmpRange, UnitRangeImpl generalRange){
	this.seismoAmpRange = seismoAmpRange;
	this.generalRange = generalRange;
    }

    public UnitRangeImpl getAmpRange(){ return generalRange; }
    
    public UnitRangeImpl getAmpRange(DataSetSeismogram seis){ return (UnitRangeImpl)seismoAmpRange.get(seis); }

    private UnitRangeImpl generalRange;

    private HashMap seismoAmpRange;
    
}// AmpSnapshot
