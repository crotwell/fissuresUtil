package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfSeismogramDC.LocalMotionVector;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalMotionVectorImpl;
import edu.iris.Fissures.IfSeismogramDC.VectorComponent;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * Rotate.java
 *
 *
 * Created: Sun Dec 15 13:43:21 2002
 *
 * @author Philip Crotwell
 * @version $Id: Rotate.java 3022 2002-12-16 18:48:33Z crotwell $
 */
public class Rotate implements LocalMotionVectorFunction {

    public Rotate() {
	
    } // Rotate constructor
    
    public LocalMotionVector apply(LocalMotionVector vec) {
	VectorComponent[] data = new VectorComponent[3];


	return new  LocalMotionVectorImpl(vec.get_id(),
					  vec.properties,
					  vec.begin_time,
					  vec.num_points,
					  vec.sampling_info,
					  vec.y_unit,
					  vec.channel_group,
					  vec.parm_ids,
					  (TimeInterval[])vec.time_corrections,
					  vec.sample_rate_history,
					  data);
    }

} // Rotate
