package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfTimeSeries.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.*;

/**
 * MotionVectorUtil.java
 *
 *
 * Created: Sat Oct 19 11:29:38 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class MotionVectorUtil {

    /** Creates a motion vector from 3 seismograms.
     *	@throws IncompatibleSeismograms if the channel ids
     *  or the time basis of the seismograms are not compatible.
    */	
    public static LocalMotionVectorImpl create(LocalSeismogram[] seismograms)
	throws IncompatibleSeismograms {
	if ( NetworkIdUtil.areEqual(seismograms[0].channel_id.network_id,
				    seismograms[1].channel_id.network_id)) {
	    throw new IncompatibleSeismograms("Networks for 0 and 1 are not the same, "
					       +NetworkIdUtil.toString(seismograms[0].channel_id.network_id)
					       +" "
					       +NetworkIdUtil.toString(seismograms[1].channel_id.network_id));
	}

	if ( ! seismograms[0].channel_id.station_code.equals(
							  seismograms[1].channel_id.station_code)) {
	    throw new IncompatibleSeismograms("Station codes for 0 and 1 are not the same. "
					      +seismograms[0].channel_id.station_code
					      +" "
					      +seismograms[1].channel_id.station_code);
	}
	if ( ! seismograms[0].channel_id.site_code.equals(
							  seismograms[1].channel_id.site_code)) {
	    throw new IncompatibleSeismograms("Site codes for 0 and 1 are not the same. "
					      +seismograms[0].channel_id.site_code
					      +" "
					      +seismograms[1].channel_id.site_code);
	}
	if ( ! seismograms[0].channel_id.site_code.equals(
							  seismograms[1].channel_id.site_code)) {
	    throw new IncompatibleSeismograms("Site codes for 0 and 1 are not the same. "
					      +seismograms[0].channel_id.site_code
					      +" "
					      +seismograms[1].channel_id.site_code);
	}


	// all checks pass, so put into a motion vector
	ChannelId[] channel_group = new ChannelId[3];
	VectorComponent[] data = new VectorComponent[3];
	channel_group[0] = seismograms[0].channel_id;
	data[0] = new VectorComponent(seismograms[0].channel_id, 
				      seismograms[0].data);
	channel_group[1] = seismograms[1].channel_id;
	data[1] = new VectorComponent(seismograms[1].channel_id, 
				      seismograms[1].data);
	channel_group[2] = seismograms[2].channel_id;
	data[1] = new VectorComponent(seismograms[2].channel_id, 
				      seismograms[2].data);
	return new  LocalMotionVectorImpl(seismograms[0].get_id()+"MotionVec",
				 seismograms[0].properties,
				 seismograms[0].begin_time,
				 seismograms[0].num_points,
				 seismograms[0].sampling_info,
				 seismograms[0].y_unit,
				  channel_group,
				 seismograms[0].parm_ids,
				 (TimeInterval[])seismograms[0].time_corrections,
				 seismograms[0].sample_rate_history,
					  data);
    }
    
}// MotionVectorUtil
